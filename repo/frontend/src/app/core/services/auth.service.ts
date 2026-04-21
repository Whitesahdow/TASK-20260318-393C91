import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { AuthUser, LoginRequest, RegisterRequest, UserRole } from '../models/auth.models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly currentUserSubject = new BehaviorSubject<AuthUser | null>(this.readStoredUser());
  readonly currentUser$ = this.currentUserSubject.asObservable();

  constructor(private readonly http: HttpClient) {}

  get currentUserValue(): AuthUser | null {
    return this.currentUserSubject.value;
  }

  login(credentials: LoginRequest): Observable<AuthUser> {
    return this.http.post<AuthUser>('/api/auth/login', credentials).pipe(
      tap((user: AuthUser) => {
        this.persistUser(user);
      })
    );
  }

  register(credentials: RegisterRequest): Observable<AuthUser> {
    return this.http.post<AuthUser>('/api/auth/register', credentials).pipe(
      tap((user: AuthUser) => {
        this.persistUser(user);
      })
    );
  }

  logout(): void {
    localStorage.removeItem('user');
    this.currentUserSubject.next(null);
  }

  routeForRole(role: UserRole): string {
    switch (role) {
      case 'ADMIN':
        return '/admin';
      case 'DISPATCHER':
        return '/dispatcher';
      case 'PASSENGER':
      default:
        return '/passenger';
    }
  }

  private persistUser(user: AuthUser): void {
    localStorage.setItem('user', JSON.stringify(user));
    this.currentUserSubject.next(user);
  }

  private readStoredUser(): AuthUser | null {
    const storedUser = localStorage.getItem('user');
    if (!storedUser) {
      return null;
    }

    try {
      return JSON.parse(storedUser) as AuthUser;
    } catch {
      return null;
    }
  }
}
