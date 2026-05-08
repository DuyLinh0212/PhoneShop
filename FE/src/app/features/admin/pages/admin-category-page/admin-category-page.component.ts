import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { Category, CategoryRequest } from '../../../../core/models/category.models';
import { CategoryService } from '../../../../core/services/category.service';

@Component({
  selector: 'app-admin-category-page',
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-category-page.component.html',
  styleUrl: './admin-category-page.component.css'
})
export class AdminCategoryPageComponent implements OnInit {
  categories: Category[] = [];
  loading = false;
  saving = false;
  errorMessage = '';

  showModal = false;
  editingId: number | null = null;

  form: CategoryRequest = this.emptyForm();

  deleteTargetId: number | null = null;
  showConfirm = false;

  constructor(private readonly categoryService: CategoryService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.errorMessage = '';
    this.categoryService.getAll().subscribe({
      next: (data) => { this.categories = data; this.loading = false; },
      error: (err) => { this.errorMessage = err?.error?.message ?? 'Không thể tải danh mục.'; this.loading = false; }
    });
  }

  openCreate(): void {
    this.editingId = null;
    this.errorMessage = '';
    this.form = this.emptyForm();
    this.showModal = true;
  }

  openEdit(cat: Category): void {
    this.editingId = cat.id;
    this.errorMessage = '';
    this.form = { name: cat.name, slug: cat.slug, description: cat.description ?? '', isActive: cat.isActive };
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
    this.errorMessage = '';
  }

  save(): void {
    if (!this.form.name.trim()) { this.errorMessage = 'Tên danh mục không được để trống.'; return; }
    if (!this.form.slug.trim()) { this.generateSlug(); }
    this.saving = true;
    this.errorMessage = '';

    const obs = this.editingId !== null
      ? this.categoryService.update(this.editingId, this.form)
      : this.categoryService.create(this.form);

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
    this.categoryService.delete(this.deleteTargetId).subscribe({
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

  private emptyForm(): CategoryRequest {
    return { name: '', slug: '', description: '', isActive: true };
  }
}
