import { CommonModule } from '@angular/common';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthUser, LoginRequest } from '../../core/models/auth.models';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  error = '';
  submitting = false;
  backendStatus = 'CHECKING';
  backendService = 'Loading...';
  traceId = 'N/A';

  loginForm = this.fb.group({
    username: ['', Validators.required],
    password: ['', [Validators.required, Validators.minLength(8)]]
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly authService: AuthService,
    private readonly router: Router,
    private readonly http: HttpClient
  ) {}

  ngOnInit(): void {
    this.refreshBackendStatus();
  }

  onSubmit(): void {
    this.error = '';
    this.submitting = true;
    if (this.loginForm.invalid) {
      this.error = 'Password must be at least 8 characters.';
      this.submitting = false;
      return;
    }

    const payload: LoginRequest = {
      username: this.loginForm.value.username ?? '',
      password: this.loginForm.value.password ?? ''
    };

    this.authService.login(payload).subscribe({
      next: (user: AuthUser) => {
        this.error = '';
        this.submitting = false;
        switch (user.role) {
          case 'PASSENGER':
            this.router.navigate(['/passenger']);
            break;
          case 'DISPATCHER':
            this.router.navigate(['/dispatcher']);
            break;
          case 'ADMIN':
            this.router.navigate(['/admin']);
            break;
          default:
            this.router.navigate(['/login']);
            break;
        }
      },
      error: () => {
        this.error = 'Login failed. Check credentials.';
        this.submitting = false;
      }
    });
  }

  refreshBackendStatus(): void {
    this.http.get<{ status: string; service: string }>('/api/health', { observe: 'response' }).subscribe({
      next: (response: HttpResponse<{ status: string; service: string }>) => {
        this.backendStatus = response.body?.status ?? 'UNKNOWN';
        this.backendService = response.body?.service ?? 'Unknown service';
        this.traceId = response.headers.get('X-Trace-ID') ?? 'N/A';
      },
      error: () => {
        this.backendStatus = 'DOWN';
        this.backendService = 'Cannot reach backend';
        this.traceId = 'N/A';
      }
    });
  }
}
