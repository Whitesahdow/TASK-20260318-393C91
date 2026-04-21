export interface LoginRequest {
  username: string;
  password: string;
}

export type UserRole = 'ADMIN' | 'DISPATCHER' | 'PASSENGER';

export interface AuthUser {
  username: string;
  role: UserRole;
}

export interface RegisterRequest {
  username: string;
  password: string;
  role: UserRole;
}
