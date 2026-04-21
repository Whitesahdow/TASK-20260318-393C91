import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { AuthUser, RegisterRequest, UserRole } from '../../core/models/auth.models';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent {
  error = '';
  success = '';
  submitting = false;

  readonly roles: UserRole[] = ['PASSENGER', 'DISPATCHER'];

  registerForm = this.fb.group({
    username: ['', Validators.required],
    password: ['', [Validators.required, Validators.minLength(8)]],
    role: ['PASSENGER' as UserRole, Validators.required]
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly authService: AuthService,
    private readonly router: Router
  ) {}

  onSubmit(): void {
    this.error = '';
    this.success = '';

    if (this.registerForm.invalid) {
      this.error = 'Fill all fields and use a password with at least 8 characters.';
      return;
    }

    this.submitting = true;
    const payload: RegisterRequest = {
      username: this.registerForm.value.username ?? '',
      password: this.registerForm.value.password ?? '',
      role: (this.registerForm.value.role ?? 'PASSENGER') as UserRole
    };

    this.authService.register(payload).subscribe({
      next: (user: AuthUser) => {
        this.success = 'Account created. Redirecting to your workspace.';
        this.submitting = false;
        this.router.navigateByUrl(this.authService.routeForRole(user.role));
      },
      error: () => {
        this.error = 'Registration failed. Check the input values.';
        this.submitting = false;
      }
    });
  }
}