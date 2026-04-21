import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthUser } from '../../core/models/auth.models';
import { LoginRequest } from '../../core/models/auth.models';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  error = '';

  loginForm = this.fb.group({
    username: ['', Validators.required],
    password: ['', [Validators.required, Validators.minLength(8)]]
  });

  constructor(private readonly fb: FormBuilder, private readonly authService: AuthService) {}

  onSubmit(): void {
    this.error = '';
    if (this.loginForm.invalid) {
      this.error = 'Password must be at least 8 characters.';
      return;
    }

    const payload: LoginRequest = {
      username: this.loginForm.value.username ?? '',
      password: this.loginForm.value.password ?? ''
    };

    this.authService.login(payload).subscribe({
      next: (user: AuthUser) => {
        this.error = `Logged in as ${user.username} (${user.role})`;
      },
      error: () => {
        this.error = 'Login failed. Check credentials.';
      }
    });
  }
}
