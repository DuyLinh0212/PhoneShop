import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../constants/api.constants';
import { Product } from '../models/product.models';

export interface ProductFilters {
  q?: string;
  brandId?: number | null;
  categoryId?: number | null;
  minPrice?: number | null;
  maxPrice?: number | null;
  inStock?: boolean | null;
  featured?: boolean | null;
  sort?: string;
}

export interface Brand {
  id: number;
  name: string;
  logo?: string;
  isActive?: boolean;
}

export interface Category {
  id: number;
  parentId?: number | null;
  name: string;
  slug: string;
  description?: string;
  isActive?: boolean;
}

export interface ProductVariantPayload {
  id?: number;
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

export interface Review {
  id: number;
  productId: number;
  userId: number;
  userName: string;
  rating: number;
  title?: string;
  content?: string;
  createdAt?: string;
}

export interface ReviewSummary {
  averageRating: number;
  totalReviews: number;
  reviews: Review[];
}

@Injectable({
  providedIn: 'root'
})
export class ProductService {
  constructor(private readonly http: HttpClient) {}

  getProducts(filters: ProductFilters = {}): Observable<Product[]> {
    const params = Object.entries(filters).reduce<Record<string, string>>((result, [key, value]) => {
      if (value !== null && value !== undefined && value !== '') {
        result[key] = String(value);
      }
      return result;
    }, {});
    return this.http.get<Product[]>(`${API_BASE_URL}/products`, { params });
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

  getBrands(): Observable<Brand[]> {
    return this.http.get<Brand[]>(`${API_BASE_URL}/products/brands`);
  }

  getCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(`${API_BASE_URL}/products/categories`);
  }

  getReviews(productId: number): Observable<ReviewSummary> {
    return this.http.get<ReviewSummary>(`${API_BASE_URL}/products/${productId}/reviews`);
  }

  createReview(productId: number, payload: { rating: number; title?: string; content?: string }): Observable<Review> {
    return this.http.post<Review>(`${API_BASE_URL}/products/${productId}/reviews`, payload);
  }
}
