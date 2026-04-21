Target ID: TASK-20260318-393C91
Purpose: Separate Login, Registration, and Role-Based Dashboards into distinct pages.
1. Frontend: Navigation Infrastructure
1.1 Root Layout Refactor
File: repo/frontend/src/app/app.component.html
Action: Replace the merged console with a clean router outlet.
code
Html
<div class="main-layout">
  <!-- Top Progress Bar / Trace ID Indicator (Optional) -->
  <div class="system-status-bar">
    <small>Platform Status: Online | English Interface</small>
  </div>

  <!-- Only the active route component will render here -->
  <router-outlet></router-outlet>
</div>
1.2 Route Configuration
File: repo/frontend/src/app/app-routing.module.ts
Action: Define clear paths for Auth and each User Role.
code
TypeScript
import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/login.component';
import { RegisterComponent } from './features/auth/register.component';
import { PassengerComponent } from './features/passenger/passenger.component';
import { DispatcherComponent } from './features/dispatcher/dispatcher.component';
import { AdminComponent } from './features/admin/admin.component';
import { AuthGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { 
    path: 'passenger', 
    component: PassengerComponent, 
    canActivate: [AuthGuard], 
    data: { role: 'PASSENGER' } 
  },
  { 
    path: 'dispatcher', 
    component: DispatcherComponent, 
    canActivate: [AuthGuard], 
    data: { role: 'DISPATCHER' } 
  },
  { 
    path: 'admin', 
    component: AdminComponent, 
    canActivate: [AuthGuard], 
    data: { role: 'ADMIN' } 
  },
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: '**', redirectTo: '/login' }
];
2. Frontend: Security & Redirection
2.1 Post-Login Navigation
File: repo/frontend/src/app/features/auth/login.component.ts
Action: Update onSubmit() to redirect users based on the role returned by the Backend.
code
TypeScript
onSubmit() {
  this.authService.login(this.loginForm.value).subscribe({
    next: (user) => {
      // Expert Logic: Move to role-specific workspace immediately
      switch (user.role) {
        case 'PASSENGER': this.router.navigate(['/passenger']); break;
        case 'DISPATCHER': this.router.navigate(['/dispatcher']); break;
        case 'ADMIN': this.router.navigate(['/admin']); break;
        default: this.router.navigate(['/login']);
      }
    },
    error: (err) => this.errorMessage = "Login Failed: Check credentials."
  });
}
2.2 Auth & Role Guard
File: repo/frontend/src/app/core/guards/auth.guard.ts
Action: Prevent manual URL entry into unauthorized areas.
code
TypeScript
@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {
  constructor(private authService: AuthService, private router: Router) {}

  canActivate(route: any) {
    const user = this.authService.currentUserValue;
    if (!user) {
      this.router.navigate(['/login']);
      return false;
    }
    // Verify if user role matches the route requirement
    if (route.data.role && route.data.role !== user.role) {
      this.router.navigate(['/login']);
      return false;
    }
    return true;
  }
}
3. Environment Fix: "Backend DOWN" Correction
3.1 Proxy Fix
File: repo/frontend/proxy.conf.json
Action: Ensure the frontend in Docker uses the service name backend instead of localhost.
code
JSON
{
  "/api": {
    "target": "http://backend:8080",
    "secure": false,
    "changeOrigin": true
  }
}
3.2 Backend CORS Check
File: repo/backend/src/main/java/com/busapp/infra/config/SecurityConfig.java
Action: Permit requests from the frontend container.
code
Java
@Bean
public CorsFilter corsFilter() {
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true);
    config.addAllowedOriginPattern("*"); // Required for LAN/Docker environment
    config.addAllowedHeader("*");
    config.addAllowedMethod("*");
    source.registerCorsConfiguration("/api/**", config);
    return new CorsFilter(source);
}
4. Verification Checklist for Copilot
Angular: Run ng generate component for Passenger, Dispatcher, and Admin layouts.
Navigation: Add a "Logout" button to all dashboard components that clears localStorage and calls router.navigate(['/login']).
UI Consistency: Ensure the "English Interface" requirement is met by using only English labels across all new separate pages.
Health Check: Verify that the "BACKEND HEALTH" indicator on the login page now shows UP after applying the proxy fix.
Apply these changes to transition the project to a professional multi-page application.