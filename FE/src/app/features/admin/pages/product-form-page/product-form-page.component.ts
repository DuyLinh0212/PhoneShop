import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';

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
  private readonly authService = inject(AuthService);

  readonly isEditMode = this.route.snapshot.paramMap.has('id');
  readonly productId = Number(this.route.snapshot.paramMap.get('id'));
  saving = false;
  uploadingImages = false;
  errorMessage = '';
  successMessage = '';

  gallery: string[] = [];

  variants: ProductVariant[] = [];

  product = {
    name: '',
    brand: 'Apple',
    category: 'Điện thoại',
    status: 'Đang kinh doanh',
    shortDescription: '',
    detail: '',
    slug: '',
    seoTitle: '',
    seoDescription: '',
    sku: '',
    releaseDate: '',
    warranty: 12,
    origin: 'Việt Nam',
    manufacturer: ''
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
          this.product.category = product.categoryId === 2 ? 'Phụ kiện' : (product.categoryId === 3 ? 'Máy tính bảng' : 'Điện thoại');
          this.product.status = product.isActive ? 'Đang kinh doanh' : 'Ngừng kinh doanh';
          this.product.seoTitle = `${product.name} | PhoneStore`;
          this.product.seoDescription = product.description || '';
          this.product.sku = product.variants?.[0]?.sku || '';
          this.product.manufacturer = product.name.split(' ')[0] || '';
          this.gallery = (product.images ?? []).map((img) => img.imageUrl).filter(Boolean);
          this.variants = (product.variants ?? []).map((v) => ({
            color: '#888888',
            name: v.color || '',
            sku: v.sku || '',
            price: String(v.price ?? 0),
            salePrice: String(v.salePrice ?? 0),
            cost: '0',
            stock: v.stock ?? 0,
            image: this.gallery[0] || ''
          }));
        }
      });
    }
  }

  onNameInput(): void {
    if (!this.isEditMode) {
      this.product.slug = this.toSlug(this.product.name);
    }
  }

  private toSlug(name: string): string {
    return name
      .toLowerCase()
      .normalize('NFD')
      .replace(/[̀-ͯ]/g, '')
      .replace(/đ/g, 'd')
      .replace(/[^a-z0-9\s-]/g, '')
      .trim()
      .replace(/\s+/g, '-')
      .replace(/-+/g, '-');
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
      this.router.navigate(['/auth/login']);
      return 'Phiên đăng nhập hết hạn. Đang chuyển về trang đăng nhập...';
    }

    if (error?.status === 403) {
      return 'Bạn không có quyền thực hiện thao tác này. Vui lòng kiểm tra lại tài khoản.';
    }

    if (error?.status === 0) {
      return 'Không kết nối được backend. Kiểm tra backend đang chạy và cấu hình CORS.';
    }

    if (error?.status === 409) {
      return error?.error?.message ?? 'Slug sản phẩm đã tồn tại. Vui lòng dùng slug khác.';
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
    const ts = Date.now();
    return {
      brandId: this.brandId(this.product.brand),
      categoryId: this.categoryId(this.product.category),
      name: this.product.name,
      slug: this.product.slug || this.toSlug(this.product.name),
      description: this.product.detail || this.product.shortDescription,
      basePrice: this.parsePrice(this.variants[0]?.salePrice || this.variants[0]?.price),
      thumbnail: this.gallery[0] || undefined,
      isActive: this.product.status === 'Đang kinh doanh',
      isFeatured: true,
      variants: this.variants.map((variant, i) => ({
        color: variant.name,
        storage: this.extractStorage(variant.name),
        price: this.parsePrice(variant.price),
        salePrice: this.parsePrice(variant.salePrice),
        stock: variant.stock,
        // Thêm timestamp vào SKU khi tạo mới để đảm bảo không trùng
        sku: this.isEditMode ? variant.sku : `${variant.sku || 'VAR'}-${ts}-${i}`,
        isActive: true
      })),
      images: this.gallery.map((imageUrl, index) => ({
        imageUrl,
        altText: `${this.product.name} ${index + 1}`,
        sortOrder: index
      })),
      specs: [
        ...(this.product.sku ? [{ specKey: 'SKU', specValue: this.product.sku, sortOrder: 1 }] : []),
        { specKey: 'Bảo hành', specValue: `${this.product.warranty} tháng`, sortOrder: 2 },
        { specKey: 'Xuất xứ', specValue: this.product.origin, sortOrder: 3 },
        ...(this.product.manufacturer ? [{ specKey: 'Nhà sản xuất', specValue: this.product.manufacturer, sortOrder: 4 }] : [])
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
