import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { API_BASE_URL } from '../../../../core/constants/api.constants';
import { Product } from '../../../../core/models/product.models';
import { ProductService } from '../../../../core/services/product.service';

type HomeProduct = Product & {
  brandName?: string;
  storage?: string;
  screen?: string;
  camera?: string;
  rating?: number;
  reviews?: number;
};

@Component({
  selector: 'app-product-list-page',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './product-list-page.component.html',
  styleUrl: './product-list-page.component.css'
})
export class ProductListPageComponent implements OnInit {
  products: Product[] = [];
  loading = false;
  errorMessage = '';
  viewMode: 'admin' | 'user' = 'user';
  searchTerm = '';
  selectedFilter = 'all';

  readonly brandMenu = [
    { icon: 'A', name: 'Apple (iPhone)' },
    { icon: 'S', name: 'Samsung' },
    { icon: 'MI', name: 'Xiaomi' },
    { icon: 'OP', name: 'OPPO' },
    { icon: 'V', name: 'Vivo' },
    { icon: 'R', name: 'Realme' },
    { icon: 'G', name: 'Google (Pixel)' },
    { icon: '1+', name: 'OnePlus' },
    { icon: 'N', name: 'Nokia' }
  ];

  readonly heroFeatures = [
    'Chip A17 Pro mạnh mẽ',
    'Camera 48MP chuyên nghiệp',
    'Titanium thiết kế cao cấp'
  ];

  readonly productTabs = ['Nổi bật', 'Mới nhất', 'Bán chạy', 'Giảm giá'];

  readonly promoTiles = [
    {
      title: 'Điện thoại Samsung',
      subtitle: 'Ưu đãi đến 20%',
      image: 'https://images.unsplash.com/photo-1610945265064-0e34e5519bbf?auto=format&fit=crop&w=460&q=80'
    },
    {
      title: 'Xiaomi 14 Series',
      subtitle: 'Hiệu năng đỉnh cao',
      image: 'https://images.unsplash.com/photo-1598327105666-5b89351aff97?auto=format&fit=crop&w=460&q=80'
    },
    {
      title: 'OPPO Reno11 Series',
      subtitle: 'Chân dung chuyên gia',
      image: 'https://images.unsplash.com/photo-1580910051074-3eb694886505?auto=format&fit=crop&w=460&q=80'
    },
    {
      title: 'Trả góp 0%',
      subtitle: 'Duyệt nhanh 5 phút',
      image: ''
    }
  ];

  readonly serviceHighlights = [
    { icon: '#', title: 'Hàng chính hãng 100%', text: 'Cam kết chất lượng' },
    { icon: '*', title: 'Bảo hành chính hãng', text: '12 - 24 tháng' },
    { icon: '+', title: 'Miễn phí giao hàng', text: 'Đơn từ 500.000đ' },
    { icon: '<>', title: 'Đổi trả dễ dàng', text: 'Trong 7 ngày' },
    { icon: '$', title: 'Thanh toán an toàn', text: 'Nhiều phương thức' }
  ];

  get inventorySummary(): { total: number; low: number; out: number } {
    const total = this.products.reduce((sum, product) => sum + Number(product.totalStock ?? 0), 0);
    const low = this.products.filter((product) => {
      const stock = Number(product.totalStock ?? 0);
      return stock > 0 && stock <= 5;
    }).length;
    const out = this.products.filter((product) => Number(product.totalStock ?? 0) <= 0).length;
    return { total, low, out };
  }

  readonly fallbackProducts: HomeProduct[] = [
    {
      id: 1001,
      brandId: 1,
      categoryId: 1,
      brandName: 'Apple',
      name: 'iPhone 15 Pro Max',
      slug: 'iphone-15-pro-max',
      description: 'Titanium. Camera 48MP. Chip A17 Pro.',
      basePrice: 29490000,
      originalPrice: 30990000,
      salePrice: 29490000,
      discountPercent: 5,
      thumbnail: 'https://images.unsplash.com/photo-1695048133142-1a20484d2569?auto=format&fit=crop&w=520&q=80',
      isActive: true,
      isFeatured: true,
      viewCount: 128,
      storage: '256GB',
      screen: '6.7"',
      camera: '48MP',
      rating: 0,
      reviews: 0
    },
    {
      id: 1002,
      brandId: 2,
      categoryId: 1,
      brandName: 'Samsung',
      name: 'Samsung Galaxy S24 Ultra',
      slug: 'samsung-galaxy-s24-ultra',
      description: 'Màn hình lớn, S Pen, camera 200MP.',
      basePrice: 24990000,
      originalPrice: 27990000,
      salePrice: 24990000,
      discountPercent: 10.72,
      thumbnail: 'https://images.unsplash.com/photo-1610945265064-0e34e5519bbf?auto=format&fit=crop&w=520&q=80',
      isActive: true,
      isFeatured: true,
      viewCount: 96,
      storage: '256GB',
      screen: '6.8"',
      camera: '200MP',
      rating: 0,
      reviews: 0
    },
    {
      id: 1003,
      brandId: 3,
      categoryId: 1,
      brandName: 'Xiaomi',
      name: 'Xiaomi 14',
      slug: 'xiaomi-14',
      description: 'Snapdragon cao cấp, sạc nhanh.',
      basePrice: 16990000,
      thumbnail: 'https://images.unsplash.com/photo-1598327105666-5b89351aff97?auto=format&fit=crop&w=520&q=80',
      isActive: true,
      isFeatured: true,
      viewCount: 74,
      storage: '256GB',
      screen: '6.36"',
      camera: '50MP',
      rating: 0,
      reviews: 0
    },
    {
      id: 1004,
      brandId: 4,
      categoryId: 1,
      brandName: 'OPPO',
      name: 'OPPO Reno11 5G',
      slug: 'oppo-reno11-5g',
      description: 'Chụp chân dung nổi bật.',
      basePrice: 10990000,
      originalPrice: 11990000,
      salePrice: 10990000,
      discountPercent: 8.34,
      thumbnail: 'https://images.unsplash.com/photo-1580910051074-3eb694886505?auto=format&fit=crop&w=520&q=80',
      isActive: true,
      isFeatured: true,
      viewCount: 74,
      storage: '256GB',
      screen: '6.7"',
      camera: '50MP',
      rating: 0,
      reviews: 0
    },
    {
      id: 1005,
      brandId: 5,
      categoryId: 1,
      brandName: 'Vivo',
      name: 'vivo V30 5G',
      slug: 'vivo-v30-5g',
      description: 'Thiết kế mỏng nhẹ, camera Aura.',
      basePrice: 9490000,
      thumbnail: 'https://images.unsplash.com/photo-1565849904461-04a58ad377e0?auto=format&fit=crop&w=520&q=80',
      isActive: true,
      isFeatured: true,
      viewCount: 42,
      storage: '256GB',
      screen: '6.78"',
      camera: '50MP',
      rating: 0,
      reviews: 0
    },
    {
      id: 1006,
      brandId: 6,
      categoryId: 1,
      brandName: 'Realme',
      name: 'realme 12 Pro+ 5G',
      slug: 'realme-12-pro-plus-5g',
      description: 'Zoom tốt, pin lớn.',
      basePrice: 8990000,
      originalPrice: 9990000,
      salePrice: 8990000,
      discountPercent: 10.01,
      thumbnail: 'https://images.unsplash.com/photo-1601784551446-20c9e07cdbdb?auto=format&fit=crop&w=520&q=80',
      isActive: true,
      isFeatured: true,
      viewCount: 35,
      storage: '256GB',
      screen: '6.7"',
      camera: '64MP',
      rating: 0,
      reviews: 0
    }
  ];

  constructor(
    private readonly productService: ProductService,
    private readonly route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    const roleData = this.route.snapshot.data['viewMode'];
    this.viewMode = roleData === 'admin' ? 'admin' : 'user';
    this.route.queryParamMap.subscribe((params) => {
      this.searchTerm = params.get('q') ?? '';
      this.selectedFilter = params.get('filter') ?? 'all';
    });
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

  get homeProducts(): HomeProduct[] {
    const source = this.products.length > 0 ? this.products : this.fallbackProducts;
    return source.map((product, index) => ({
      ...product,
      brandName: this.brandName(product.brandId),
      storage: this.storageFor(index),
      screen: this.screenFor(index),
      camera: this.cameraFor(index),
      rating: this.averageRating(product),
      reviews: this.reviewCount(product),
      thumbnail: product.thumbnail || this.fallbackProducts[index % this.fallbackProducts.length].thumbnail
    }));
  }

  get filteredHomeProducts(): HomeProduct[] {
    const keyword = this.searchTerm.trim().toLowerCase();

    return this.homeProducts.filter((product) => {
      const matchesKeyword =
        !keyword ||
        [product.name, product.brandName, product.description, product.storage, product.camera]
          .filter(Boolean)
          .some((value) => String(value).toLowerCase().includes(keyword));

      const stock = Number(product.totalStock ?? 1);
      const rating = Number(product.rating ?? 0);
      const matchesFilter =
        this.selectedFilter === 'all' ||
        (this.selectedFilter === 'featured' && product.isFeatured) ||
        (this.selectedFilter === 'sale' && this.hasDiscount(product)) ||
        (this.selectedFilter === 'top-rated' && rating >= 4.7) ||
        (this.selectedFilter === 'in-stock' && stock > 0);

      return matchesKeyword && matchesFilter;
    });
  }

  get productStats(): { total: number; averageRating: string; reviews: number; inStock: number } {
    const products = this.filteredHomeProducts;
    const reviews = products.reduce((sum, product) => sum + Number(product.reviews ?? 0), 0);
    const totalRating = products.reduce(
      (sum, product) => sum + Number(product.rating ?? 0) * Number(product.reviews ?? 0),
      0
    );
    const inStock = products.filter((product) => Number(product.totalStock ?? 1) > 0).length;

    return {
      total: products.length,
      averageRating: reviews > 0 ? (totalRating / reviews).toFixed(1) : '0.0',
      reviews,
      inStock
    };
  }

  averageRating(product: Product): number {
    const value = Number(product.averageRating ?? 0);
    return Number.isFinite(value) ? Math.round(value * 10) / 10 : 0;
  }

  reviewCount(product: Product): number {
    const value = Number(product.reviewCount ?? 0);
    return Number.isFinite(value) ? value : 0;
  }

  brandName(brandId: number): string {
    const names: Record<number, string> = {
      1: 'Apple',
      2: 'Samsung',
      3: 'Xiaomi',
      4: 'OPPO',
      5: 'Vivo',
      6: 'Realme'
    };

    return names[brandId] ?? 'PhoneStore';
  }

  storageFor(index: number): string {
    return ['256GB', '256GB', '256GB', '256GB', '256GB', '256GB'][index % 6];
  }

  screenFor(index: number): string {
    return ['6.7"', '6.8"', '6.36"', '6.7"', '6.78"', '6.7"'][index % 6];
  }

  cameraFor(index: number): string {
    return ['48MP', '200MP', '50MP', '50MP', '50MP', '64MP'][index % 6];
  }

  displayPrice(product: Product): number {
    if (this.hasDiscount(product)) {
      return Number(product.salePrice ?? product.basePrice);
    }

    return Number(product.basePrice ?? product.originalPrice ?? 0);
  }

  originalPrice(product: Product): number {
    return Number(product.originalPrice ?? product.basePrice ?? 0);
  }

  discountPercent(product: Product): number {
    const explicitPercent = Number(product.discountPercent ?? 0);
    if (Number.isFinite(explicitPercent) && explicitPercent > 0) {
      return Math.round(explicitPercent);
    }

    const originalPrice = this.originalPrice(product);
    const salePrice = Number(product.salePrice ?? product.basePrice ?? 0);
    if (originalPrice <= 0 || salePrice <= 0 || salePrice >= originalPrice) {
      return 0;
    }

    return Math.round((originalPrice - salePrice) * 100 / originalPrice);
  }

  hasDiscount(product: Product): boolean {
    const originalPrice = this.originalPrice(product);
    const salePrice = Number(product.salePrice ?? product.basePrice ?? 0);
    return originalPrice > 0 && salePrice > 0 && salePrice < originalPrice && this.discountPercent(product) > 0;
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

  stockLabel(stock: number | undefined): string {
    const value = Number(stock ?? 0);
    if (value <= 0) {
      return 'Hết hàng';
    }
    if (value <= 5) {
      return `Sắp hết: ${value}`;
    }
    return `Tồn kho: ${value}`;
  }

  stockTone(stock: number | undefined): string {
    const value = Number(stock ?? 0);
    if (value <= 0) {
      return 'out';
    }
    if (value <= 5) {
      return 'low';
    }
    return 'ok';
  }
}
