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
      if (error.status === 401 && authService.getRefreshToken()) {
        return authService.refreshToken().pipe(
          catchError((refreshError) => {
            // Chỉ logout khi refresh token thất bại, KHÔNG logout khi retry thất bại
            authService.logout();
            return throwError(() => refreshError);
          }),
          switchMap((tokenResponse) => {
            const retryRequest = request.clone({
              setHeaders: { Authorization: `Bearer ${tokenResponse.accessToken}` }
            });
            return next(retryRequest);
          })
        );
      }

      return throwError(() => error);
    })
  );
};
