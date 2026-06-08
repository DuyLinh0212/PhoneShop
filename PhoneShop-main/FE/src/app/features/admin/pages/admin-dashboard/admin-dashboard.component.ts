import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';

import { AdminStatistics, AdminStatisticsService } from '../../../../core/services/admin-statistics.service';

interface StatCard {
  icon: string;
  label: string;
  value: string;
  note: string;
  tone: string;
}

interface CategoryRow {
  name: string;
  percent: string;
  percentValue: number;
  revenue: string;
  color: string;
}

interface OrderRow {
  code: string;
  customer: string;
  total: string;
  status: string;
  tone: string;
  date: string;
}

interface BestSellerRow {
  name: string;
  sold: number;
  revenue: string;
}

@Component({
  selector: 'app-admin-dashboard',
  imports: [CommonModule, RouterLink],
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.css'
})
export class AdminDashboardComponent implements OnInit {
  loading = false;
  errorMessage = '';
  totalRevenueText = this.formatPrice(0);
  stats: StatCard[] = [];
  categories: CategoryRow[] = [];
  orders: OrderRow[] = [];
  bestSellers: BestSellerRow[] = [];
  bottomStats: StatCard[] = [];

  private readonly categoryColors = ['#2563eb', '#7c3aed', '#16a34a', '#f59e0b', '#ef4444', '#0ea5e9'];

  constructor(private readonly statisticsService: AdminStatisticsService) {}

  ngOnInit(): void {
    this.loadStatistics();
  }

  loadStatistics(): void {
    this.loading = true;
    this.errorMessage = '';

    this.statisticsService.getStatistics().subscribe({
      next: (statistics) => {
        this.applyStatistics(statistics);
        this.loading = false;
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage = error?.error?.message ?? 'Không thể tải dữ liệu thống kê từ hệ thống.';
        this.clearStatistics();
      }
    });
  }

  get categoryGradient(): string {
    if (this.categories.length === 0) {
      return '#eef2f7';
    }

    let cursor = 0;
    const segments = this.categories.map((item) => {
      const start = cursor;
      cursor = Math.min(100, cursor + item.percentValue);
      return `${item.color} ${start}% ${cursor}%`;
    });

    if (cursor < 100) {
      segments.push(`#e5eaf2 ${cursor}% 100%`);
    }

    return `conic-gradient(${segments.join(', ')})`;
  }

  private applyStatistics(statistics: AdminStatistics): void {
    this.totalRevenueText = this.formatPrice(statistics.totalRevenue);
    this.stats = [
      {
        icon: 'DT',
        label: 'Tổng doanh thu',
        value: this.totalRevenueText,
        note: 'Từ đơn hàng không bị hủy',
        tone: 'blue'
      },
      {
        icon: 'ĐH',
        label: 'Tổng đơn hàng',
        value: this.formatNumber(statistics.totalOrders),
        note: 'Tất cả đơn trong hệ thống',
        tone: 'green'
      },
      {
        icon: 'KH',
        label: 'Khách hàng',
        value: this.formatNumber(statistics.totalCustomers),
        note: 'Tài khoản đã ghi nhận',
        tone: 'purple'
      },
      {
        icon: 'SP',
        label: 'Sản phẩm',
        value: this.formatNumber(statistics.totalProducts),
        note: 'Sản phẩm đang quản lý',
        tone: 'amber'
      },
      {
        icon: 'CX',
        label: 'Đơn chờ xử lý',
        value: this.formatNumber(statistics.pendingOrders),
        note: 'Cần kiểm tra',
        tone: 'red'
      }
    ];

    this.categories = (statistics.categoryRevenue ?? []).map((item, index) => ({
      name: item.name,
      percent: `${Number(item.percent || 0).toFixed(1)}%`,
      percentValue: Number(item.percent || 0),
      revenue: this.formatPrice(item.revenue),
      color: this.categoryColors[index % this.categoryColors.length]
    }));

    this.orders = (statistics.recentOrders ?? []).map((order) => ({
      code: order.code,
      customer: order.customer || 'Khách hàng',
      total: this.formatPrice(order.total),
      status: this.statusLabel(order.status),
      tone: this.statusTone(order.status),
      date: order.createdAt || '-'
    }));

    this.bestSellers = (statistics.bestSellers ?? []).map((product) => ({
      name: product.name,
      sold: product.sold,
      revenue: this.formatPrice(product.revenue)
    }));

    this.bottomStats = [
      {
        icon: 'TK',
        label: 'Khách hàng',
        value: this.formatNumber(statistics.totalCustomers),
        note: 'Tổng tài khoản',
        tone: 'purple'
      },
      {
        icon: 'HH',
        label: 'Sản phẩm hết hàng',
        value: this.formatNumber(statistics.outOfStockProducts),
        note: 'Cần nhập kho',
        tone: 'amber down'
      },
      {
        icon: 'DG',
        label: 'Tổng đánh giá',
        value: this.formatNumber(statistics.totalReviews),
        note: 'Đánh giá đã duyệt',
        tone: 'green'
      },
      {
        icon: 'TB',
        label: 'Sao trung bình',
        value: `${Number(statistics.averageRating || 0).toFixed(1)} / 5`,
        note: 'Tính từ review thật',
        tone: 'amber'
      }
    ];
  }

  private clearStatistics(): void {
    this.totalRevenueText = this.formatPrice(0);
    this.stats = [];
    this.categories = [];
    this.orders = [];
    this.bestSellers = [];
    this.bottomStats = [];
  }

  private formatNumber(value: number): string {
    return new Intl.NumberFormat('vi-VN').format(Number(value || 0));
  }

  private formatPrice(price: number): string {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
      maximumFractionDigits: 0
    }).format(Number(price || 0));
  }

  private statusTone(status: string): string {
    const normalized = this.normalizeStatus(status);
    if (normalized.includes('cancel') || normalized.includes('huy')) {
      return 'cancel';
    }
    if (normalized.includes('complete') || normalized.includes('done') || normalized.includes('hoan thanh')) {
      return 'paid';
    }
    if (normalized.includes('ship') || normalized.includes('delivery') || normalized.includes('giao')) {
      return 'ship';
    }
    if (normalized.includes('confirm') || normalized.includes('process') || normalized.includes('xu ly')) {
      return 'process';
    }
    return 'pending';
  }

  private statusLabel(status: string): string {
    const normalized = this.normalizeStatus(status);
    if (normalized.includes('cancel') || normalized.includes('huy')) {
      return 'Đã hủy';
    }
    if (normalized.includes('complete') || normalized.includes('done') || normalized.includes('hoan thanh')) {
      return 'Hoàn thành';
    }
    if (normalized.includes('ship') || normalized.includes('delivery') || normalized.includes('giao')) {
      return 'Đang giao';
    }
    if (normalized.includes('confirm') || normalized.includes('process') || normalized.includes('xu ly')) {
      return 'Đang xử lý';
    }
    if (normalized.includes('pending') || normalized.includes('cho')) {
      return 'Chờ xác nhận';
    }
    return status || 'Không rõ';
  }

  private normalizeStatus(value: string): string {
    return (value || '')
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .replace(/đ/g, 'd')
      .replace(/Đ/g, 'D')
      .trim()
      .toLowerCase();
  }
}
