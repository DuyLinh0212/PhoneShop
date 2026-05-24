import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError } from 'rxjs';

import { AuthService } from '../services/auth.service';

const AUTH_ENDPOINTS = ['/api/auth/login', '/api/auth/register', '/api/auth/refresh', '/api/auth/logout'];

export const authTokenInterceptor: HttpInterceptorFn = (request, next) => {
  const isAuthEndpoint = AUTH_ENDPOINTS.some((ep) => request.url.includes(ep));
  if (isAuthEndpoint) {
    return next(request);
  }

  const authService = inject(AuthService);
  const accessToken = authService.getAccessToken();

  const authorizedRequest = accessToken
    ? request.clone({ setHeaders: { Authorization: `Bearer ${accessToken}` } })
    : request;

  return next(authorizedRequest).pipe(
    catchError((error: HttpErrorResponse) => {
      // Nếu lỗi 401 và có refresh token thì thử làm mới access token
      if (error.status === 401 && authService.getRefreshToken()) {
        return authService.refreshToken().pipe(
          switchMap((tokenResponse) => {
            const retryRequest = request.clone({
              setHeaders: { Authorization: `Bearer ${tokenResponse.accessToken}` }
            });
            return next(retryRequest);
          }),
          catchError((refreshError) => {
            // Refresh token hết hạn hoặc không hợp lệ -> đăng xuất
            authService.logout();
            return throwError(() => refreshError);
          })
        );
      }

      return throwError(() => error);
    })
  );
};
