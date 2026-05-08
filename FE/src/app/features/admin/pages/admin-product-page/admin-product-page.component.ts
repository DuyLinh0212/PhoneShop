import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { Brand } from '../../../../core/models/brand.models';
import { Category } from '../../../../core/models/category.models';
import { Product, ProductRequest } from '../../../../core/models/product.models';
import { BrandService } from '../../../../core/services/brand.service';
import { CategoryService } from '../../../../core/services/category.service';
import { ProductService } from '../../../../core/services/product.service';

@Component({
  selector: 'app-admin-product-page',
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-product-page.component.html',
  styleUrl: './admin-product-page.component.css'
})
export class AdminProductPageComponent implements OnInit {
  products: Product[] = [];
  categories: Category[] = [];
  brands: Brand[] = [];

  loading = false;
  saving = false;
  errorMessage = '';

  showModal = false;
  editingId: number | null = null;

  form: ProductRequest = this.emptyForm();

  deleteTargetId: number | null = null;
  showConfirm = false;

  constructor(
    private readonly productService: ProductService,
    private readonly categoryService: CategoryService,
    private readonly brandService: BrandService
  ) {}

  ngOnInit(): void {
    this.load();
    this.categoryService.getAll().subscribe({ next: (c) => (this.categories = c) });
    this.brandService.getAll().subscribe({ next: (b) => (this.brands = b) });
  }

  load(): void {
    this.loading = true;
    this.errorMessage = '';
    this.productService.getAdminProducts().subscribe({
      next: (data) => {
        this.products = data;
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = err?.error?.message ?? 'Không thể tải danh sách sản phẩm.';
        this.loading = false;
      }
    });
  }

  openCreate(): void {
    this.editingId = null;
    this.form = this.emptyForm();
    this.showModal = true;
  }

  openEdit(product: Product): void {
    this.editingId = product.id;
    this.form = {
      brandId: product.brandId,
      categoryId: product.categoryId,
      name: product.name,
      slug: product.slug,
      description: product.description ?? '',
      basePrice: Number(product.basePrice),
      thumbnail: product.thumbnail ?? '',
      isActive: product.isActive,
      isFeatured: product.isFeatured
    };
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
  }

  save(): void {
    this.saving = true;
    const obs = this.editingId !== null
      ? this.productService.update(this.editingId, this.form)
      : this.productService.create(this.form);

    obs.subscribe({
      next: () => {
        this.saving = false;
        this.showModal = false;
        this.load();
      },
      error: (err) => {
        this.saving = false;
        this.errorMessage = err?.error?.message ?? 'Lưu thất bại.';
      }
    });
  }

  confirmDelete(id: number): void {
    this.deleteTargetId = id;
    this.showConfirm = true;
  }

  cancelDelete(): void {
    this.deleteTargetId = null;
    this.showConfirm = false;
  }

  doDelete(): void {
    if (this.deleteTargetId === null) return;
    this.productService.delete(this.deleteTargetId).subscribe({
      next: () => {
        this.showConfirm = false;
        this.deleteTargetId = null;
        this.load();
      },
      error: (err) => {
        this.errorMessage = err?.error?.message ?? 'Xóa thất bại.';
        this.showConfirm = false;
      }
    });
  }

  formatPrice(price: number | string): string {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(Number(price));
  }

  getCategoryName(id: number): string {
    return this.categories.find(c => c.id === id)?.name ?? '—';
  }

  getBrandName(id: number): string {
    return this.brands.find(b => b.id === id)?.name ?? '—';
  }

  generateSlug(): void {
    this.form = {
      ...this.form,
      slug: this.form.name
        .toLowerCase()
        .normalize('NFD').replace(/[̀-ͯ]/g, '')
        .replace(/đ/g, 'd').replace(/Đ/g, 'D')
        .replace(/[^a-z0-9\s-]/g, '')
        .trim().replace(/\s+/g, '-')
    };
  }

  private emptyForm(): ProductRequest {
    return { brandId: 0, categoryId: 0, name: '', slug: '', description: '', basePrice: 0, thumbnail: '', isActive: true, isFeatured: false };
  }
}
