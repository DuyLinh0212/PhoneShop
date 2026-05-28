import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../constants/api.constants';

export interface CheckoutPayload {
  shippingName: string;
  shippingPhone: string;
  shippingAddress: string;
  paymentMethod: string;
  note?: string;
}

export interface OrderItem {
  id: number;
  variantId: number;
  productName: string;
  variantInfo?: string;
  unitPrice: number;
  quantity: number;
  subtotal: number;
}

export interface Order {
  id: number;
  userId?: number;
  customerName?: string;
  customerEmail?: string;
  customerPhone?: string;
  shippingName: string;
  shippingPhone: string;
  shippingAddress: string;
  subtotal: number;
  shippingFee: number;
  totalAmount: number;
  status: string;
  paymentMethod: string;
  paymentStatus: string;
  note?: string;
  createdAt?: string;
  items: OrderItem[];
}

@Injectable({ providedIn: 'root' })
export class OrderService {
  constructor(private readonly http: HttpClient) {}

  getOrders(): Observable<Order[]> {
    return this.http.get<Order[]>(`${API_BASE_URL}/orders`);
  }

  checkout(payload: CheckoutPayload): Observable<Order> {
    return this.http.post<Order>(`${API_BASE_URL}/orders/checkout`, payload);
  }

  cancelOrder(orderId: number): Observable<Order> {
    return this.http.put<Order>(`${API_BASE_URL}/orders/${orderId}/cancel`, {});
  }

  getAdminOrders(status = 'all'): Observable<Order[]> {
    return this.http.get<Order[]>(`${API_BASE_URL}/admin/orders`, { params: { status } });
  }

  updateAdminOrderStatus(orderId: number, status: string, paymentStatus?: string, note?: string): Observable<Order> {
    return this.http.put<Order>(`${API_BASE_URL}/admin/orders/${orderId}/status`, {
      status,
      paymentStatus,
      note
    });
  }
}
