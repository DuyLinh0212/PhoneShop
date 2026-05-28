import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

import { API_BASE_URL } from '../constants/api.constants';

export interface CartItem {
  id: number;
  productId: number;
  variantId: number;
  productName: string;
  thumbnail?: string;
  color?: string;
  storage?: string;
  ram?: string;
  sku?: string;
  unitPrice: number;
  quantity: number;
  stock: number;
  subtotal: number;
}

export interface Cart {
  items: CartItem[];
  totalQuantity: number;
  subtotal: number;
}

@Injectable({ providedIn: 'root' })
export class CartService {
  private readonly cartSignal = signal<Cart>({ items: [], totalQuantity: 0, subtotal: 0 });

  readonly cart = this.cartSignal.asReadonly();

  constructor(private readonly http: HttpClient) {}

  loadCart(): Observable<Cart> {
    return this.http.get<Cart>(`${API_BASE_URL}/cart`).pipe(tap((cart) => this.cartSignal.set(cart)));
  }

  addItem(variantId: number, quantity = 1): Observable<Cart> {
    return this.http
      .post<Cart>(`${API_BASE_URL}/cart/items`, { variantId, quantity })
      .pipe(tap((cart) => this.cartSignal.set(cart)));
  }

  updateItem(itemId: number, quantity: number): Observable<Cart> {
    return this.http
      .put<Cart>(`${API_BASE_URL}/cart/items/${itemId}`, { quantity })
      .pipe(tap((cart) => this.cartSignal.set(cart)));
  }

  removeItem(itemId: number): Observable<Cart> {
    return this.http
      .delete<Cart>(`${API_BASE_URL}/cart/items/${itemId}`)
      .pipe(tap((cart) => this.cartSignal.set(cart)));
  }

  setCart(cart: Cart): void {
    this.cartSignal.set(cart);
  }
}
