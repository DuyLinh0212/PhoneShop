import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../constants/api.constants';

export interface AdminColumn {
  key: string;
  label: string;
  type: 'text' | 'textarea' | 'number' | 'boolean' | 'datetime';
  editable: boolean;
  required: boolean;
}

export interface AdminResourceSummary {
  key: string;
  title: string;
  description: string;
}

export interface AdminResourceData extends AdminResourceSummary {
  columns: AdminColumn[];
  rows: Record<string, unknown>[];
  stats: Record<string, unknown>;
}

@Injectable({
  providedIn: 'root'
})
export class AdminCatalogService {
  constructor(private readonly http: HttpClient) {}

  getResources(): Observable<AdminResourceSummary[]> {
    return this.http.get<AdminResourceSummary[]>(`${API_BASE_URL}/admin/catalog`);
  }

  getResource(resource: string): Observable<AdminResourceData> {
    return this.http.get<AdminResourceData>(`${API_BASE_URL}/admin/catalog/${resource}`);
  }

  create(resource: string, payload: Record<string, unknown>): Observable<Record<string, unknown>> {
    return this.http.post<Record<string, unknown>>(`${API_BASE_URL}/admin/catalog/${resource}`, payload);
  }

  update(resource: string, id: number, payload: Record<string, unknown>): Observable<Record<string, unknown>> {
    return this.http.put<Record<string, unknown>>(`${API_BASE_URL}/admin/catalog/${resource}/${id}`, payload);
  }

  delete(resource: string, id: number): Observable<void> {
    return this.http.delete<void>(`${API_BASE_URL}/admin/catalog/${resource}/${id}`);
  }
}
