import { signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { AdminCatalogPageComponent } from '../app/features/admin/pages/admin-catalog-page/admin-catalog-page.component';
import { AdminDashboardComponent } from '../app/features/admin/pages/admin-dashboard/admin-dashboard.component';
import { AdminOrdersPageComponent } from '../app/features/admin/pages/admin-orders-page/admin-orders-page.component';
import { ProductFormPageComponent } from '../app/features/admin/pages/product-form-page/product-form-page.component';
import { ProductListPageComponent } from '../app/features/product/pages/product-list-page/product-list-page.component';
import { AdminLayoutComponent } from '../app/layouts/admin-layout/admin-layout.component';
import { AdminCatalogService } from '../app/core/services/admin-catalog.service';
import { AdminStatisticsService } from '../app/core/services/admin-statistics.service';
import { AuthService } from '../app/core/services/auth.service';
import { OrderService } from '../app/core/services/order.service';
import { ProductService } from '../app/core/services/product.service';

declare global {
  interface Window {
    __adminUiJasmineReportInstalled?: boolean;
  }
}

if (!window.__adminUiJasmineReportInstalled) {
  window.__adminUiJasmineReportInstalled = true;
  const results: string[] = [];

  jasmine.getEnv().addReporter({
    specDone: (result) => {
      results.push(`${result.status.toUpperCase()} | ${result.fullName}`);
    },
    jasmineDone: () => {
      console.info('===== Báo cáo Jasmine/Karma: Admin UI tiếng Việt =====');
      results.forEach((line) => console.info(line));
      console.info(`Tổng số test: ${results.length}`);
    }
  });
}

describe('Admin UI tiếng Việt', () => {
  describe('AdminLayoutComponent', () => {
    let fixture: ComponentFixture<AdminLayoutComponent>;

    beforeEach(async () => {
      const authServiceMock = {
        currentUser: signal({
          userId: 1,
          fullName: 'Quản trị viên',
          email: 'admin@phonestore.vn',
          role: 'admin',
          roleId: 1
        }).asReadonly(),
        logout: jasmine.createSpy('logout')
      };

      await TestBed.configureTestingModule({
        imports: [AdminLayoutComponent],
        providers: [
          provideRouter([]),
          { provide: AuthService, useValue: authServiceMock }
        ]
      }).compileComponents();

      fixture = TestBed.createComponent(AdminLayoutComponent);
      fixture.detectChanges();
    });

    it('hiển thị menu quản trị bằng tiếng Việt có dấu', () => {
      const text = pageText(fixture);

      expect(text).toContain('Bảng điều khiển quản trị');
      expect(text).toContain('Tổng quan');
      expect(text).toContain('Sản phẩm');
      expect(text).toContain('Đơn hàng');
      expect(text).toContain('Vai trò & phân quyền');
      expect(text).toContain('Nhật ký hoạt động');
    });
  });

  describe('AdminDashboardComponent', () => {
    let fixture: ComponentFixture<AdminDashboardComponent>;

    beforeEach(async () => {
      const statisticsServiceMock = {
        getStatistics: jasmine.createSpy('getStatistics').and.returnValue(
          of({
            totalRevenue: 3245690000,
            totalOrders: 12,
            totalCustomers: 5,
            totalProducts: 8,
            pendingOrders: 2,
            outOfStockProducts: 1,
            totalReviews: 4,
            averageRating: 4.8,
            categoryRevenue: [{ name: 'Điện thoại', revenue: 2000000, percent: 70 }],
            recentOrders: [
              {
                id: 1,
                code: '#DH00001',
                customer: 'Nguyễn Văn A',
                total: 2000000,
                status: 'pending',
                paymentStatus: 'unpaid',
                createdAt: '06/06/2026 12:00'
              }
            ],
            bestSellers: [{ name: 'iPhone 15 Pro Max', sold: 3, revenue: 6000000 }]
          })
        )
      };

      await TestBed.configureTestingModule({
        imports: [AdminDashboardComponent],
        providers: [{ provide: AdminStatisticsService, useValue: statisticsServiceMock }]
      }).compileComponents();

      fixture = TestBed.createComponent(AdminDashboardComponent);
      fixture.detectChanges();
    });

    it('hiển thị lời chào và nhãn thống kê tiếng Việt', () => {
      const text = pageText(fixture);

      expect(text).toContain('Xin chào, quản trị viên!');
      expect(text).toContain('Doanh thu');
      expect(text).toContain('Đơn hàng mới nhất');
      expect(text).toContain('Sản phẩm bán chạy');
    });
  });

  describe('AdminCatalogPageComponent', () => {
    let fixture: ComponentFixture<AdminCatalogPageComponent>;

    beforeEach(async () => {
      const catalogServiceMock = {
        getResource: jasmine.createSpy('getResource').and.returnValue(
          of({
            key: 'categories',
            title: 'Danh mục',
            description: 'Nhóm sản phẩm hiển thị trên website',
            stats: { total: 1, active: 1, inactive: 0 },
            columns: [
              { key: 'id', label: 'ID', type: 'number', editable: false, required: false },
              { key: 'name', label: 'Tên danh mục', type: 'text', editable: true, required: true },
              { key: 'is_active', label: 'Đang hiển thị', type: 'boolean', editable: true, required: false }
            ],
            rows: [{ id: 1, name: 'Điện thoại', is_active: true }]
          })
        ),
        create: jasmine.createSpy('create'),
        update: jasmine.createSpy('update'),
        delete: jasmine.createSpy('delete')
      };

      await TestBed.configureTestingModule({
        imports: [AdminCatalogPageComponent],
        providers: [
          { provide: AdminCatalogService, useValue: catalogServiceMock },
          {
            provide: ActivatedRoute,
            useValue: {
              paramMap: of(convertToParamMap({ resource: 'categories' }))
            }
          }
        ]
      }).compileComponents();

      fixture = TestBed.createComponent(AdminCatalogPageComponent);
      fixture.detectChanges();
    });

    it('hiển thị trang catalog bằng tiếng Việt có dấu', () => {
      const text = pageText(fixture);

      expect(text).toContain('Quản trị dữ liệu');
      expect(text).toContain('Danh mục');
      expect(text).toContain('Tổng bản ghi');
      expect(text).toContain('Đang bật');
      expect(text).toContain('Dữ liệu hiện có');
      expect(text).toContain('Tên danh mục');
      expect(text).toContain('Điện thoại');
    });
  });

  describe('AdminOrdersPageComponent', () => {
    let fixture: ComponentFixture<AdminOrdersPageComponent>;

    beforeEach(async () => {
      const orderServiceMock = {
        getAdminOrders: jasmine.createSpy('getAdminOrders').and.returnValue(
          of([
            {
              id: 101,
              userId: 1,
              customerName: 'Nguyễn Văn A',
              customerEmail: 'a@example.com',
              customerPhone: '0900000001',
              shippingName: 'Nguyễn Văn A',
              shippingPhone: '0900000001',
              shippingAddress: 'Quận 1, TP. Hồ Chí Minh',
              subtotal: 1000000,
              shippingFee: 30000,
              totalAmount: 1030000,
              status: 'pending',
              paymentMethod: 'cod',
              paymentStatus: 'unpaid',
              createdAt: '2026-06-06T05:00:00Z',
              items: [
                {
                  id: 1,
                  variantId: 1,
                  productName: 'iPhone 15 Pro Max',
                  variantInfo: 'Titan / 256GB',
                  unitPrice: 1000000,
                  quantity: 1,
                  subtotal: 1000000
                }
              ]
            }
          ])
        ),
        updateAdminOrderStatus: jasmine.createSpy('updateAdminOrderStatus')
      };

      await TestBed.configureTestingModule({
        imports: [AdminOrdersPageComponent],
        providers: [{ provide: OrderService, useValue: orderServiceMock }]
      }).compileComponents();

      fixture = TestBed.createComponent(AdminOrdersPageComponent);
      fixture.detectChanges();
    });

    it('hiển thị trang đơn hàng quản trị bằng tiếng Việt có dấu', () => {
      const text = pageText(fixture);

      expect(text).toContain('Giao diện quản trị');
      expect(text).toContain('Quản lý đơn hàng');
      expect(text).toContain('Tổng đơn');
      expect(text).toContain('Chờ xác nhận');
      expect(text).toContain('Đang xử lý');
      expect(text).toContain('Thao tác');
    });
  });

  describe('ProductListPageComponent ở chế độ quản trị', () => {
    let fixture: ComponentFixture<ProductListPageComponent>;

    beforeEach(async () => {
      const productServiceMock = {
        getProducts: jasmine.createSpy('getProducts').and.returnValue(
          of([
            {
              id: 1,
              brandId: 1,
              categoryId: 1,
              name: 'iPhone 15 Pro Max',
              slug: 'iphone-15-pro-max',
              description: 'Máy mới chính hãng.',
              basePrice: 29490000,
              thumbnail: '',
              isActive: true,
              isFeatured: true,
              viewCount: 10,
              totalStock: 4
            }
          ])
        )
      };

      await TestBed.configureTestingModule({
        imports: [ProductListPageComponent],
        providers: [
          provideRouter([]),
          { provide: ProductService, useValue: productServiceMock },
          {
            provide: ActivatedRoute,
            useValue: {
              snapshot: { data: { viewMode: 'admin' } },
              queryParamMap: of(convertToParamMap({}))
            }
          }
        ]
      }).compileComponents();

      fixture = TestBed.createComponent(ProductListPageComponent);
      fixture.detectChanges();
    });

    it('hiển thị danh sách sản phẩm admin bằng tiếng Việt có dấu', () => {
      const text = pageText(fixture);

      expect(text).toContain('Giao diện quản trị');
      expect(text).toContain('Danh sách sản phẩm');
      expect(text).toContain('Thêm sản phẩm');
      expect(text).toContain('Tải lại');
      expect(text).toContain('Sắp hết');
      expect(text).toContain('Đang bán');
    });
  });

  describe('ProductFormPageComponent', () => {
    let fixture: ComponentFixture<ProductFormPageComponent>;

    beforeEach(async () => {
      const productServiceMock = {
        getBrands: jasmine.createSpy('getBrands').and.returnValue(of([{ id: 11, name: 'Apple', isActive: true }])),
        getCategories: jasmine
          .createSpy('getCategories')
          .and.returnValue(of([{ id: 21, name: 'Điện thoại', slug: 'dien-thoai', isActive: true }])),
        getProductDetail: jasmine.createSpy('getProductDetail'),
        createProduct: jasmine.createSpy('createProduct'),
        updateProduct: jasmine.createSpy('updateProduct'),
        uploadProductImage: jasmine.createSpy('uploadProductImage')
      };
      const authServiceMock = {
        currentUser: signal(null).asReadonly(),
        logout: jasmine.createSpy('logout')
      };

      await TestBed.configureTestingModule({
        imports: [ProductFormPageComponent],
        providers: [
          provideRouter([]),
          { provide: ProductService, useValue: productServiceMock },
          { provide: AuthService, useValue: authServiceMock },
          {
            provide: ActivatedRoute,
            useValue: {
              snapshot: { paramMap: convertToParamMap({}) }
            }
          }
        ]
      }).compileComponents();

      fixture = TestBed.createComponent(ProductFormPageComponent);
      fixture.detectChanges();
    });

    it('mở dialog và thêm biến thể vào bảng', () => {
      const component = fixture.componentInstance;

      component.openVariantDialog();
      component.variantDraft = {
        color: '#000000',
        name: 'Đen / 256GB',
        sku: 'IP15PM-256-BK',
        price: '29490000',
        salePrice: '',
        costPrice: '25000000',
        discountPercent: '5',
        stock: 5,
        image: ''
      };
      component.saveVariantDialog();
      fixture.detectChanges();

      expect(component.variants.length).toBe(1);
      expect(pageText(fixture)).toContain('Đen / 256GB');
      expect(pageText(fixture)).toContain('IP15PM-256-BK');
    });
  });
});

function pageText<T>(fixture: ComponentFixture<T>): string {
  return (fixture.nativeElement as HTMLElement).textContent?.replace(/\s+/g, ' ').trim() ?? '';
}
