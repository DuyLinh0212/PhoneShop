import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { Order, OrderService } from '../../../../core/services/order.service';

@Component({
  selector: 'app-admin-orders-page',
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-orders-page.component.html',
  styleUrl: './admin-orders-page.component.css'
})
export class AdminOrdersPageComponent implements OnInit {
  orders: Order[] = [];
  statusFilter = 'all';
  paymentFilter = 'all';
  search = '';
  message = '';
  selectedOrder: Order | null = null;

  readonly statuses = [
    { value: 'pending', label: 'Chờ xác nhận' },
    { value: 'confirmed', label: 'Đang xử lý' },
    { value: 'processing', label: 'Đang xử lý' },
    { value: 'shipping', label: 'Đang giao' },
    { value: 'delivered', label: 'Hoàn thành' },
    { value: 'cancelled', label: 'Đã hủy' },
    { value: 'refunded', label: 'Hoàn tiền' }
  ];

  constructor(private readonly orderService: OrderService) {}

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.orderService.getAdminOrders(this.statusFilter).subscribe({
      next: (orders) => (this.orders = orders),
      error: (error) => {
        this.message = error?.error?.message ?? 'Không thể tải danh sách đơn hàng.';
      }
    });
  }

  filteredOrders(): Order[] {
    const keyword = this.search.trim().toLowerCase();
    return this.orders.filter((order) => {
      const matchesPayment = this.paymentFilter === 'all' || order.paymentStatus === this.paymentFilter;
      const haystack = [
        `dh${order.id}`,
        order.customerName,
        order.customerEmail,
        order.customerPhone,
        order.shippingPhone,
        ...order.items.map((item) => item.productName)
      ].join(' ').toLowerCase();
      return matchesPayment && (!keyword || haystack.includes(keyword));
    });
  }

  count(status?: string): number {
    return status ? this.orders.filter((order) => order.status === status).length : this.orders.length;
  }

  updateStatus(order: Order, status: string): void {
    if (status === order.status) {
      return;
    }

    this.orderService.updateAdminOrderStatus(order.id, status, order.paymentStatus, 'Quản trị viên cập nhật trạng thái đơn hàng').subscribe({
      next: () => {
        this.message = `Đã cập nhật đơn #DH${order.id}.`;
        this.loadOrders();
      },
      error: (error) => {
        this.message = error?.error?.message ?? 'Không thể cập nhật đơn hàng.';
      }
    });
  }

  markPaid(order: Order): void {
    this.orderService.updateAdminOrderStatus(order.id, order.status, 'paid').subscribe({
      next: () => this.loadOrders(),
      error: (error) => {
        this.message = error?.error?.message ?? 'Không thể cập nhật thanh toán.';
      }
    });
  }

  openDetail(order: Order): void {
    this.selectedOrder = order;
  }

  closeDetail(): void {
    this.selectedOrder = null;
  }

  nextStatusOptions(order: Order): Array<{ value: string; label: string }> {
    const allowed: Record<string, string[]> = {
      pending: ['confirmed', 'cancelled'],
      confirmed: ['processing', 'cancelled'],
      processing: ['shipping'],
      shipping: ['delivered'],
      delivered: ['refunded']
    };
    const values = [order.status, ...(allowed[order.status] ?? [])];
    return values.map((value) => ({ value, label: this.statusLabel(value) }));
  }

  itemCount(order: Order): number {
    return order.items.reduce((sum, item) => sum + item.quantity, 0);
  }

  statusLabel(status: string): string {
    return this.statuses.find((item) => item.value === status)?.label ?? status;
  }

  paymentLabel(order: Order): string {
    if (order.paymentStatus === 'paid') {
      return 'Đã thanh toán';
    }
    return order.paymentMethod === 'cod' ? 'COD' : 'Chưa thanh toán';
  }

  statusClass(status: string): string {
    return `status ${status}`;
  }

  paymentClass(order: Order): string {
    return `payment ${order.paymentStatus === 'paid' ? 'paid' : 'unpaid'}`;
  }

  formatDate(value?: string): string {
    if (!value) {
      return '';
    }
    return new Intl.DateTimeFormat('vi-VN', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    }).format(new Date(value));
  }

  formatPrice(price: number | string): string {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
      maximumFractionDigits: 0
    }).format(Number(price));
  }
}
