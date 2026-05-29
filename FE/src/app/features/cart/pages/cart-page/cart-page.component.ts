import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { API_BASE_URL } from '../../../../core/constants/api.constants';
import { Cart, CartItem, CartService } from '../../../../core/services/cart.service';
import { OrderService } from '../../../../core/services/order.service';

@Component({
  selector: 'app-cart-page',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './cart-page.component.html',
  styleUrl: './cart-page.component.css'
})
export class CartPageComponent implements OnInit {
  cart: Cart = { items: [], totalQuantity: 0, subtotal: 0 };
  loading = false;
  placingOrder = false;
  message = '';

  checkout = {
    shippingName: '',
    shippingPhone: '',
    shippingAddress: '',
    paymentMethod: 'cod',
    note: ''
  };

  constructor(
    private readonly cartService: CartService,
    private readonly orderService: OrderService
  ) {}

  ngOnInit(): void {
    this.loading = true;
    this.cartService.loadCart().subscribe({
      next: (cart) => {
        this.cart = cart;
        this.loading = false;
      },
      error: () => {
        this.message = 'Không thể tải giỏ hàng.';
        this.loading = false;
      }
    });
  }

  updateQuantity(item: CartItem, quantity: number): void {
    this.cartService.updateItem(item.id, quantity).subscribe((cart) => (this.cart = cart));
  }

  removeItem(item: CartItem): void {
    this.cartService.removeItem(item.id).subscribe((cart) => (this.cart = cart));
  }

  placeOrder(): void {
    if (!this.checkout.shippingName || !this.checkout.shippingPhone || !this.checkout.shippingAddress) {
      this.message = 'Vui lòng nhập đủ thông tin giao hàng.';
      return;
    }

    const invalidItem = this.cart.items.find((item) => item.quantity > item.stock || item.stock <= 0);
    if (invalidItem) {
      this.message = `${invalidItem.productName} không đủ tồn kho. Hiện còn ${invalidItem.stock} sản phẩm.`;
      return;
    }

    this.placingOrder = true;
    this.message = '';
    this.orderService.checkout(this.checkout).subscribe({
      next: (order) => {
        this.cart = { items: [], totalQuantity: 0, subtotal: 0 };
        this.cartService.setCart(this.cart);
        this.message = `Đặt hàng thành công. Mã đơn #${order.id}.`;
        this.placingOrder = false;
      },
      error: (error) => {
        this.message = error?.error?.message ?? 'Không thể đặt hàng.';
        this.placingOrder = false;
      }
    });
  }

  shippingFee(): number {
    return Number(this.cart.subtotal) >= 500000 ? 0 : this.cart.items.length ? 30000 : 0;
  }

  total(): number {
    return Number(this.cart.subtotal) + this.shippingFee();
  }

  canIncrease(item: CartItem): boolean {
    return item.quantity < item.stock;
  }

  imageSrc(imageUrl?: string): string {
    if (!imageUrl) {
      return 'https://images.unsplash.com/photo-1598327105666-5b89351aff97?auto=format&fit=crop&w=300&q=80';
    }

    return imageUrl.startsWith('/uploads/') ? `${API_BASE_URL.replace('/api', '')}${imageUrl}` : imageUrl;
  }

  formatPrice(price: number | string): string {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
      maximumFractionDigits: 0
    }).format(Number(price));
  }
}
