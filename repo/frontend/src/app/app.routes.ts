import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/login.component';
import { RegisterComponent } from './features/auth/register.component';
import { AuthGuard, HomeRedirectGuard, PublicOnlyGuard } from './core/guards/auth.guard';
import { PassengerComponent } from './features/passenger/passenger.component';
import { DispatcherComponent } from './features/dispatcher/dispatcher.component';
import { AdminComponent } from './features/admin/admin.component';

export const appRoutes: Routes = [
  { path: 'login', component: LoginComponent, canActivate: [PublicOnlyGuard] },
  { path: 'register', component: RegisterComponent, canActivate: [PublicOnlyGuard] },
  { path: 'passenger', component: PassengerComponent, canActivate: [AuthGuard], data: { role: 'PASSENGER' } },
  { path: 'dispatcher', component: DispatcherComponent, canActivate: [AuthGuard], data: { role: 'DISPATCHER' } },
  { path: 'admin', component: AdminComponent, canActivate: [AuthGuard], data: { role: 'ADMIN' } },
  { path: '', canActivate: [HomeRedirectGuard], component: LoginComponent },
  { path: '**', redirectTo: '/login' }
];