import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { AuthService } from '../../core/services/auth.service';
import { CartService } from '../../core/services/cart.service';

@Component({
  selector: 'app-user-layout',
  imports: [FormsModule, RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './user-layout.component.html',
  styleUrl: './user-layout.component.css'
})
export class UserLayoutComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly cartService = inject(CartService);
  private readonly router = inject(Router);

  readonly user = this.authService.currentUser;
  readonly cart = this.cartService.cart;
  searchTerm = '';
  selectedFilter = 'all';

  ngOnInit(): void {
    this.cartService.loadCart().subscribe({ error: () => {} });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/auth/login']);
  }

  searchProducts(): void {
    this.router.navigate(['/products'], {
      queryParams: {
        q: this.searchTerm.trim() || null,
        filter: this.selectedFilter === 'all' ? null : this.selectedFilter
      },
      queryParamsHandling: 'merge'
    });
  }

  applyHeaderFilter(): void {
    this.searchProducts();
  }
}
