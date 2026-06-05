import { computed, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { finalize, map, Observable, shareReplay, tap } from 'rxjs';

import { API_BASE_URL, ADMIN_ROLE_ID } from '../constants/api.constants';
import {
  AuthSession,
  LoginRequest,
  LoginResponse,
  RefreshTokenRequest,
  RefreshTokenResponse,
  RegisterRequest,
  RegisterResponse
} from '../models/auth.models';
import { AppUser } from '../models/user.models';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly storageKey = 'web-ban-dt-session';
  private readonly sessionSignal = signal<AuthSession | null>(this.readStoredSession());

  readonly session = this.sessionSignal.asReadonly();
  readonly currentUser = computed(() => this.sessionSignal()?.user ?? null);
  readonly isLoggedIn = computed(() => this.sessionSignal() !== null);
  readonly isAdmin = computed(() => this.sessionSignal()?.user.roleId === ADMIN_ROLE_ID);

  private ongoingRefresh$: Observable<RefreshTokenResponse> | null = null;

  constructor(private readonly http: HttpClient) {}

  login(payload: LoginRequest): Observable<AuthSession> {
    return this.http.post<LoginResponse>(`${API_BASE_URL}/auth/login`, payload).pipe(
      map((response) => this.mapLoginResponseToSession(response)),
      tap((session) => this.persistSession(session))
    );
  }

  register(payload: RegisterRequest): Observable<RegisterResponse> {
    return this.http.post<RegisterResponse>(`${API_BASE_URL}/auth/register`, payload);
  }

  refreshToken(): Observable<RefreshTokenResponse> {
    if (this.ongoingRefresh$) {
      return this.ongoingRefresh$;
    }

    const currentRefreshToken = this.sessionSignal()?.refreshToken;
    const payload: RefreshTokenRequest = { refreshToken: currentRefreshToken ?? '' };

    this.ongoingRefresh$ = this.http.post<RefreshTokenResponse>(`${API_BASE_URL}/auth/refresh`, payload).pipe(
      tap((response) => {
        const current = this.sessionSignal();
        if (current) {
          this.persistSession({
            ...current,
            accessToken: response.accessToken,
            refreshToken: response.refreshToken,
            tokenType: response.tokenType,
            expiresIn: response.expiresIn
          });
        }
      }),
      finalize(() => {
        this.ongoingRefresh$ = null;
      }),
      shareReplay(1)
    );

    return this.ongoingRefresh$;
  }

  logout(): void {
    const refreshToken = this.sessionSignal()?.refreshToken;
    this.sessionSignal.set(null);
    localStorage.removeItem(this.storageKey);
    if (refreshToken) {
      this.http.post(`${API_BASE_URL}/auth/logout`, { refreshToken }).subscribe({ error: () => {} });
    }
  }

  getAccessToken(): string | null {
    return this.sessionSignal()?.accessToken ?? null;
  }

  getRefreshToken(): string | null {
    return this.sessionSignal()?.refreshToken ?? null;
  }

  resolveRoleId(role: string | null | undefined): number {
    return role?.trim().toLowerCase() === 'admin' ? ADMIN_ROLE_ID : 2;
  }

  getPostLoginUrl(): string {
    return this.isAdmin() ? '/admin/products' : '/products';
  }

  private persistSession(session: AuthSession): void {
    this.sessionSignal.set(session);
    localStorage.setItem(this.storageKey, JSON.stringify(session));
  }

  private mapLoginResponseToSession(response: LoginResponse): AuthSession {
    const user: AppUser = {
      userId: response.userId,
      fullName: response.fullName,
      email: response.email,
      role: response.role,
      roleId: this.resolveRoleId(response.role)
    };

    return {
      accessToken: response.accessToken,
      refreshToken: response.refreshToken,
      tokenType: response.tokenType,
      expiresIn: response.expiresIn,
      user
    };
  }

  private readStoredSession(): AuthSession | null {
    const raw = localStorage.getItem(this.storageKey);
    if (!raw) {
      return null;
    }

    try {
      const parsed = JSON.parse(raw) as AuthSession;
      if (!parsed?.accessToken || !parsed?.user?.email) {
        return null;
      }

      return {
        ...parsed,
        user: {
          ...parsed.user,
          roleId: this.resolveRoleId(parsed.user.role)
        }
      };
    } catch {
      return null;
    }
  }
}
