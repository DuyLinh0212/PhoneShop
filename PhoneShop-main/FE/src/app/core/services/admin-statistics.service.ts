import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../constants/api.constants';

export interface CategoryRevenue {
  name: string;
  revenue: number;
  percent: number;
}

export interface RecentOrder {
  id: number;
  code: string;
  customer: string;
  total: number;
  status: string;
  paymentStatus: string;
  createdAt: string;
}

export interface BestSeller {
  name: string;
  sold: number;
  revenue: number;
}

export interface AdminStatistics {
  totalRevenue: number;
  totalOrders: number;
  totalCustomers: number;
  totalProducts: number;
  pendingOrders: number;
  outOfStockProducts: number;
  totalReviews: number;
  averageRating: number;
  categoryRevenue: CategoryRevenue[];
  recentOrders: RecentOrder[];
  bestSellers: BestSeller[];
}

@Injectable({
  providedIn: 'root'
})
export class AdminStatisticsService {
  constructor(private readonly http: HttpClient) {}

  getStatistics(): Observable<AdminStatistics> {
    return this.http.get<AdminStatistics>(`${API_BASE_URL}/admin/statistics`);
  }
}
