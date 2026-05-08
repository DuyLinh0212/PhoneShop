import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../constants/api.constants';
import { Product } from '../models/product.models';

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  constructor(private readonly http: HttpClient) {}

  getProducts(): Observable<Product[]> {
    return this.http.get<Product[]>(`${API_BASE_URL}/products`);
  }
}
