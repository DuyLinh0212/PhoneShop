import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { Product } from '../../../../core/models/product.models';
import { ProductService } from '../../../../core/services/product.service';

@Component({
  selector: 'app-product-list-page',
  imports: [CommonModule],
  templateUrl: './product-list-page.component.html',
  styleUrl: './product-list-page.component.css'
})
export class ProductListPageComponent implements OnInit {
  products: Product[] = [];
  loading = false;
  errorMessage = '';
  viewMode: 'admin' | 'user' = 'user';

  constructor(
    private readonly productService: ProductService,
    private readonly route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    const roleData = this.route.snapshot.data['viewMode'];
    this.viewMode = roleData === 'admin' ? 'admin' : 'user';
    this.loadProducts();
  }

  loadProducts(): void {
    this.loading = true;
    this.errorMessage = '';

    this.productService.getProducts().subscribe({
      next: (products) => {
        this.products = products;
        this.loading = false;
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage =
          error?.error?.message ?? 'Không thể tải danh sách sản phẩm. Kiểm tra backend WebBanPhone.';
      }
    });
  }

  formatPrice(price: number | string): string {
    const numericPrice = Number(price);
    if (Number.isNaN(numericPrice)) {
      return `${price} VND`;
    }

    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
      maximumFractionDigits: 0
    }).format(numericPrice);
  }
}
