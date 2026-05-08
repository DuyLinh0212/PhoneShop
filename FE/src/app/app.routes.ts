import { Routes } from '@angular/router';

import { adminGuard } from './core/guards/admin.guard';
import { authGuard } from './core/guards/auth.guard';
import { guestGuard } from './core/guards/guest.guard';
import { userLayoutGuard } from './core/guards/user-layout.guard';
import { AdminBrandPageComponent } from './features/admin/pages/admin-brand-page/admin-brand-page.component';
import { AdminCategoryPageComponent } from './features/admin/pages/admin-category-page/admin-category-page.component';
import { AdminProductPageComponent } from './features/admin/pages/admin-product-page/admin-product-page.component';
import { LoginPageComponent } from './features/auth/pages/login-page/login-page.component';
import { RegisterPageComponent } from './features/auth/pages/register-page/register-page.component';
import { ProductListPageComponent } from './features/product/pages/product-list-page/product-list-page.component';
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
      { path: 'products', component: AdminProductPageComponent },
      { path: 'categories', component: AdminCategoryPageComponent },
      { path: 'brands', component: AdminBrandPageComponent },
      { path: '', pathMatch: 'full', redirectTo: 'products' }
    ]
  },
  {
    path: '',
    component: UserLayoutComponent,
    canActivate: [authGuard, userLayoutGuard],
    children: [
      { path: 'products', component: ProductListPageComponent, data: { viewMode: 'user' } },
      { path: '', pathMatch: 'full', redirectTo: 'products' }
    ]
  },
  { path: '**', redirectTo: '' }
];
