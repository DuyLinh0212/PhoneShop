import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { API_BASE_URL } from '../../../../core/constants/api.constants';
import { CartService } from '../../../../core/services/cart.service';
import { ProductDetail, ProductService, ProductVariantPayload, ReviewSummary } from '../../../../core/services/product.service';
import { WishlistService } from '../../../../core/services/wishlist.service';

@Component({
  selector: 'app-product-detail-page',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './product-detail-page.component.html',
  styleUrl: './product-detail-page.component.css'
})
export class ProductDetailPageComponent implements OnInit {
  product: ProductDetail | null = null;
  loading = false;
  errorMessage = '';
  selectedImage = '';
  selectedVariantIndex = 0;
  quantity = 1;
  activeTab: 'specs' | 'description' | 'reviews' | 'questions' = 'specs';
  actionMessage = '';
  reviewSummary: ReviewSummary = { averageRating: 0, totalReviews: 0, reviews: [] };
  reviewForm = {
    rating: 5,
    title: '',
    content: ''
  };
  reviewMessage = '';
  reviewSubmitting = false;
  favorite = false;
  favoriteLoading = false;

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
    private readonly router: Router,
    private readonly productService: ProductService,
    private readonly cartService: CartService,
    private readonly wishlistService: WishlistService
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
        this.loadReviews(product.id);
        this.loadFavoriteStatus(product.id);
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage =
          error?.error?.message ?? 'Không thể tải thông tin sản phẩm. Vui lòng thử lại.';
      }
    });
  }

  loadReviews(productId: number): void {
    this.productService.getReviews(productId).subscribe({
      next: (summary) => (this.reviewSummary = summary),
      error: () => (this.reviewSummary = { averageRating: 0, totalReviews: 0, reviews: [] })
    });
  }

  loadFavoriteStatus(productId: number): void {
    this.wishlistService.getStatus(productId).subscribe({
      next: (status) => (this.favorite = Boolean(status.favorite)),
      error: () => (this.favorite = false)
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
    if (variant) {
      return this.variantDisplayPrice(variant);
    }

    if (this.product && this.productHasDiscount()) {
      return this.product.salePrice ?? this.product.basePrice ?? 0;
    }

    return this.product?.basePrice ?? 0;
  }

  get oldPrice(): number {
    const variant = this.selectedVariant;
    if (variant) {
      return Number(variant.price ?? 0);
    }

    return Number(this.product?.originalPrice ?? this.product?.basePrice ?? 0);
  }

  get hasDiscount(): boolean {
    const variant = this.selectedVariant;
    return variant ? this.variantHasDiscount(variant) : this.productHasDiscount();
  }

  get discountPercent(): number {
    const variant = this.selectedVariant;
    return variant ? this.variantDiscountPercent(variant) : this.productDiscountPercent();
  }

  get colorOptions(): ProductVariantPayload[] {
    const variants = this.product?.variants ?? [];
    const source = variants.length > 0 ? variants : this.fallbackVariants;
    const seenColors = new Set<string>();

    return source.filter((variant) => {
      const key = this.colorKey(variant.color);
      if (seenColors.has(key)) {
        return false;
      }

      seenColors.add(key);
      return true;
    });
  }

  get storageOptions(): string[] {
    const selectedColor = this.colorKey(this.selectedVariant?.color);
    const variants = (this.product?.variants?.length ? this.product.variants : this.fallbackVariants)
      .filter((variant) => this.colorKey(variant.color) === selectedColor);
    const values = variants.map((variant) => variant.storage || '256GB');
    return [...new Set(values)];
  }

  get fallbackVariants(): ProductVariantPayload[] {
    return [
      { color: 'Titan tự nhiên', storage: '256GB', price: 30990000, salePrice: 29490000, discountPercent: 5, stock: 20 },
      { color: 'Titan xanh', storage: '512GB', price: 36990000, salePrice: 34990000, discountPercent: 5.41, stock: 12 },
      { color: 'Titan đen', storage: '1TB', price: 42990000, salePrice: 39990000, discountPercent: 6.98, stock: 8 }
    ];
  }

  selectVariant(index: number): void {
    this.selectedVariantIndex = index;
  }

  selectColor(color: string | undefined): void {
    const variants = this.product?.variants?.length ? this.product.variants : this.fallbackVariants;
    const currentStorage = this.selectedVariant?.storage;
    const selectedColor = this.colorKey(color);
    const sameStorageIndex = variants.findIndex(
      (variant) => this.colorKey(variant.color) === selectedColor && variant.storage === currentStorage
    );

    this.selectedVariantIndex = sameStorageIndex >= 0
      ? sameStorageIndex
      : variants.findIndex((variant) => this.colorKey(variant.color) === selectedColor);
    if (this.selectedVariantIndex < 0) {
      this.selectedVariantIndex = 0;
    }
  }

  selectStorage(storage: string): void {
    const variants = this.product?.variants?.length ? this.product.variants : this.fallbackVariants;
    const selectedColor = this.colorKey(this.selectedVariant?.color);
    const nextIndex = variants.findIndex(
      (variant) => this.colorKey(variant.color) === selectedColor && (variant.storage || '256GB') === storage
    );

    if (nextIndex >= 0) {
      this.selectedVariantIndex = nextIndex;
    }
  }

  selectImage(imageUrl: string): void {
    this.selectedImage = imageUrl;
  }

  decreaseQuantity(): void {
    this.quantity = Math.max(1, this.quantity - 1);
  }

  increaseQuantity(): void {
    const stock = this.selectedVariant?.stock ?? 99;
    this.quantity = Math.min(stock, this.quantity + 1);
  }

  addToCart(): void {
    const variantId = this.selectedVariant?.id;
    if (!variantId) {
      this.actionMessage = 'Sản phẩm chưa có biến thể hợp lệ.';
      return;
    }

    this.cartService.addItem(variantId, this.quantity).subscribe({
      next: () => {
        this.actionMessage = 'Đã thêm sản phẩm vào giỏ hàng.';
      },
      error: (error) => {
        this.actionMessage = error?.error?.message ?? 'Không thể thêm vào giỏ hàng.';
      }
    });
  }

  buyNow(): void {
    const variantId = this.selectedVariant?.id;
    if (!variantId) {
      this.actionMessage = 'Sản phẩm chưa có biến thể hợp lệ.';
      return;
    }

    this.cartService.addItem(variantId, this.quantity).subscribe({
      next: () => this.router.navigate(['/cart']),
      error: (error) => {
        this.actionMessage = error?.error?.message ?? 'Không thể mua sản phẩm này.';
      }
    });
  }

  toggleFavorite(): void {
    if (!this.product || this.favoriteLoading) {
      return;
    }

    this.favoriteLoading = true;
    const request = this.favorite
      ? this.wishlistService.removeFavorite(this.product.id)
      : this.wishlistService.addFavorite(this.product.id);

    request.subscribe({
      next: (status) => {
        this.favorite = Boolean(status.favorite);
        this.favoriteLoading = false;
        this.actionMessage = this.favorite ? 'Đã thêm vào sản phẩm yêu thích.' : 'Đã bỏ khỏi sản phẩm yêu thích.';
      },
      error: (error) => {
        this.favoriteLoading = false;
        this.actionMessage = error?.error?.message ?? 'Không thể cập nhật sản phẩm yêu thích.';
      }
    });
  }

  submitReview(): void {
    if (!this.product) {
      return;
    }

    this.reviewSubmitting = true;
    this.reviewMessage = '';
    this.productService.createReview(this.product.id, this.reviewForm).subscribe({
      next: () => {
        this.reviewSubmitting = false;
        this.reviewMessage = 'Da gui danh gia cua ban.';
        this.reviewForm = { rating: 5, title: '', content: '' };
        this.loadReviews(this.product!.id);
      },
      error: (error) => {
        this.reviewSubmitting = false;
        this.reviewMessage = error?.error?.message ?? 'Khong the gui danh gia.';
      }
    });
  }

  setReviewRating(rating: number): void {
    this.reviewForm.rating = rating;
  }

  starList(rating: number): number[] {
    return Array.from({ length: Math.max(0, Math.min(5, Math.round(rating))) }, (_, index) => index + 1);
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

  displayColor(color: string | undefined): string {
    return (color || 'Màu mặc định').split('/')[0].trim();
  }

  private variantDisplayPrice(variant: ProductVariantPayload): number {
    const price = Number(variant.price ?? 0);
    const discountPercent = this.variantDiscountPercent(variant);
    if (price > 0 && discountPercent > 0) {
      return Math.round(price * (100 - discountPercent) / 100);
    }

    const salePrice = Number(variant.salePrice ?? 0);
    return salePrice > 0 && salePrice < price ? salePrice : price;
  }

  private variantHasDiscount(variant: ProductVariantPayload): boolean {
    const price = Number(variant.price ?? 0);
    const salePrice = this.variantDisplayPrice(variant);
    return price > 0 && salePrice > 0 && salePrice < price && this.variantDiscountPercent(variant) > 0;
  }

  private variantDiscountPercent(variant: ProductVariantPayload): number {
    const explicitPercent = Number(variant.discountPercent ?? 0);
    if (Number.isFinite(explicitPercent) && explicitPercent > 0) {
      return explicitPercent;
    }

    const price = Number(variant.price ?? 0);
    const salePrice = Number(variant.salePrice ?? 0);
    if (!Number.isFinite(price) || !Number.isFinite(salePrice) || price <= 0 || salePrice <= 0 || salePrice >= price) {
      return 0;
    }

    return Math.round(((price - salePrice) * 100 / price) * 100) / 100;
  }

  private productHasDiscount(): boolean {
    if (!this.product) {
      return false;
    }

    const originalPrice = Number(this.product.originalPrice ?? this.product.basePrice ?? 0);
    const salePrice = Number(this.product.salePrice ?? this.product.basePrice ?? 0);
    return originalPrice > 0 && salePrice > 0 && salePrice < originalPrice && this.productDiscountPercent() > 0;
  }

  private productDiscountPercent(): number {
    if (!this.product) {
      return 0;
    }

    const explicitPercent = Number(this.product.discountPercent ?? 0);
    if (Number.isFinite(explicitPercent) && explicitPercent > 0) {
      return explicitPercent;
    }

    const originalPrice = Number(this.product.originalPrice ?? this.product.basePrice ?? 0);
    const salePrice = Number(this.product.salePrice ?? this.product.basePrice ?? 0);
    if (originalPrice <= 0 || salePrice <= 0 || salePrice >= originalPrice) {
      return 0;
    }

    return Math.round(((originalPrice - salePrice) * 100 / originalPrice) * 100) / 100;
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

  colorSwatch(color: string | undefined): string {
    const normalized = this.normalizeText(this.displayColor(color));
    const colorMap: Array<[string, string]> = [
      ['xanh duong', '#2563eb'],
      ['xanh bien', '#0ea5e9'],
      ['xanh ngoc', '#14b8a6'],
      ['xanh la', '#16a34a'],
      ['green', '#16a34a'],
      ['blue', '#2563eb'],
      ['titan sa mac', '#b9a88f'],
      ['sa mac', '#b9a88f'],
      ['titan tu nhien', '#8b8172'],
      ['tu nhien', '#8b8172'],
      ['titan trang', '#e6e4df'],
      ['trang', '#f3f4f6'],
      ['titan den', '#2b2f38'],
      ['den', '#1f2937'],
      ['xanh', '#2563eb'],
      ['hong', '#f4b6c2'],
      ['do', '#dc2626'],
      ['vang', '#eab308'],
      ['bac', '#cbd5e1'],
      ['xam', '#64748b'],
      ['cam', '#b77942'],
      ['tim', '#7c3aed']
    ];

    return colorMap.find(([name]) => normalized.includes(name))?.[1] ?? '#94a3b8';
  }

  private colorKey(color: string | undefined): string {
    return this.normalizeText(this.displayColor(color));
  }

  private normalizeText(value: string | undefined): string {
    return (value || '')
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .replace(/đ/g, 'd')
      .replace(/Đ/g, 'D')
      .trim()
      .toLowerCase();
  }
}
