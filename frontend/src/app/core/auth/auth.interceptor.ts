import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from './auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken();

  if (!token) {
    return next(req);
  }

  const isJwt = token.split('.').length === 3;
  const scheme = isJwt ? 'Bearer' : 'Basic';

  const authReq = req.clone({
    setHeaders: {
      Authorization: `${scheme} ${token}`
    }
  });

  return next(authReq);
};
