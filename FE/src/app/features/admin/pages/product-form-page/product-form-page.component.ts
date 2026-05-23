import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { API_BASE_URL } from '../../../../core/constants/api.constants';
import { ProductPayload, ProductService } from '../../../../core/services/product.service';

type ProductVariant = {
  color: string;
  name: string;
  sku: string;
  price: string;
  salePrice: string;
  cost: string;
  stock: number;
  image: string;
};

@Component({
  selector: 'app-product-form-page',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './product-form-page.component.html',
  styleUrl: './product-form-page.component.css'
})
export class ProductFormPageComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly productService = inject(ProductService);

  readonly isEditMode = this.route.snapshot.paramMap.has('id');
  readonly productId = Number(this.route.snapshot.paramMap.get('id'));
  saving = false;
  uploadingImages = false;
  errorMessage = '';
  successMessage = '';

  gallery = [
    'https://images.unsplash.com/photo-1695048133142-1a20484d2569?auto=format&fit=crop&w=160&q=80',
    'https://images.unsplash.com/photo-1616410011236-7a42121dd981?auto=format&fit=crop&w=160&q=80',
    'https://images.unsplash.com/photo-1592750475338-74b7b21085ab?auto=format&fit=crop&w=160&q=80',
    'https://images.unsplash.com/photo-1598327105666-5b89351aff97?auto=format&fit=crop&w=160&q=80'
  ];

  readonly variants: ProductVariant[] = [
    {
      color: '#c5c6c2',
      name: 'Titan Tự nhiên / 256GB',
      sku: 'IP15PM-256-TITAN-NAT',
      price: '34.990.000',
      salePrice: '29.490.000',
      cost: '24.000.000',
      stock: 50,
      image: this.gallery[0]
    },
    {
      color: '#748796',
      name: 'Titan Xanh / 256GB',
      sku: 'IP15PM-256-TITAN-BLU',
      price: '34.990.000',
      salePrice: '29.490.000',
      cost: '24.000.000',
      stock: 45,
      image: this.gallery[1]
    },
    {
      color: '#2e3237',
      name: 'Titan Đen / 256GB',
      sku: 'IP15PM-256-TITAN-BLK',
      price: '34.990.000',
      salePrice: '29.490.000',
      cost: '24.000.000',
      stock: 30,
      image: this.gallery[2]
    },
    {
      color: '#f2f2ef',
      name: 'Titan Trắng / 256GB',
      sku: 'IP15PM-256-TITAN-WHT',
      price: '34.990.000',
      salePrice: '29.490.000',
      cost: '24.000.000',
      stock: 40,
      image: this.gallery[3]
    }
  ];

  product = {
    name: 'iPhone 15 Pro Max',
    brand: 'Apple',
    category: 'Điện thoại',
    status: 'Đang kinh doanh',
    shortDescription:
      'iPhone 15 Pro Max với thiết kế titan mạnh mẽ, chip A17 Pro vượt trội và hệ thống camera đỉnh cao.',
    detail:
      'iPhone 15 Pro Max là chiếc iPhone cao cấp nhất của Apple với khung titan siêu nhẹ và bền, chip A17 Pro mạnh mẽ, hệ thống camera 48MP chuyên nghiệp và thời lượng pin tốt nhất trên iPhone.\n\n- Thiết kế titan sang trọng, bền bỉ\n- Chip A17 Pro - Hiệu năng vượt trội\n- Camera chính 48MP - Chụp ảnh chuyên nghiệp\n- Màn hình Super Retina XDR 6.7 inch\n- Thời lượng pin cả ngày dài',
    slug: 'iphone-15-pro-max',
    seoTitle: 'iPhone 15 Pro Max - Chính hãng VN/A | PhoneStore',
    seoDescription:
      'Mua iPhone 15 Pro Max chính hãng VN/A tại PhoneStore. Giá tốt, trả góp 0%, bảo hành chính hãng 12 tháng.',
    sku: 'IP15PM-256-TITAN-NAT',
    releaseDate: '2023-09-22',
    warranty: 12,
    origin: 'Việt Nam',
    manufacturer: 'Apple'
  };

  ngOnInit(): void {
    if (this.isEditMode && Number.isFinite(this.productId)) {
      this.productService.getProductDetail(this.productId).subscribe({
        next: (product) => {
          this.product.name = product.name;
          this.product.slug = product.slug;
          this.product.shortDescription = product.description || '';
          this.product.detail = product.description || '';
          this.product.brand = this.brandName(product.brandId);
          this.product.category = product.categoryId === 2 ? 'Phụ kiện' : 'Điện thoại';
          this.product.status = product.isActive ? 'Đang kinh doanh' : 'Ngừng kinh doanh';
          this.product.seoTitle = `${product.name} | PhoneStore`;
          this.product.seoDescription = product.description || '';
          this.product.sku = product.variants?.[0]?.sku || this.product.sku;
          this.gallery = product.images?.map((image) => image.imageUrl).filter(Boolean) || this.gallery;
        }
      });
    }
  }

  onImageSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.addImageFiles(input.files);
    input.value = '';
  }

  onImageDropped(event: DragEvent): void {
    event.preventDefault();
    this.addImageFiles(event.dataTransfer?.files ?? null);
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
  }

  removeImage(index: number): void {
    this.gallery = this.gallery.filter((_, currentIndex) => currentIndex !== index);
  }

  setPrimaryImage(index: number): void {
    const selected = this.gallery[index];
    if (!selected) {
      return;
    }

    this.gallery = [selected, ...this.gallery.filter((_, currentIndex) => currentIndex !== index)];
  }

  imageSrc(imageUrl: string): string {
    if (imageUrl.startsWith('/uploads/')) {
      return `${API_BASE_URL.replace('/api', '')}${imageUrl}`;
    }

    return imageUrl;
  }

  saveProduct(continueEditing: boolean): void {
    const payload = this.toPayload();
    this.saving = true;
    this.errorMessage = '';
    this.successMessage = '';

    const request =
      this.isEditMode && Number.isFinite(this.productId)
        ? this.productService.updateProduct(this.productId, payload)
        : this.productService.createProduct(payload);

    request.subscribe({
      next: (savedProduct) => {
        this.saving = false;
        this.successMessage = 'Đã lưu sản phẩm thành công.';
        if (continueEditing) {
          this.router.navigate(['/admin/products', savedProduct.id, 'edit']);
          return;
        }
        this.router.navigate(['/admin/products']);
      },
      error: (error) => {
        this.saving = false;
        this.errorMessage = this.resolveSaveError(error);
      }
    });
  }

  private resolveSaveError(error: any): string {
    if (error?.status === 401) {
      return 'Backend chưa nhận được quyền Admin từ token đăng nhập. Hãy restart backend/frontend rồi đăng nhập lại tài khoản Admin.';
    }

    if (error?.status === 403) {
      return 'Tài khoản hiện tại không có quyền lưu sản phẩm. Vui lòng đăng nhập bằng tài khoản Admin.';
    }

    if (error?.status === 0) {
      return 'Không kết nối được backend. Kiểm tra backend đang chạy và cấu hình CORS.';
    }

    return error?.error?.message ?? 'Không thể lưu sản phẩm. Vui lòng kiểm tra lại dữ liệu.';
  }

  private addImageFiles(files: FileList | null): void {
    if (!files || files.length === 0) {
      return;
    }

    const imageFiles = Array.from(files).filter((file) => file.type.startsWith('image/'));
    if (imageFiles.length === 0) {
      this.errorMessage = 'Vui lòng chọn đúng định dạng ảnh JPG, PNG hoặc WEBP.';
      return;
    }

    this.uploadingImages = true;
    this.errorMessage = '';

    let completed = 0;
    imageFiles.forEach((file) => {
      this.productService.uploadProductImage(file).subscribe({
        next: (response) => {
          this.gallery = [...this.gallery, response.imageUrl];
        },
        error: (error) => {
          this.errorMessage = error?.error?.message ?? 'Không thể tải ảnh lên. Vui lòng thử lại.';
        },
        complete: () => {
          completed += 1;
          this.uploadingImages = completed < imageFiles.length;
        }
      });
    });
  }

  private toPayload(): ProductPayload {
    return {
      brandId: this.brandId(this.product.brand),
      categoryId: this.categoryId(this.product.category),
      name: this.product.name,
      slug: this.product.slug,
      description: this.product.detail || this.product.shortDescription,
      basePrice: this.parsePrice(this.variants[0]?.salePrice || this.variants[0]?.price),
      thumbnail: this.gallery[0],
      isActive: this.product.status === 'Đang kinh doanh',
      isFeatured: true,
      variants: this.variants.map((variant) => ({
        color: variant.name,
        storage: this.extractStorage(variant.name),
        price: this.parsePrice(variant.price),
        salePrice: this.parsePrice(variant.salePrice),
        stock: variant.stock,
        sku: variant.sku,
        isActive: true
      })),
      images: this.gallery.map((imageUrl, index) => ({
        imageUrl,
        altText: `${this.product.name} ${index + 1}`,
        sortOrder: index
      })),
      specs: [
        { specKey: 'SKU', specValue: this.product.sku, sortOrder: 1 },
        { specKey: 'Bảo hành', specValue: `${this.product.warranty} tháng`, sortOrder: 2 },
        { specKey: 'Xuất xứ', specValue: this.product.origin, sortOrder: 3 },
        { specKey: 'Nhà sản xuất', specValue: this.product.manufacturer, sortOrder: 4 }
      ]
    };
  }

  private brandId(brandName: string): number {
    const brandIds: Record<string, number> = {
      Apple: 1,
      Samsung: 2,
      Xiaomi: 3,
      OPPO: 4,
      Vivo: 5,
      Realme: 6
    };

    return brandIds[brandName] ?? 1;
  }

  private brandName(brandId: number): string {
    const brandNames: Record<number, string> = {
      1: 'Apple',
      2: 'Samsung',
      3: 'Xiaomi',
      4: 'OPPO',
      5: 'Vivo',
      6: 'Realme'
    };

    return brandNames[brandId] ?? 'Apple';
  }

  private categoryId(categoryName: string): number {
    const categoryIds: Record<string, number> = {
      'Điện thoại': 1,
      'Phụ kiện': 2,
      'Máy tính bảng': 3
    };

    return categoryIds[categoryName] ?? 1;
  }

  private parsePrice(value: string | number | undefined): number {
    if (typeof value === 'number') {
      return value;
    }

    return Number((value || '0').replace(/\./g, ''));
  }

  private extractStorage(name: string): string {
    return name.split('/')[1]?.trim() || '256GB';
  }
}
