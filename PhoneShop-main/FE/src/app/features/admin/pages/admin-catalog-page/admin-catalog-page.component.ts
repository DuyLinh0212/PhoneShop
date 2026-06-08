import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';

import {
  AdminCatalogService,
  AdminColumn,
  AdminResourceData
} from '../../../../core/services/admin-catalog.service';
import { extractApiErrorMessage } from '../../../../core/utils/http-error.util';

@Component({
  selector: 'app-admin-catalog-page',
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-catalog-page.component.html',
  styleUrl: './admin-catalog-page.component.css'
})
export class AdminCatalogPageComponent implements OnInit, OnDestroy {
  resourceKey = '';
  data: AdminResourceData | null = null;
  formValue: Record<string, unknown> = {};
  selectedRow: Record<string, unknown> | null = null;
  loading = false;
  saving = false;
  errorMessage = '';
  successMessage = '';

  private routeSubscription?: Subscription;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly adminCatalogService: AdminCatalogService
  ) {}

  ngOnInit(): void {
    this.routeSubscription = this.route.paramMap.subscribe((params) => {
      this.resourceKey = params.get('resource') ?? 'categories';
      this.resetForm();
      this.loadResource();
    });
  }

  ngOnDestroy(): void {
    this.routeSubscription?.unsubscribe();
  }

  get editableColumns(): AdminColumn[] {
    return this.data?.columns.filter((column) => column.editable) ?? [];
  }

  get visibleColumns(): AdminColumn[] {
    return this.data?.columns ?? [];
  }

  loadResource(): void {
    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.adminCatalogService.getResource(this.resourceKey).subscribe({
      next: (data) => {
        this.data = data;
        this.loading = false;
        this.ensureFormDefaults();
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage = extractApiErrorMessage(error, 'Không thể tải dữ liệu quản trị.');
      }
    });
  }

  editRow(row: Record<string, unknown>): void {
    this.selectedRow = row;
    this.formValue = {};
    this.editableColumns.forEach((column) => {
      this.formValue[column.key] = row[column.key] ?? defaultValue(column);
    });
    this.successMessage = '';
    this.errorMessage = '';
  }

  resetForm(): void {
    this.selectedRow = null;
    this.formValue = {};
    this.ensureFormDefaults();
  }

  save(): void {
    if (!this.data || this.saving) {
      return;
    }

    this.saving = true;
    this.errorMessage = '';
    this.successMessage = '';
    const payload = this.preparePayload();
    const request =
      this.selectedRow && this.selectedRow['id']
        ? this.adminCatalogService.update(this.resourceKey, Number(this.selectedRow['id']), payload)
        : this.adminCatalogService.create(this.resourceKey, payload);

    request.subscribe({
      next: () => {
        this.saving = false;
        this.successMessage = this.selectedRow ? 'Đã cập nhật dữ liệu.' : 'Đã thêm dữ liệu mới.';
        this.resetForm();
        this.loadResource();
      },
      error: (error) => {
        this.saving = false;
        this.errorMessage = extractApiErrorMessage(error, 'Không thể lưu dữ liệu.');
      }
    });
  }

  deleteRow(row: Record<string, unknown>): void {
    const id = Number(row['id']);
    if (!id || !window.confirm('Xác nhận tắt/xóa bản ghi này?')) {
      return;
    }

    this.adminCatalogService.delete(this.resourceKey, id).subscribe({
      next: () => {
        this.successMessage = 'Đã cập nhật trạng thái bản ghi.';
        this.loadResource();
      },
      error: (error) => {
        this.errorMessage = extractApiErrorMessage(error, 'Không thể xóa bản ghi.');
      }
    });
  }

  formatCell(value: unknown, column: AdminColumn): string {
    if (value === null || value === undefined || value === '') {
      return '-';
    }
    if (column.type === 'boolean') {
      return value === true || value === 1 ? 'Đang bật' : 'Đang tắt';
    }
    if (column.type === 'number') {
      return Number(value).toLocaleString('vi-VN');
    }
    return String(value);
  }

  private ensureFormDefaults(): void {
    this.editableColumns.forEach((column) => {
      if (!(column.key in this.formValue)) {
        this.formValue[column.key] = defaultValue(column);
      }
    });
  }

  private preparePayload(): Record<string, unknown> {
    return this.editableColumns.reduce<Record<string, unknown>>((payload, column) => {
      const value = this.formValue[column.key];
      if (column.type === 'number') {
        payload[column.key] = value === '' || value === null || value === undefined ? null : Number(value);
      } else if (column.type === 'boolean') {
        payload[column.key] = Boolean(value);
      } else {
        payload[column.key] = typeof value === 'string' ? value.trim() : value;
      }
      return payload;
    }, {});
  }
}

function defaultValue(column: AdminColumn): unknown {
  if (column.type === 'boolean') {
    return true;
  }
  if (column.type === 'number') {
    return null;
  }
  return '';
}
