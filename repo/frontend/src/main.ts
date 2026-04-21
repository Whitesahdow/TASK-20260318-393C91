import { bootstrapApplication } from '@angular/platform-browser';
import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { provideHttpClient, withInterceptorsFromDi, HTTP_INTERCEPTORS } from '@angular/common/http';
import { Router, RouterOutlet, provideRouter } from '@angular/router';
import { TraceInterceptor } from './app/core/interceptors/trace.interceptor';
import { appRoutes } from './app/app.routes';
import { AuthService } from './app/core/services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet],
  templateUrl: './app/app.component.html',
  styleUrls: ['./app/app.component.css']
})
class AppComponent {
  constructor(private readonly authService: AuthService, private readonly router: Router) {}

  get isAuthenticated(): boolean {
    return this.authService.currentUserValue !== null;
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}

bootstrapApplication(AppComponent, {
  providers: [
    provideRouter(appRoutes),
    provideHttpClient(withInterceptorsFromDi()),
    { provide: HTTP_INTERCEPTORS, useClass: TraceInterceptor, multi: true }
  ]
}).catch((err: unknown) => console.error(err));
