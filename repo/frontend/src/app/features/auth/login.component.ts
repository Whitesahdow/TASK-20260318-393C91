import { CommonModule } from '@angular/common';
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

  loginForm = this.fb.group({
    username: ['', Validators.required],
    password: ['', [Validators.required, Validators.minLength(8)]]
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly authService: AuthService,
    private readonly router: Router
  ) {}

  ngOnInit(): void {}

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

}
