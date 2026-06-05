import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { Order, OrderService } from '../../../../core/services/order.service';
import { Profile, ProfileService } from '../../../../core/services/profile.service';

@Component({
  selector: 'app-profile-page',
  imports: [CommonModule, FormsModule],
  templateUrl: './profile-page.component.html',
  styleUrl: './profile-page.component.css'
})
export class ProfilePageComponent implements OnInit {
  profile: Profile | null = null;
  orders: Order[] = [];
  activeTab: 'info' | 'orders' | 'address' | 'security' | 'favorites' = 'info';
  orderStatusFilter = 'all';
  orderSearch = '';
  message = '';

  form = {
    fullName: '',
    phone: ''
  };

  constructor(
    private readonly profileService: ProfileService,
    private readonly orderService: OrderService
  ) {}

  ngOnInit(): void {
    this.loadProfile();
    this.loadOrders();
  }

  loadOrders(): void {
    this.orderService.getOrders().subscribe((orders) => (this.orders = orders));
  }

  loadProfile(): void {
    this.profileService.getProfile().subscribe({
      next: (profile) => {
        this.profile = profile;
        this.form.fullName = profile.fullName;
        this.form.phone = profile.phone || '';
      },
      error: () => {
        this.message = 'Không thể tải hồ sơ.';
      }
    });
  }

  saveProfile(): void {
    this.profileService.updateProfile(this.form).subscribe({
      next: (profile) => {
        this.profile = profile;
        this.message = 'Đã lưu thay đổi.';
      },
      error: (error) => {
        this.message = error?.error?.message ?? 'Không thể lưu hồ sơ.';
      }
    });
  }

  initials(): string {
    return (this.profile?.fullName || 'U').trim().charAt(0).toUpperCase();
  }

  filteredOrders(): Order[] {
    const keyword = this.orderSearch.trim().toLowerCase();
    return this.orders.filter((order) => {
      const matchesStatus = this.orderStatusFilter === 'all' || order.status === this.orderStatusFilter;
      const matchesKeyword = !keyword || `dh${order.id}`.includes(keyword) || order.items.some(
        (item) => item.productName.toLowerCase().includes(keyword)
      );
      return matchesStatus && matchesKeyword;
    });
  }

  countOrders(status?: string): number {
    return status ? this.orders.filter((order) => order.status === status).length : this.orders.length;
  }

  orderItemCount(order: Order): number {
    return order.items.reduce((sum, item) => sum + item.quantity, 0);
  }

  cancelOrder(order: Order): void {
    this.orderService.cancelOrder(order.id).subscribe({
      next: () => {
        this.message = `Đã hủy đơn #DH${order.id}.`;
        this.loadOrders();
      },
      error: (error) => {
        this.message = error?.error?.message ?? 'Không thể hủy đơn hàng.';
      }
    });
  }

  statusLabel(status: string): string {
    const labels: Record<string, string> = {
      pending: 'Chờ xác nhận',
      confirmed: 'Đang xử lý',
      processing: 'Đang xử lý',
      shipping: 'Đang giao',
      delivered: 'Hoàn thành',
      cancelled: 'Đã hủy',
      refunded: 'Hoàn tiền'
    };
    return labels[status] ?? status;
  }

  statusClass(status: string): string {
    return `status ${status}`;
  }

  formatDate(value?: string): string {
    if (!value) {
      return '';
    }
    return new Intl.DateTimeFormat('vi-VN').format(new Date(value));
  }

  formatPrice(price: number | string): string {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
      maximumFractionDigits: 0
    }).format(Number(price));
  }
}
