import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../constants/api.constants';
import { Product, ProductRequest } from '../models/product.models';

@Injectable({ providedIn: 'root' })
export class ProductService {
  constructor(private readonly http: HttpClient) {}

  getProducts(): Observable<Product[]> {
    return this.http.get<Product[]>(`${API_BASE_URL}/products`);
  }

  getAdminProducts(): Observable<Product[]> {
    return this.http.get<Product[]>(`${API_BASE_URL}/admin/products`);
  }

  create(request: ProductRequest): Observable<Product> {
    return this.http.post<Product>(`${API_BASE_URL}/admin/products`, request);
  }

  update(id: number, request: ProductRequest): Observable<Product> {
    return this.http.put<Product>(`${API_BASE_URL}/admin/products/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${API_BASE_URL}/admin/products/${id}`);
  }
}
