import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { IsAuthenticatedUseCase } from './application/is-authenticated.usecase';

export const authGuard: CanActivateFn = () => {
  const isAuthenticatedUseCase = inject(IsAuthenticatedUseCase);
  const router = inject(Router);

  if (isAuthenticatedUseCase.execute()) {
    return true;
  }

  return router.createUrlTree(['/auth-select']);
};
