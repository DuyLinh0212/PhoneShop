import { Routes } from '@angular/router';

import { adminGuard } from './core/guards/admin.guard';
import { authGuard } from './core/guards/auth.guard';
import { guestGuard } from './core/guards/guest.guard';
import { userLayoutGuard } from './core/guards/user-layout.guard';
import { LoginPageComponent } from './features/auth/pages/login-page/login-page.component';
import { RegisterPageComponent } from './features/auth/pages/register-page/register-page.component';
import { CartPageComponent } from './features/cart/pages/cart-page/cart-page.component';
import { AdminDashboardComponent } from './features/admin/pages/admin-dashboard/admin-dashboard.component';
import { AdminOrdersPageComponent } from './features/admin/pages/admin-orders-page/admin-orders-page.component';
import { ProductFormPageComponent } from './features/admin/pages/product-form-page/product-form-page.component';
import { ProductDetailPageComponent } from './features/product/pages/product-detail-page/product-detail-page.component';
import { ProductListPageComponent } from './features/product/pages/product-list-page/product-list-page.component';
import { ProfilePageComponent } from './features/profile/pages/profile-page/profile-page.component';
import { AdminLayoutComponent } from './layouts/admin-layout/admin-layout.component';
import { UserLayoutComponent } from './layouts/user-layout/user-layout.component';

export const routes: Routes = [
  {
    path: 'auth',
    canActivate: [guestGuard],
    children: [
      { path: 'login', component: LoginPageComponent },
      { path: 'register', component: RegisterPageComponent },
      { path: '', pathMatch: 'full', redirectTo: 'login' }
    ]
  },
  {
    path: 'admin',
    component: AdminLayoutComponent,
    canActivate: [authGuard, adminGuard],
    children: [
      { path: 'dashboard', component: AdminDashboardComponent },
      { path: 'products/new', component: ProductFormPageComponent },
      { path: 'products/:id/edit', component: ProductFormPageComponent },
      { path: 'products', component: ProductListPageComponent, data: { viewMode: 'admin' } },
      { path: 'orders', component: AdminOrdersPageComponent },
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' }
    ]
  },
  {
    path: '',
    component: UserLayoutComponent,
    canActivate: [authGuard, userLayoutGuard],
    children: [
      { path: 'products/:id', component: ProductDetailPageComponent },
      { path: 'products', component: ProductListPageComponent, data: { viewMode: 'user' } },
      { path: 'cart', component: CartPageComponent },
      { path: 'profile', component: ProfilePageComponent },
      { path: '', pathMatch: 'full', redirectTo: 'products' }
    ]
  },
  { path: '**', redirectTo: '' }
];
