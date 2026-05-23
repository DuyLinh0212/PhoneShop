import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-admin-layout',
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './admin-layout.component.html',
  styleUrl: './admin-layout.component.css'
})
export class AdminLayoutComponent {
  private readonly authService = inject(AuthService);
  readonly router = inject(Router);

  readonly user = this.authService.currentUser;
  accountMenuOpen = false;

  toggleAccountMenu(): void {
    this.accountMenuOpen = !this.accountMenuOpen;
  }

  logout(): void {
    this.accountMenuOpen = false;
    this.authService.logout();
    this.router.navigate(['/auth/login']);
  }
}
