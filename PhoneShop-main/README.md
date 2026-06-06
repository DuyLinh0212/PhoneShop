# PhoneShop

PhoneShop la ung dung ban dien thoai gom backend Spring Boot, frontend Angular va SQL Server.

## Cau hinh database

- Server: `KIEUPHAT`
- Port: `1433`
- Database: `PhoneShopDB`
- Tai khoan SQL Server: `sa`
- Mat khau SQL Server: `123`
- File cau hinh: `BE/src/main/resources/application.properties`

Backend dang bat:

```properties
spring.jpa.hibernate.ddl-auto=update
app.bootstrap.enabled=true
```

Khi backend khoi dong, Hibernate tu cap nhat cac bang/cot cua entity neu database thieu. Sau do `DatabaseBootstrap` tu kiem tra va chen du lieu mau neu cac chuc nang chua co du lieu.

## Thay doi database tu dong

`DatabaseBootstrap` thuc hien cac viec sau:

- Tao bang nghiep vu phu neu thieu: `promotions`, `posts`, `banners`, `store_settings`, `activity_logs`.
- Seed role: `admin`, `user`.
- Seed tai khoan ung dung:
  - Admin: `admin@phonestore.vn` / `123456`
  - User: `user@phonestore.vn` / `123456`
- Seed du lieu hien thi: danh muc, thuong hieu, san pham, bien the, hinh anh, thong so ky thuat, danh gia, don hang mau.
- Seed du lieu quan tri: khuyen mai, bai viet, banner, cau hinh cua hang, nhat ky bootstrap.

## Chuc nang da co

### User

- Dang ky, dang nhap, refresh token, dang xuat.
- Xem danh sach san pham, loc/tim kiem/sap xep.
- Xem chi tiet san pham, bien the, hinh anh, thong so, danh gia.
- Gio hang, cap nhat so luong, xoa gio hang.
- Checkout, xem don hang, huy don hang hop le.
- Ho so nguoi dung va dia chi.

### Admin

- Dashboard thong ke doanh thu, don hang, khach hang, san pham, ton kho, danh gia.
- Quan ly san pham: them/sua/tat san pham, upload anh, quan ly bien the/anh/thong so theo form san pham.
- Quan ly don hang va cap nhat trang thai.
- Cac muc sidebar da co giao dien va API du lieu:
  - Danh muc
  - Thuong hieu
  - Bien the
  - Thong so ky thuat
  - Hinh anh
  - Khach hang
  - Khuyen mai
  - Bai viet
  - Banner
  - Binh luan
  - Thong ke
  - Cau hinh
  - Nguoi dung
  - Vai tro va phan quyen
  - Nhat ky hoat dong

## Chay backend

```powershell
cd BE
.\mvnw.cmd spring-boot:run
```

Backend mac dinh chay tai:

```text
http://localhost:8080
```

## Chay frontend

```powershell
cd FE
npm.cmd install
npm.cmd start
```

Frontend mac dinh chay tai:

```text
http://localhost:4200
```

## Kiem test

Backend test dung H2 rieng, khong phu thuoc SQL Server:

```powershell
cd BE
.\mvnw.cmd test
```

Frontend build:

```powershell
cd FE
npm.cmd run build
```

Ket qua da kiem:

- `.\mvnw.cmd test`: PASS.
- `npm.cmd run build`: PASS, con warning budget kich thuoc bundle/CSS cua Angular.

