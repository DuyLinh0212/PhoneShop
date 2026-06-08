import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { API_BASE_URL } from '../../../../core/constants/api.constants';
import { Order, OrderService } from '../../../../core/services/order.service';
import { ProductDetail } from '../../../../core/services/product.service';
import { AddressSummary, Profile, ProfileService } from '../../../../core/services/profile.service';
import { WishlistService } from '../../../../core/services/wishlist.service';

@Component({
  selector: 'app-profile-page',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './profile-page.component.html',
  styleUrl: './profile-page.component.css'
})
export class ProfilePageComponent implements OnInit {
  profile: Profile | null = null;
  orders: Order[] = [];
  favorites: ProductDetail[] = [];
  selectedOrder: Order | null = null;
  activeTab: 'info' | 'orders' | 'address' | 'security' | 'favorites' = 'info';
  orderStatusFilter = 'all';
  orderSearch = '';
  message = '';

  form = {
    fullName: '',
    phone: ''
  };

  addressForm = {
    fullName: '',
    phone: '',
    province: '',
    district: '',
    ward: '',
    street: ''
  };

  constructor(
    private readonly profileService: ProfileService,
    private readonly orderService: OrderService,
    private readonly wishlistService: WishlistService
  ) {}

  ngOnInit(): void {
    this.loadProfile();
    this.loadOrders();
    this.loadFavorites();
  }

  loadOrders(): void {
    this.orderService.getOrders().subscribe((orders) => (this.orders = orders));
  }

  loadProfile(): void {
    this.profileService.getProfile().subscribe({
      next: (profile) => {
        this.profile = profile;
        this.form.fullName = profile.fullName;
        this.form.phone = profile.phone || '';
        this.applyAddressForm(profile.defaultAddress);
      },
      error: () => {
        this.message = 'Không thể tải hồ sơ.';
      }
    });
  }

  loadFavorites(): void {
    this.wishlistService.getFavorites().subscribe({
      next: (favorites) => (this.favorites = favorites),
      error: () => (this.favorites = [])
    });
  }

  saveProfile(): void {
    this.profileService.updateProfile(this.form).subscribe({
      next: (profile) => {
        this.profile = profile;
        this.message = 'Đã lưu thay đổi.';
      },
      error: (error) => {
        this.message = error?.error?.message ?? 'Không thể lưu hồ sơ.';
      }
    });
  }

  initials(): string {
    return (this.profile?.fullName || 'U').trim().charAt(0).toUpperCase();
  }

  filteredOrders(): Order[] {
    const keyword = this.orderSearch.trim().toLowerCase();
    return this.orders.filter((order) => {
      const matchesStatus = this.orderStatusFilter === 'all' || order.status === this.orderStatusFilter;
      const matchesKeyword = !keyword || `dh${order.id}`.includes(keyword) || order.items.some(
        (item) => item.productName.toLowerCase().includes(keyword)
      );
      return matchesStatus && matchesKeyword;
    });
  }

  countOrders(status?: string): number {
    return status ? this.orders.filter((order) => order.status === status).length : this.orders.length;
  }

  orderItemCount(order: Order): number {
    return order.items.reduce((sum, item) => sum + item.quantity, 0);
  }

  orderSubtotal(order: Order): number {
    return order.items.reduce((sum, item) => sum + Number(item.subtotal ?? 0), 0);
  }

  cancelOrder(order: Order): void {
    this.orderService.cancelOrder(order.id).subscribe({
      next: () => {
        this.message = `Đã hủy đơn #DH${order.id}.`;
        this.loadOrders();
      },
      error: (error) => {
        this.message = error?.error?.message ?? 'Không thể hủy đơn hàng.';
      }
    });
  }

  openOrderDetail(order: Order): void {
    this.selectedOrder = order;
  }

  closeOrderDetail(): void {
    this.selectedOrder = null;
  }

  saveDefaultAddress(): void {
    this.profileService.saveDefaultAddress(this.addressForm).subscribe({
      next: (address) => {
        if (this.profile) {
          this.profile = { ...this.profile, defaultAddress: address };
        }
        this.applyAddressForm(address);
        this.message = 'Đã lưu địa chỉ giao hàng mặc định.';
      },
      error: (error) => {
        this.message = error?.error?.message ?? 'Không thể lưu địa chỉ giao hàng.';
      }
    });
  }

  removeFavorite(product: ProductDetail): void {
    this.wishlistService.removeFavorite(product.id).subscribe({
      next: () => {
        this.favorites = this.favorites.filter((item) => item.id !== product.id);
        if (this.profile) {
          this.profile = { ...this.profile, wishlistCount: this.favorites.length };
        }
        this.message = 'Đã bỏ sản phẩm khỏi danh sách yêu thích.';
      },
      error: (error) => {
        this.message = error?.error?.message ?? 'Không thể bỏ yêu thích sản phẩm.';
      }
    });
  }

  statusLabel(status: string): string {
    const labels: Record<string, string> = {
      pending: 'Chờ xác nhận',
      confirmed: 'Đang xử lý',
      processing: 'Đang xử lý',
      shipping: 'Đang giao',
      delivered: 'Hoàn thành',
      cancelled: 'Đã hủy',
      refunded: 'Hoàn tiền'
    };
    return labels[status] ?? status;
  }

  statusClass(status: string): string {
    return `status ${status}`;
  }

  formatDate(value?: string): string {
    if (!value) {
      return '';
    }
    return new Intl.DateTimeFormat('vi-VN').format(new Date(value));
  }

  fullAddress(address: AddressSummary | null | undefined): string {
    if (!address) {
      return '';
    }

    return [address.street, address.ward, address.district, address.province].filter(Boolean).join(', ');
  }

  favoritePrice(product: ProductDetail): number {
    return Number(product.salePrice ?? product.basePrice ?? 0);
  }

  favoriteOldPrice(product: ProductDetail): number {
    return Number(product.originalPrice ?? product.basePrice ?? 0);
  }

  favoriteHasDiscount(product: ProductDetail): boolean {
    const oldPrice = this.favoriteOldPrice(product);
    const price = this.favoritePrice(product);
    return oldPrice > 0 && price > 0 && price < oldPrice;
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

  private applyAddressForm(address: AddressSummary | null | undefined): void {
    this.addressForm = {
      fullName: address?.fullName || this.profile?.fullName || '',
      phone: address?.phone || this.profile?.phone || '',
      province: address?.province || '',
      district: address?.district || '',
      ward: address?.ward || '',
      street: address?.street || ''
    };
  }
}
