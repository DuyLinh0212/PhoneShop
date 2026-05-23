import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';

@Component({
  selector: 'app-admin-dashboard',
  imports: [CommonModule],
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.css'
})
export class AdminDashboardComponent {
  readonly stats = [
    { icon: 'B', label: 'Tổng doanh thu', value: '3.245.690.000 đ', tone: 'blue', change: '+ 12.5% so với kỳ trước' },
    { icon: 'C', label: 'Tổng đơn hàng', value: '1.248', tone: 'green', change: '+ 8.7% so với kỳ trước' },
    { icon: 'U', label: 'Khách hàng mới', value: '856', tone: 'purple', change: '+ 15.3% so với kỳ trước' },
    { icon: 'P', label: 'Sản phẩm', value: '50', tone: 'amber', change: 'Không đổi' },
    { icon: 'O', label: 'Đơn hàng chờ xử lý', value: '32', tone: 'red', change: 'Xem chi tiết' }
  ];

  readonly categories = [
    { name: 'iPhone', percent: '35.6%', revenue: '1.155.890.000 đ', color: '#1976f3' },
    { name: 'Samsung', percent: '28.7%', revenue: '931.450.000 đ', color: '#665cf6' },
    { name: 'Xiaomi', percent: '15.2%', revenue: '492.560.000 đ', color: '#22b573' },
    { name: 'OPPO', percent: '8.9%', revenue: '289.100.000 đ', color: '#21a77a' },
    { name: 'Vivo', percent: '5.6%', revenue: '181.720.000 đ', color: '#f04450' },
    { name: 'Khác', percent: '5.0%', revenue: '162.970.000 đ', color: '#f6a75d' }
  ];

  readonly orders = [
    { code: '#DH10050', customer: 'Nguyễn Văn A', total: '24.990.000 đ', status: 'Chờ xác nhận', tone: 'pending', date: '31/05/2024 14:32' },
    { code: '#DH10049', customer: 'Trần Thị B', total: '15.490.000 đ', status: 'Đang xử lý', tone: 'process', date: '31/05/2024 13:15' },
    { code: '#DH10048', customer: 'Lê Văn C', total: '8.990.000 đ', status: 'Đã thanh toán', tone: 'paid', date: '31/05/2024 12:01' },
    { code: '#DH10047', customer: 'Phạm Thị D', total: '29.990.000 đ', status: 'Đã giao hàng', tone: 'ship', date: '31/05/2024 10:45' },
    { code: '#DH10046', customer: 'Hoàng Văn E', total: '11.990.000 đ', status: 'Đã hủy', tone: 'cancel', date: '30/05/2024 16:22' }
  ];

  readonly bestSellers = [
    { name: 'iPhone 15 Pro Max 256GB', sold: 128, revenue: '3.197.120.000 đ' },
    { name: 'Samsung Galaxy S24 Ultra 256GB', sold: 98, revenue: '2.449.020.000 đ' },
    { name: 'Xiaomi 14 256GB', sold: 76, revenue: '1.275.240.000 đ' },
    { name: 'OPPO Reno11 5G 256GB', sold: 54, revenue: '538.920.000 đ' },
    { name: 'vivo V30 5G 256GB', sold: 42, revenue: '419.580.000 đ' }
  ];

  readonly bottomStats = [
    { icon: 'U', label: 'Tổng khách hàng', value: '2.350', change: '+ 10.2%', tone: 'purple' },
    { icon: 'P', label: 'Sản phẩm hết hàng', value: '3', change: '- 25.0%', tone: 'amber down' },
    { icon: 'N', label: 'Tổng bài viết', value: '12', change: '+ 9.1%', tone: 'green' },
    { icon: 'M', label: 'Bình luận mới', value: '8', change: '+ 14.3%', tone: 'blue' },
    { icon: '*', label: 'Đánh giá sản phẩm', value: '4.8 / 5', change: '+ 0.3', tone: 'amber' }
  ];
}
