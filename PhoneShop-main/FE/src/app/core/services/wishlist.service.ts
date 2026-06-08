import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../constants/api.constants';
import { ProductDetail } from './product.service';

export interface FavoriteStatus {
  favorite: boolean;
  wishlistCount: number;
}

@Injectable({ providedIn: 'root' })
export class WishlistService {
  constructor(private readonly http: HttpClient) {}

  getFavorites(): Observable<ProductDetail[]> {
    return this.http.get<ProductDetail[]>(`${API_BASE_URL}/wishlist`);
  }

  getStatus(productId: number): Observable<FavoriteStatus> {
    return this.http.get<FavoriteStatus>(`${API_BASE_URL}/wishlist/${productId}/status`);
  }

  addFavorite(productId: number): Observable<FavoriteStatus> {
    return this.http.post<FavoriteStatus>(`${API_BASE_URL}/wishlist/${productId}`, {});
  }

  removeFavorite(productId: number): Observable<FavoriteStatus> {
    return this.http.delete<FavoriteStatus>(`${API_BASE_URL}/wishlist/${productId}`);
  }
}
