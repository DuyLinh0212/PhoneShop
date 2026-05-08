import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../constants/api.constants';
import { Brand, BrandRequest } from '../models/brand.models';

@Injectable({ providedIn: 'root' })
export class BrandService {
  constructor(private readonly http: HttpClient) {}

  getAll(): Observable<Brand[]> {
    return this.http.get<Brand[]>(`${API_BASE_URL}/brands`);
  }

  create(request: BrandRequest): Observable<Brand> {
    return this.http.post<Brand>(`${API_BASE_URL}/admin/brands`, request);
  }

  update(id: number, request: BrandRequest): Observable<Brand> {
    return this.http.put<Brand>(`${API_BASE_URL}/admin/brands/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${API_BASE_URL}/admin/brands/${id}`);
  }
}
