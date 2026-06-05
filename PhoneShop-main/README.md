# 📱 PhoneShop - Premium Smartphone E-Commerce

[![Spring Boot](https://img.shields.io/badge/Spring_Boot-F2F4F9?style=for-the-badge&logo=spring-boot)](https://spring.io/projects/spring-boot)
[![Angular](https://img.shields.io/badge/Angular-DD0031?style=for-the-badge&logo=angular&logoColor=white)](https://angular.io/)
[![SQL Server](https://img.shields.io/badge/SQL_Server-CC2927?style=for-the-badge&logo=microsoft-sql-server&logoColor=white)](https://www.microsoft.com/en-us/sql-server)


## 📖 Giới thiệu dự án
**PhoneShop** là hệ thống website kinh doanh điện thoại di động trực tuyến được phát triển theo mô hình Client-Server (RESTful API). Hệ thống cung cấp không gian mua sắm chuẩn luxury cho khách hàng với các tính năng tìm kiếm, đặt hàng, thanh toán và theo dõi vận chuyển. Đồng thời cung cấp công cụ quản trị toàn diện cho Admin và nhân viên xử lý đơn hàng.

Dự án thuộc Đồ án môn học Công Nghệ Java - Trường Đại học Công Thương TP.HCM (HUIT).

## 👨‍💻 Đội ngũ phát triển (Nhóm 3)
* **Nguyễn Duy Linh**
* **Lê Hoàng Bảo Long**
* **Kiều Tấn Phát**

## 🛠️ Công nghệ & Kiến trúc
* **Frontend (`/FE`):** Angular, CSS/Bootstrap (Giao diện Responsive, tương thích Mobile/PC).
* **Backend (`/BE`):** Java 17, Spring Boot, Spring Data JPA, RESTful API.
* **Database:** SQL Server / MySQL (Thiết kế chuẩn hóa với 24 bảng dữ liệu).
* **Kiến trúc:** Model-View-Controller (MVC), tách biệt FE/BE.

## ✨ Tính năng nổi bật

### 🛒 Dành cho Khách hàng
* **Mua sắm:** Tìm kiếm, lọc sản phẩm (theo hãng, giá, dung lượng), xem chi tiết cấu hình.
* **Giỏ hàng & Đơn hàng:** Quản lý giỏ hàng, đặt hàng, tùy chọn phương thức thanh toán (COD, Online).
* **1 Số chức năng khác:** Theo dõi trạng thái đơn hàng realtime, đánh giá & nhận xét sản phẩm, yêu cầu đổi trả.

### 💼 Dành cho Nhân viên & Admin
* **Quản lý Kho & Sản phẩm:** Thêm/sửa/xóa sản phẩm, quản lý các biến thể (Màu sắc, RAM, ROM), theo dõi tồn kho.
* **Xử lý Đơn hàng:** Xác nhận đơn, cập nhật trạng thái đóng gói và giao hàng.
* **Khuyến mãi:** Tạo mã giảm giá (Coupons), thiết lập các chiến dịch Flash Sale theo khung giờ.
* **Thống kê:** Báo cáo doanh thu, thống kê lượt xem và sản phẩm bán chạy.

## 📂 Cấu trúc Repository (Monorepo)
```text
PhoneShop/
├── BE/               # Backend Source Code (Spring Boot)
│   ├── src/main/java # Controller, Service, Repository, Entity
│   └── pom.xml       # Maven dependencies
├── FE/               # Frontend Source Code (Angular)
│   ├── src/app/      # Components, Services, Layouts
│   └── package.json  # NPM dependencies
└── README.md
