import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';

import { AuthService } from '../services/auth.service';

export const authTokenInterceptor: HttpInterceptorFn = (request, next) => {
  const isAuthEndpoint =
    request.url.includes('/api/auth/login') || request.url.includes('/api/auth/register');
  if (isAuthEndpoint) {
    return next(request);
  }

  const authService = inject(AuthService);
  const accessToken = authService.getAccessToken();

  if (!accessToken) {
    return next(request);
  }

  const authorizedRequest = request.clone({
    setHeaders: {
      Authorization: `Bearer ${accessToken}`
    }
  });

  return next(authorizedRequest);
};
