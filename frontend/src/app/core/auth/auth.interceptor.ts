import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { GetTokenUseCase } from './application/get-token.usecase';
import { tap } from 'rxjs/operators';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const getTokenUseCase = inject(GetTokenUseCase);
  const token = getTokenUseCase.execute();
  const startedAt = performance.now();
  const traceId = `http-${Date.now()}-${Math.random().toString(16).slice(2, 8)}`;

  if (!token) {
    console.groupCollapsed(`[HTTP][${traceId}] ${req.method} ${req.urlWithParams}`);
    console.info('No auth token present');
    console.info('Headers', req.headers.keys());
    console.groupEnd();
    return next(req).pipe(
      tap({
        next: (event) => {
          if ('status' in event) {
            console.groupCollapsed(`[HTTP][${traceId}] response ${req.method} ${req.urlWithParams}`);
            console.info('Status', event.status);
            console.info('ElapsedMs', Math.round(performance.now() - startedAt));
            console.groupEnd();
          }
        },
        error: (error) => {
          console.groupCollapsed(`[HTTP][${traceId}] error ${req.method} ${req.urlWithParams}`);
          console.error('Status', error?.status);
          console.error('Body', error?.error);
          console.info('ElapsedMs', Math.round(performance.now() - startedAt));
          console.groupEnd();
        }
      })
    );
  }

  const isJwt = token.split('.').length === 3;
  const scheme = isJwt ? 'Bearer' : 'Basic';
  const maskedToken = `${token.substring(0, 6)}...${token.substring(Math.max(token.length - 4, 0))}`;

  const authReq = req.clone({
    setHeaders: {
      Authorization: `${scheme} ${token}`
    }
  });

  console.groupCollapsed(`[HTTP][${traceId}] ${authReq.method} ${authReq.urlWithParams}`);
  console.info('AuthScheme', scheme);
  console.info('TokenPreview', maskedToken);
  console.info('Headers', authReq.headers.keys());
  console.groupEnd();

  return next(authReq).pipe(
    tap({
      next: (event) => {
        if ('status' in event) {
          console.groupCollapsed(`[HTTP][${traceId}] response ${authReq.method} ${authReq.urlWithParams}`);
          console.info('Status', event.status);
          console.info('ElapsedMs', Math.round(performance.now() - startedAt));
          console.groupEnd();
        }
      },
      error: (error) => {
        console.groupCollapsed(`[HTTP][${traceId}] error ${authReq.method} ${authReq.urlWithParams}`);
        console.error('Status', error?.status);
        console.error('Body', error?.error);
        console.info('ElapsedMs', Math.round(performance.now() - startedAt));
        console.groupEnd();
      }
    })
  );
};
