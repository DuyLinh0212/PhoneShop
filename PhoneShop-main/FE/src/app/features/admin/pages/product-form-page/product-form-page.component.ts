import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { AuthService } from '../../../../core/services/auth.service';

import { API_BASE_URL } from '../../../../core/constants/api.constants';
import { Brand, Category, ProductPayload, ProductService } from '../../../../core/services/product.service';

type ProductVariant = {
  color: string;
  name: string;
  sku: string;
  price: string;
  salePrice: string;
  costPrice: string;
  discountPercent: string;
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
  catalogLoading = false;
  variantDialogOpen = false;
  editingVariantIndex: number | null = null;
  variantError = '';
  errorMessage = '';
  successMessage = '';

  brands: Brand[] = [
    { id: 1, name: 'Apple' },
    { id: 2, name: 'Samsung' },
    { id: 3, name: 'Xiaomi' },
    { id: 4, name: 'OPPO' },
    { id: 5, name: 'Vivo' },
    { id: 6, name: 'Realme' }
  ];
  categories: Category[] = [
    { id: 1, name: 'Điện thoại', slug: 'dien-thoai' },
    { id: 2, name: 'Phụ kiện', slug: 'phu-kien' },
    { id: 3, name: 'Máy tính bảng', slug: 'may-tinh-bang' }
  ];

  gallery: string[] = [];

  variants: ProductVariant[] = [];
  variantDraft: ProductVariant = this.emptyVariantDraft();

  product = {
    name: '',
    brandId: 1,
    brand: 'Apple',
    categoryId: 1,
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
    this.loadCatalogOptions();

    if (this.isEditMode && Number.isFinite(this.productId)) {
      this.productService.getProductDetail(this.productId).subscribe({
        next: (product) => {
          this.product.name = product.name;
          this.product.slug = product.slug;
          this.product.shortDescription = product.description || '';
          this.product.detail = product.description || '';
          this.product.brandId = product.brandId;
          this.product.categoryId = product.categoryId;
          this.product.brand = this.brandName(product.brandId);
          this.product.category = this.categoryName(product.categoryId);
          this.product.status = product.isActive ? 'Đang kinh doanh' : 'Ngừng kinh doanh';
          this.product.seoTitle = `${product.name} | PhoneStore`;
          this.product.seoDescription = product.description || '';
          this.product.sku = product.variants?.[0]?.sku || '';
          this.product.manufacturer = product.name.split(' ')[0] || '';
          this.gallery = (product.images ?? []).map((img) => img.imageUrl).filter(Boolean);
          this.variants = (product.variants ?? []).map((v) => ({
            ...this.variantFromApi(v),
            image: this.gallery[0] || ''
          }));
        }
      });
    }
  }

  private variantFromApi(v: any): ProductVariant {
    const price = Number(v.price ?? 0);
    const discountPercent = this.percentFromApiVariant(v);
    const salePrice = this.discountedPrice(price, discountPercent);
    return {
      color: '#888888',
      name: v.color || '',
      sku: v.sku || '',
      price: String(price),
      salePrice: discountPercent > 0 ? String(salePrice) : '',
      costPrice: String(v.costPrice ?? 0),
      discountPercent: String(discountPercent),
      stock: v.stock ?? 0,
      image: ''
    };
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

  openVariantDialog(index?: number): void {
    this.variantError = '';
    if (typeof index === 'number') {
      const variant = this.variants[index];
      if (!variant) {
        return;
      }
      this.editingVariantIndex = index;
      this.variantDraft = { ...variant };
      this.variantDialogOpen = true;
      return;
    }

    this.editingVariantIndex = null;
    this.variantDraft = this.emptyVariantDraft();
    this.variantDialogOpen = true;
  }

  closeVariantDialog(): void {
    this.variantDialogOpen = false;
    this.editingVariantIndex = null;
    this.variantError = '';
  }

  saveVariantDialog(): void {
    const normalized = this.normalizeVariantDraft();
    if (!normalized) {
      return;
    }

    if (this.editingVariantIndex === null) {
      this.variants = [...this.variants, normalized];
    } else {
      this.variants = this.variants.map((variant, index) =>
        index === this.editingVariantIndex ? normalized : variant
      );
    }

    this.closeVariantDialog();
    this.persistVariantChanges();
  }

  deleteVariant(index: number): void {
    if (!window.confirm('Xóa biến thể này?')) {
      return;
    }
    this.variants = this.variants.filter((_, currentIndex) => currentIndex !== index);
    this.persistVariantChanges();
  }

  imageSrc(imageUrl: string): string {
    if (imageUrl.startsWith('/uploads/')) {
      return `${API_BASE_URL.replace('/api', '')}${imageUrl}`;
    }

    return imageUrl;
  }

  saveProduct(continueEditing: boolean): void {
    const payload = this.toPayload();
    if (!payload) {
      return;
    }

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

  private loadCatalogOptions(): void {
    this.catalogLoading = true;

    forkJoin({
      brands: this.productService.getBrands(),
      categories: this.productService.getCategories()
    }).subscribe({
      next: ({ brands, categories }) => {
        this.brands = brands.filter((brand) => brand.isActive !== false);
        this.categories = categories.filter((category) => category.isActive !== false);

        if (!this.brands.some((brand) => brand.id === this.product.brandId) && this.brands[0]) {
          this.product.brandId = this.brands[0].id;
          this.product.brand = this.brands[0].name;
        }

        if (!this.categories.some((category) => category.id === this.product.categoryId) && this.categories[0]) {
          this.product.categoryId = this.categories[0].id;
          this.product.category = this.categories[0].name;
        }
      },
      error: () => {
        this.errorMessage = 'Không thể tải danh sách thương hiệu/danh mục. Vui lòng kiểm tra backend.';
      },
      complete: () => {
        this.catalogLoading = false;
      }
    });
  }

  private persistVariantChanges(): void {
    if (!this.isEditMode || !Number.isFinite(this.productId)) {
      this.successMessage = 'Đã thêm biến thể vào form. Bấm “Lưu sản phẩm” để lưu xuống hệ thống.';
      return;
    }

    const payload = this.toPayload();
    if (!payload) {
      return;
    }

    this.saving = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.productService.updateProduct(this.productId, payload).subscribe({
      next: () => {
        this.saving = false;
        this.successMessage = 'Đã lưu biến thể vào sản phẩm.';
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

  private emptyVariantDraft(): ProductVariant {
    return {
      color: '#111827',
      name: '',
      sku: '',
      price: '',
      salePrice: '',
      costPrice: '',
      discountPercent: '0',
      stock: 0,
      image: this.gallery[0] || ''
    };
  }

  private normalizeVariantDraft(): ProductVariant | null {
    const name = this.variantDraft.name.trim();
    const price = this.parsePrice(this.variantDraft.price);
    const costPrice = this.variantDraft.costPrice === '' ? 0 : this.parsePrice(this.variantDraft.costPrice);
    const discountPercent = this.parsePercent(this.variantDraft.discountPercent);
    const salePrice = this.discountedPrice(price, discountPercent);
    const stock = Number(this.variantDraft.stock ?? 0);

    if (!name) {
      this.variantError = 'Vui lòng nhập tên biến thể.';
      return null;
    }

    if (!Number.isFinite(price) || price <= 0) {
      this.variantError = 'Giá bán phải lớn hơn 0.';
      return null;
    }

    if (!Number.isFinite(costPrice) || costPrice < 0) {
      this.variantError = 'Giá nhập không hợp lệ.';
      return null;
    }

    if (!Number.isFinite(discountPercent) || discountPercent < 0 || discountPercent > 100) {
      this.variantError = 'Khuyến mãi phải từ 0 đến 100%.';
      return null;
    }

    if (!Number.isFinite(stock) || stock < 0) {
      this.variantError = 'Tồn kho không được âm.';
      return null;
    }

    return {
      color: this.variantDraft.color || '#111827',
      name,
      sku: this.variantDraft.sku.trim(),
      price: String(price),
      salePrice: discountPercent > 0 ? String(salePrice) : '',
      costPrice: this.variantDraft.costPrice === '' ? '' : String(costPrice),
      discountPercent: String(discountPercent),
      stock,
      image: this.variantDraft.image || this.gallery[0] || ''
    };
  }

  private toPayload(): ProductPayload | null {
    const brandId = this.brandId(this.product.brandId);
    const categoryId = this.categoryId(this.product.categoryId);
    if (!brandId) {
      this.errorMessage = 'Không tìm thấy thương hiệu hợp lệ. Vui lòng tải lại trang rồi chọn lại thương hiệu.';
      return null;
    }
    if (!categoryId) {
      this.errorMessage = 'Không tìm thấy danh mục hợp lệ. Vui lòng tải lại trang rồi chọn lại danh mục.';
      return null;
    }

    const ts = Date.now();
    return {
      brandId,
      categoryId,
      name: this.product.name,
      slug: this.product.slug || this.toSlug(this.product.name),
      description: this.product.detail || this.product.shortDescription,
      basePrice: this.variants[0] ? this.variantFinalPrice(this.variants[0]) : 0,
      thumbnail: this.gallery[0] || undefined,
      isActive: this.product.status === 'Đang kinh doanh',
      isFeatured: true,
      variants: this.variants.map((variant, i) => {
        const price = this.parsePrice(variant.price);
        const discountPercent = this.parsePercent(variant.discountPercent);
        return {
          color: variant.name,
          storage: this.extractStorage(variant.name),
          price,
          salePrice: discountPercent > 0 ? this.discountedPrice(price, discountPercent) : null,
          costPrice: this.parsePrice(variant.costPrice),
          discountPercent,
          stock: variant.stock,
          // Thêm timestamp vào SKU khi tạo mới để đảm bảo không trùng
          sku: this.isEditMode ? variant.sku : `${variant.sku || 'VAR'}-${ts}-${i}`,
          isActive: true
        };
      }),
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

  private brandId(brandId: number): number | null {
    return this.brands.some((brand) => brand.id === brandId) ? brandId : null;
  }

  private brandName(brandId: number): string {
    return this.brands.find((brand) => brand.id === brandId)?.name ?? this.product.brand;
  }

  private categoryId(categoryId: number): number | null {
    return this.categories.some((category) => category.id === categoryId) ? categoryId : null;
  }

  private categoryName(categoryId: number): string {
    return this.categories.find((category) => category.id === categoryId)?.name ?? this.product.category;
  }

  private parsePrice(value: string | number | undefined): number {
    if (typeof value === 'number') {
      return value;
    }

    return Number((value || '0').replace(/\./g, ''));
  }

  parsePercent(value: string | number | undefined): number {
    if (typeof value === 'number') {
      return value;
    }

    return Number((value || '0').replace(',', '.'));
  }

  variantFinalPrice(variant: ProductVariant): number {
    const price = this.parsePrice(variant.price);
    const discountPercent = this.parsePercent(variant.discountPercent);
    return this.discountedPrice(price, discountPercent);
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

  private discountedPrice(price: number, discountPercent: number): number {
    if (!Number.isFinite(price)) {
      return 0;
    }

    if (!Number.isFinite(discountPercent) || discountPercent <= 0) {
      return price;
    }

    return Math.round(price * (100 - discountPercent) / 100);
  }

  private percentFromApiVariant(variant: { price?: number | null; salePrice?: number | null; discountPercent?: number | null }): number {
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

  private extractStorage(name: string): string {
    return name.split('/')[1]?.trim() || '256GB';
  }
}
