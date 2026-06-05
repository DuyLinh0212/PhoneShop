import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';

import { AdminStatistics, AdminStatisticsService } from '../../../../core/services/admin-statistics.service';

@Component({
  selector: 'app-admin-dashboard',
  imports: [CommonModule],
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.css'
})
export class AdminDashboardComponent implements OnInit {
  stats = [
    { icon: 'B', label: 'Tổng doanh thu', value: '3.245.690.000 đ', tone: 'blue', change: '+ 12.5% so với kỳ trước' },
    { icon: 'C', label: 'Tổng đơn hàng', value: '1.248', tone: 'green', change: '+ 8.7% so với kỳ trước' },
    { icon: 'U', label: 'Khách hàng mới', value: '856', tone: 'purple', change: '+ 15.3% so với kỳ trước' },
    { icon: 'P', label: 'Sản phẩm', value: '50', tone: 'amber', change: 'Không đổi' },
    { icon: 'O', label: 'Đơn hàng chờ xử lý', value: '32', tone: 'red', change: 'Xem chi tiết' }
  ];

  categories = [
    { name: 'iPhone', percent: '35.6%', revenue: '1.155.890.000 đ', color: '#1976f3' },
    { name: 'Samsung', percent: '28.7%', revenue: '931.450.000 đ', color: '#665cf6' },
    { name: 'Xiaomi', percent: '15.2%', revenue: '492.560.000 đ', color: '#22b573' },
    { name: 'OPPO', percent: '8.9%', revenue: '289.100.000 đ', color: '#21a77a' },
    { name: 'Vivo', percent: '5.6%', revenue: '181.720.000 đ', color: '#f04450' },
    { name: 'Khác', percent: '5.0%', revenue: '162.970.000 đ', color: '#f6a75d' }
  ];

  orders = [
    { code: '#DH10050', customer: 'Nguyễn Văn A', total: '24.990.000 đ', status: 'Chờ xác nhận', tone: 'pending', date: '31/05/2024 14:32' },
    { code: '#DH10049', customer: 'Trần Thị B', total: '15.490.000 đ', status: 'Đang xử lý', tone: 'process', date: '31/05/2024 13:15' },
    { code: '#DH10048', customer: 'Lê Văn C', total: '8.990.000 đ', status: 'Đã thanh toán', tone: 'paid', date: '31/05/2024 12:01' },
    { code: '#DH10047', customer: 'Phạm Thị D', total: '29.990.000 đ', status: 'Đã giao hàng', tone: 'ship', date: '31/05/2024 10:45' },
    { code: '#DH10046', customer: 'Hoàng Văn E', total: '11.990.000 đ', status: 'Đã hủy', tone: 'cancel', date: '30/05/2024 16:22' }
  ];

  bestSellers = [
    { name: 'iPhone 15 Pro Max 256GB', sold: 128, revenue: '3.197.120.000 đ' },
    { name: 'Samsung Galaxy S24 Ultra 256GB', sold: 98, revenue: '2.449.020.000 đ' },
    { name: 'Xiaomi 14 256GB', sold: 76, revenue: '1.275.240.000 đ' },
    { name: 'OPPO Reno11 5G 256GB', sold: 54, revenue: '538.920.000 đ' },
    { name: 'vivo V30 5G 256GB', sold: 42, revenue: '419.580.000 đ' }
  ];

  bottomStats = [
    { icon: 'U', label: 'Tổng khách hàng', value: '2.350', change: '+ 10.2%', tone: 'purple' },
    { icon: 'P', label: 'Sản phẩm hết hàng', value: '3', change: '- 25.0%', tone: 'amber down' },
    { icon: 'N', label: 'Tổng bài viết', value: '12', change: '+ 9.1%', tone: 'green' },
    { icon: 'M', label: 'Bình luận mới', value: '8', change: '+ 14.3%', tone: 'blue' },
    { icon: '*', label: 'Đánh giá sản phẩm', value: '4.8 / 5', change: '+ 0.3', tone: 'amber' }
  ];

  constructor(private readonly statisticsService: AdminStatisticsService) {}

  ngOnInit(): void {
    this.statisticsService.getStatistics().subscribe({
      next: (statistics) => this.applyStatistics(statistics),
      error: () => undefined
    });
  }

  private applyStatistics(statistics: AdminStatistics): void {
    this.stats = [
      { icon: 'B', label: 'Tổng doanh thu', value: this.formatPrice(statistics.totalRevenue), tone: 'blue', change: 'Dữ liệu từ đơn hàng' },
      { icon: 'C', label: 'Tổng đơn hàng', value: String(statistics.totalOrders), tone: 'green', change: 'Tất cả đơn hàng' },
      { icon: 'U', label: 'Tổng khách hàng', value: String(statistics.totalCustomers), tone: 'purple', change: 'Tài khoản trong hệ thống' },
      { icon: 'P', label: 'Sản phẩm', value: String(statistics.totalProducts), tone: 'amber', change: 'Đang quản lý' },
      { icon: 'O', label: 'Đơn hàng chờ xử lý', value: String(statistics.pendingOrders), tone: 'red', change: 'Cần kiểm tra' }
    ];

    this.categories = statistics.categoryRevenue.map((item, index) => ({
      name: item.name,
      percent: `${item.percent.toFixed(1)}%`,
      revenue: this.formatPrice(item.revenue),
      color: ['#1976f3', '#665cf6', '#22b573', '#21a77a', '#f04450', '#f6a75d'][index % 6]
    }));

    this.orders = statistics.recentOrders.map((order) => ({
      code: order.code,
      customer: order.customer,
      total: this.formatPrice(order.total),
      status: order.status,
      tone: this.statusTone(order.status),
      date: order.createdAt
    }));

    this.bestSellers = statistics.bestSellers.map((product) => ({
      name: product.name,
      sold: product.sold,
      revenue: this.formatPrice(product.revenue)
    }));

    this.bottomStats = [
      { icon: 'U', label: 'Tổng khách hàng', value: String(statistics.totalCustomers), change: 'Đang hoạt động', tone: 'purple' },
      { icon: 'P', label: 'Sản phẩm hết hàng', value: String(statistics.outOfStockProducts), change: 'Cần nhập kho', tone: 'amber down' },
      { icon: 'N', label: 'Tổng đánh giá', value: String(statistics.totalReviews), change: 'Đã duyệt', tone: 'green' },
      { icon: 'M', label: 'Đơn hàng mới', value: String(statistics.totalOrders), change: 'Theo DB hiện tại', tone: 'blue' },
      { icon: '*', label: 'Đánh giá sản phẩm', value: `${statistics.averageRating.toFixed(1)} / 5`, change: 'Trung bình', tone: 'amber' }
    ];
  }

  private formatPrice(price: number): string {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
      maximumFractionDigits: 0
    }).format(Number(price || 0));
  }

  private statusTone(status: string): string {
    const normalized = (status || '').toLowerCase();
    if (normalized.includes('cancel') || normalized.includes('hủy')) {
      return 'cancel';
    }
    if (normalized.includes('paid') || normalized.includes('thanh')) {
      return 'paid';
    }
    if (normalized.includes('ship') || normalized.includes('giao')) {
      return 'ship';
    }
    if (normalized.includes('process') || normalized.includes('xử')) {
      return 'process';
    }
    return 'pending';
  }
}
