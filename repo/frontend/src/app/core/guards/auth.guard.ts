import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, UrlTree } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { UserRole } from '../models/auth.models';

@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {
  constructor(private readonly authService: AuthService, private readonly router: Router) {}

  canActivate(route: ActivatedRouteSnapshot): boolean | UrlTree {
    const user = this.authService.currentUserValue;
    if (!user) {
      return this.router.parseUrl('/login');
    }

    const requiredRole = route.data['role'] as UserRole | undefined;
    if (requiredRole && requiredRole !== user.role) {
      return this.router.parseUrl(this.authService.routeForRole(user.role));
    }

    return true;
  }
}

@Injectable({ providedIn: 'root' })
export class PublicOnlyGuard implements CanActivate {
  constructor(private readonly authService: AuthService, private readonly router: Router) {}

  canActivate(): boolean | UrlTree {
    const user = this.authService.currentUserValue;
    if (!user) {
      return true;
    }

    return this.router.parseUrl(this.authService.routeForRole(user.role));
  }
}

@Injectable({ providedIn: 'root' })
export class HomeRedirectGuard implements CanActivate {
  constructor(private readonly authService: AuthService, private readonly router: Router) {}

  canActivate(): UrlTree {
    const user = this.authService.currentUserValue;
    if (user) {
      return this.router.parseUrl(this.authService.routeForRole(user.role));
    }

    return this.router.parseUrl('/login');
  }
}