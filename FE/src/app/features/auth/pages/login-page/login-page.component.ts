import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { ADMIN_ROLE_ID } from '../../../../core/constants/api.constants';
import { AuthService } from '../../../../core/services/auth.service';
import { extractApiErrorMessage } from '../../../../core/utils/http-error.util';

@Component({
  selector: 'app-login-page',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login-page.component.html',
  styleUrl: './login-page.component.css'
})
export class LoginPageComponent {
  private readonly formBuilder = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  loading = false;
  errorMessage = '';

  readonly loginForm = this.formBuilder.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]]
  });

  onSubmit(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      this.errorMessage = 'Vui long nhap dung email va mat khau.';
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.authService
      .login(this.loginForm.getRawValue())
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (session) => {
          const redirectUrl =
            session.user.roleId === ADMIN_ROLE_ID ? '/admin/products' : '/products';
          this.router.navigateByUrl(redirectUrl);
        },
        error: (error) => {
          this.errorMessage = extractApiErrorMessage(
            error,
            'Dang nhap that bai. Kiem tra email hoac mat khau.'
          );
        }
      });
  }
}
