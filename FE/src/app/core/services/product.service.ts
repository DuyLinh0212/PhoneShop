import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../constants/api.constants';
import { Product } from '../models/product.models';

export interface ProductVariantPayload {
  color?: string;
  storage?: string;
  ram?: string;
  price: number;
  salePrice?: number | null;
  stock?: number;
  sku?: string;
  isActive?: boolean;
}

export interface ProductImagePayload {
  imageUrl: string;
  altText?: string;
  sortOrder?: number;
}

export interface ProductSpecPayload {
  specKey: string;
  specValue: string;
  sortOrder?: number;
}

export interface ProductPayload {
  brandId: number;
  categoryId: number;
  name: string;
  slug: string;
  description?: string;
  basePrice?: number;
  thumbnail?: string;
  isActive?: boolean;
  isFeatured?: boolean;
  variants?: ProductVariantPayload[];
  images?: ProductImagePayload[];
  specs?: ProductSpecPayload[];
}

export interface ProductDetail extends Product {
  variants?: ProductVariantPayload[];
  images?: ProductImagePayload[];
  specs?: ProductSpecPayload[];
}

export interface ImageUploadResponse {
  imageUrl: string;
}

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  constructor(private readonly http: HttpClient) {}

  getProducts(): Observable<Product[]> {
    return this.http.get<Product[]>(`${API_BASE_URL}/products`);
  }

  getProductDetail(id: number): Observable<ProductDetail> {
    return this.http.get<ProductDetail>(`${API_BASE_URL}/products/${id}`);
  }

  createProduct(payload: ProductPayload): Observable<ProductDetail> {
    return this.http.post<ProductDetail>(`${API_BASE_URL}/admin/products`, payload);
  }

  updateProduct(id: number, payload: ProductPayload): Observable<ProductDetail> {
    return this.http.put<ProductDetail>(`${API_BASE_URL}/admin/products/${id}`, payload);
  }

  deactivateProduct(id: number): Observable<void> {
    return this.http.delete<void>(`${API_BASE_URL}/admin/products/${id}`);
  }

  uploadProductImage(file: File): Observable<ImageUploadResponse> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<ImageUploadResponse>(`${API_BASE_URL}/admin/products/images`, formData);
  }
}
