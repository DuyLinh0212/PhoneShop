import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { API_BASE_URL } from '../../../../core/constants/api.constants';
import { ProductDetail, ProductService, ProductVariantPayload } from '../../../../core/services/product.service';

@Component({
  selector: 'app-product-detail-page',
  imports: [CommonModule, RouterLink],
  templateUrl: './product-detail-page.component.html',
  styleUrl: './product-detail-page.component.css'
})
export class ProductDetailPageComponent implements OnInit {
  product: ProductDetail | null = null;
  loading = false;
  errorMessage = '';
  selectedImage = '';
  selectedVariantIndex = 0;
  activeTab: 'specs' | 'description' | 'reviews' | 'questions' = 'specs';

  readonly fallbackImage =
    'https://images.unsplash.com/photo-1695048133142-1a20484d2569?auto=format&fit=crop&w=900&q=85';

  readonly fallbackGallery = [
    this.fallbackImage,
    'https://images.unsplash.com/photo-1616410011236-7a42121dd981?auto=format&fit=crop&w=900&q=85',
    'https://images.unsplash.com/photo-1592750475338-74b7b21085ab?auto=format&fit=crop&w=900&q=85',
    'https://images.unsplash.com/photo-1598327105666-5b89351aff97?auto=format&fit=crop&w=900&q=85'
  ];

  readonly quickSpecs = [
    { icon: 'CPU', label: 'Chip A17 Pro' },
    { icon: 'CAM', label: 'Camera 48MP' },
    { icon: '6.7', label: 'Màn hình 6.7 inch' },
    { icon: 'PIN', label: 'Pin dùng cả ngày' }
  ];

  constructor(
    private readonly route: ActivatedRoute,
    private readonly productService: ProductService
  ) {}

  ngOnInit(): void {
    const productId = Number(this.route.snapshot.paramMap.get('id'));
    if (!Number.isFinite(productId)) {
      this.errorMessage = 'Không tìm thấy sản phẩm.';
      return;
    }

    this.loading = true;
    this.productService.getProductDetail(productId).subscribe({
      next: (product) => {
        this.product = product;
        this.selectedImage = this.gallery[0] || this.fallbackImage;
        this.loading = false;
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage =
          error?.error?.message ?? 'Không thể tải thông tin sản phẩm. Vui lòng thử lại.';
      }
    });
  }

  get gallery(): string[] {
    if (!this.product) {
      return this.fallbackGallery;
    }

    const images = this.product.images?.map((image) => image.imageUrl).filter(Boolean) ?? [];
    if (this.product.thumbnail) {
      images.unshift(this.product.thumbnail);
    }

    return [...new Set(images.length > 0 ? images : this.fallbackGallery)];
  }

  get selectedVariant(): ProductVariantPayload | null {
    return this.product?.variants?.[this.selectedVariantIndex] ?? this.product?.variants?.[0] ?? null;
  }

  get displayPrice(): number | string {
    const variant = this.selectedVariant;
    return variant?.salePrice ?? variant?.price ?? this.product?.basePrice ?? 0;
  }

  get oldPrice(): number {
    const variant = this.selectedVariant;
    const price = Number(variant?.price ?? this.product?.basePrice ?? 0);
    return Math.round(price * 1.05);
  }

  get colorOptions(): ProductVariantPayload[] {
    const variants = this.product?.variants ?? [];
    return variants.length > 0 ? variants : this.fallbackVariants;
  }

  get storageOptions(): string[] {
    const values = this.colorOptions.map((variant) => variant.storage || '256GB');
    return [...new Set(values)];
  }

  get fallbackVariants(): ProductVariantPayload[] {
    return [
      { color: 'Titan tự nhiên', storage: '256GB', price: 30990000, salePrice: 29490000, stock: 20 },
      { color: 'Titan xanh', storage: '512GB', price: 36990000, salePrice: 34990000, stock: 12 },
      { color: 'Titan đen', storage: '1TB', price: 42990000, salePrice: 39990000, stock: 8 }
    ];
  }

  selectVariant(index: number): void {
    this.selectedVariantIndex = index;
  }

  selectImage(imageUrl: string): void {
    this.selectedImage = imageUrl;
  }

  brandName(brandId: number | undefined): string {
    const names: Record<number, string> = {
      1: 'Apple',
      2: 'Samsung',
      3: 'Xiaomi',
      4: 'OPPO',
      5: 'Vivo',
      6: 'Realme'
    };

    return brandId ? names[brandId] ?? 'PhoneStore' : 'PhoneStore';
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

  imageSrc(imageUrl: string): string {
    if (imageUrl.startsWith('/uploads/')) {
      return `${API_BASE_URL.replace('/api', '')}${imageUrl}`;
    }

    return imageUrl;
  }
}
