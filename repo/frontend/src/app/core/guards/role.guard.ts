import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { UserRole } from '../models/auth.models';

export const roleGuard: CanActivateFn = (route) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const requiredRole = route.data['role'] as UserRole | undefined;
  const currentUser = authService.currentUserValue;

  if (!currentUser) {
    return router.parseUrl('/login');
  }

  if (requiredRole && currentUser.role !== requiredRole) {
    return router.parseUrl(authService.routeForRole(currentUser.role));
  }

  return true;
};