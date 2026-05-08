import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { Brand, BrandRequest } from '../../../../core/models/brand.models';
import { BrandService } from '../../../../core/services/brand.service';

@Component({
  selector: 'app-admin-brand-page',
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-brand-page.component.html',
  styleUrl: './admin-brand-page.component.css'
})
export class AdminBrandPageComponent implements OnInit {
  brands: Brand[] = [];
  loading = false;
  saving = false;
  errorMessage = '';

  showModal = false;
  editingId: number | null = null;

  form: BrandRequest = this.emptyForm();

  deleteTargetId: number | null = null;
  showConfirm = false;

  constructor(private readonly brandService: BrandService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.errorMessage = '';
    this.brandService.getAll().subscribe({
      next: (data) => { this.brands = data; this.loading = false; },
      error: (err) => { this.errorMessage = err?.error?.message ?? 'Không thể tải thương hiệu.'; this.loading = false; }
    });
  }

  openCreate(): void {
    this.editingId = null;
    this.errorMessage = '';
    this.form = this.emptyForm();
    this.showModal = true;
  }

  openEdit(brand: Brand): void {
    this.editingId = brand.id;
    this.errorMessage = '';
    this.form = { name: brand.name, slug: brand.slug, logo: brand.logo ?? '', isActive: brand.isActive };
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
    this.errorMessage = '';
  }

  save(): void {
    if (!this.form.name.trim()) { this.errorMessage = 'Tên thương hiệu không được để trống.'; return; }
    if (!this.form.slug.trim()) { this.generateSlug(); }
    this.saving = true;
    this.errorMessage = '';

    const obs = this.editingId !== null
      ? this.brandService.update(this.editingId, this.form)
      : this.brandService.create(this.form);

    obs.subscribe({
      next: () => { this.saving = false; this.showModal = false; this.load(); },
      error: (err) => {
        this.saving = false;
        this.errorMessage = err?.error?.message ?? err?.message ?? 'Lưu thất bại. Vui lòng thử lại.';
      }
    });
  }

  confirmDelete(id: number): void { this.deleteTargetId = id; this.showConfirm = true; }
  cancelDelete(): void { this.deleteTargetId = null; this.showConfirm = false; }

  doDelete(): void {
    if (this.deleteTargetId === null) return;
    this.brandService.delete(this.deleteTargetId).subscribe({
      next: () => { this.showConfirm = false; this.deleteTargetId = null; this.load(); },
      error: (err) => { this.errorMessage = err?.error?.message ?? 'Xóa thất bại.'; this.showConfirm = false; }
    });
  }

  generateSlug(): void {
    this.form.slug = this.form.name
      .toLowerCase()
      .normalize('NFD')
      .replace(/[̀-ͯ]/g, '')
      .replace(/đ/g, 'd')
      .replace(/[^a-z0-9\s-]/g, '')
      .trim()
      .replace(/\s+/g, '-');
  }

  private emptyForm(): BrandRequest {
    return { name: '', slug: '', logo: '', isActive: true };
  }
}
