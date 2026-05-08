import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { AuthService } from '../../../../core/services/auth.service';
import { extractApiErrorMessage } from '../../../../core/utils/http-error.util';

@Component({
  selector: 'app-register-page',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './register-page.component.html',
  styleUrl: './register-page.component.css'
})
export class RegisterPageComponent {
  private readonly formBuilder = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  loading = false;
  errorMessage = '';
  successMessage = '';

  readonly registerForm = this.formBuilder.nonNullable.group({
    fullName: ['', [Validators.required]],
    email: ['', [Validators.required, Validators.email]],
    phone: [''],
    password: [
      '',
      [
        Validators.required,
        Validators.minLength(8),
        Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).+$/)
      ]
    ],
    confirmPassword: ['', [Validators.required]]
  });

  onSubmit(): void {
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      this.errorMessage = 'Vui long kiem tra lai thong tin dang ky.';
      return;
    }

    const formValue = this.registerForm.getRawValue();
    if (formValue.password !== formValue.confirmPassword) {
      this.errorMessage = 'Mat khau xac nhan chua khop.';
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.authService
      .register({
        fullName: formValue.fullName,
        email: formValue.email,
        password: formValue.password,
        phone: formValue.phone || undefined
      })
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: () => {
          this.successMessage = 'Dang ky User thanh cong. Chuyen sang trang dang nhap...';
          setTimeout(() => this.router.navigate(['/auth/login']), 900);
        },
        error: (error) => {
          this.errorMessage = extractApiErrorMessage(error, 'Khong the dang ky tai khoan.');
        }
      });
  }
}
