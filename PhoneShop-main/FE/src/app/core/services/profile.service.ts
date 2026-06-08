import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../constants/api.constants';

export interface AddressSummary {
  id: number;
  fullName: string;
  phone: string;
  province: string;
  district: string;
  ward: string;
  street: string;
  isDefault: boolean;
}

export interface Profile {
  userId: number;
  fullName: string;
  email: string;
  phone?: string;
  role: string;
  orderCount: number;
  wishlistCount: number;
  rewardPoints: number;
  defaultAddress?: AddressSummary | null;
}

@Injectable({ providedIn: 'root' })
export class ProfileService {
  constructor(private readonly http: HttpClient) {}

  getProfile(): Observable<Profile> {
    return this.http.get<Profile>(`${API_BASE_URL}/profile`);
  }

  updateProfile(payload: { fullName: string; phone?: string }): Observable<Profile> {
    return this.http.put<Profile>(`${API_BASE_URL}/profile`, payload);
  }

  getAddresses(): Observable<AddressSummary[]> {
    return this.http.get<AddressSummary[]>(`${API_BASE_URL}/profile/addresses`);
  }

  saveDefaultAddress(payload: Omit<AddressSummary, 'id' | 'isDefault'>): Observable<AddressSummary> {
    return this.http.put<AddressSummary>(`${API_BASE_URL}/profile/default-address`, payload);
  }
}
