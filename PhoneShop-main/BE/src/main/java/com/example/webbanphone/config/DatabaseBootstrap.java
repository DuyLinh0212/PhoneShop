package com.example.webbanphone.config;

import com.example.webbanphone.entities.Brand;
import com.example.webbanphone.entities.Category;
import com.example.webbanphone.entities.Product;
import com.example.webbanphone.entities.ProductImage;
import com.example.webbanphone.entities.ProductSpec;
import com.example.webbanphone.entities.ProductVariant;
import com.example.webbanphone.entities.Role;
import com.example.webbanphone.entities.User;
import com.example.webbanphone.repositories.BrandRepository;
import com.example.webbanphone.repositories.CategoryRepository;
import com.example.webbanphone.repositories.ProductImageRepository;
import com.example.webbanphone.repositories.ProductRepository;
import com.example.webbanphone.repositories.ProductSpecRepository;
import com.example.webbanphone.repositories.ProductVariantRepository;
import com.example.webbanphone.repositories.RoleRepository;
import com.example.webbanphone.repositories.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
@ConditionalOnProperty(name = "app.bootstrap.enabled", havingValue = "true", matchIfMissing = true)
public class DatabaseBootstrap implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductImageRepository imageRepository;
    private final ProductSpecRepository specRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseBootstrap(
            JdbcTemplate jdbcTemplate,
            RoleRepository roleRepository,
            UserRepository userRepository,
            BrandRepository brandRepository,
            CategoryRepository categoryRepository,
            ProductRepository productRepository,
            ProductVariantRepository variantRepository,
            ProductImageRepository imageRepository,
            ProductSpecRepository specRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.brandRepository = brandRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
        this.imageRepository = imageRepository;
        this.specRepository = specRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        createOperationalTables();
        seedCoreData();
        seedOperationalTables();
    }

    private void createOperationalTables() {
        execute("""
                IF OBJECT_ID('promotions', 'U') IS NULL
                CREATE TABLE promotions (
                    id INT IDENTITY(1,1) PRIMARY KEY,
                    code NVARCHAR(50) NOT NULL UNIQUE,
                    name NVARCHAR(150) NOT NULL,
                    discount_percent DECIMAL(5,2) NOT NULL DEFAULT 0,
                    start_at DATETIME2 NULL,
                    end_at DATETIME2 NULL,
                    is_active BIT NOT NULL DEFAULT 1
                )
                """);
        execute("""
                IF OBJECT_ID('posts', 'U') IS NULL
                CREATE TABLE posts (
                    id INT IDENTITY(1,1) PRIMARY KEY,
                    title NVARCHAR(255) NOT NULL,
                    slug NVARCHAR(255) NOT NULL UNIQUE,
                    summary NVARCHAR(500) NULL,
                    content NVARCHAR(MAX) NULL,
                    is_published BIT NOT NULL DEFAULT 1,
                    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
                )
                """);
        execute("""
                IF OBJECT_ID('banners', 'U') IS NULL
                CREATE TABLE banners (
                    id INT IDENTITY(1,1) PRIMARY KEY,
                    title NVARCHAR(150) NOT NULL,
                    image_url NVARCHAR(500) NOT NULL,
                    link_url NVARCHAR(500) NULL,
                    position NVARCHAR(50) NOT NULL DEFAULT 'home',
                    sort_order INT NOT NULL DEFAULT 0,
                    is_active BIT NOT NULL DEFAULT 1
                )
                """);
        execute("""
                IF OBJECT_ID('store_settings', 'U') IS NULL
                CREATE TABLE store_settings (
                    id INT IDENTITY(1,1) PRIMARY KEY,
                    setting_key NVARCHAR(100) NOT NULL UNIQUE,
                    setting_value NVARCHAR(1000) NULL,
                    note NVARCHAR(255) NULL
                )
                """);
        execute("""
                IF OBJECT_ID('activity_logs', 'U') IS NULL
                CREATE TABLE activity_logs (
                    id INT IDENTITY(1,1) PRIMARY KEY,
                    actor NVARCHAR(150) NOT NULL,
                    action NVARCHAR(150) NOT NULL,
                    target NVARCHAR(150) NULL,
                    detail NVARCHAR(500) NULL,
                    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
                )
                """);
        execute("""
                IF OBJECT_ID('wishlists', 'U') IS NULL
                CREATE TABLE wishlists (
                    id INT IDENTITY(1,1) PRIMARY KEY,
                    user_id INT NOT NULL,
                    product_id INT NOT NULL,
                    added_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
                    CONSTRAINT uq_wishlists_user_product UNIQUE (user_id, product_id),
                    CONSTRAINT fk_wishlists_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                    CONSTRAINT fk_wishlists_product FOREIGN KEY (product_id) REFERENCES products(id)
                )
                """);
        ensureOperationalColumns();
    }

    private void ensureOperationalColumns() {
        addColumnIfMissing("promotions", "code", "NVARCHAR(50) NULL");
        addColumnIfMissing("promotions", "name", "NVARCHAR(150) NULL");
        addColumnIfMissing("promotions", "discount_percent", "DECIMAL(5,2) NOT NULL DEFAULT 0");
        addColumnIfMissing("promotions", "start_at", "DATETIME2 NULL");
        addColumnIfMissing("promotions", "end_at", "DATETIME2 NULL");
        addColumnIfMissing("promotions", "is_active", "BIT NOT NULL DEFAULT 1");

        addColumnIfMissing("posts", "title", "NVARCHAR(255) NULL");
        addColumnIfMissing("posts", "slug", "NVARCHAR(255) NULL");
        addColumnIfMissing("posts", "author_id", "INT NULL");
        addColumnIfMissing("posts", "thumbnail", "NVARCHAR(500) NULL");
        addColumnIfMissing("posts", "summary", "NVARCHAR(500) NULL");
        addColumnIfMissing("posts", "content", "NVARCHAR(MAX) NULL");
        addColumnIfMissing("posts", "status", "NVARCHAR(30) NOT NULL DEFAULT 'published'");
        addColumnIfMissing("posts", "is_published", "BIT NOT NULL DEFAULT 1");
        addColumnIfMissing("posts", "published_at", "DATETIME2 NULL");
        addColumnIfMissing("posts", "created_at", "DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()");
        addColumnIfMissing("posts", "updated_at", "DATETIME2 NULL");

        addColumnIfMissing("banners", "title", "NVARCHAR(150) NULL");
        addColumnIfMissing("banners", "image_url", "NVARCHAR(500) NULL");
        addColumnIfMissing("banners", "link_url", "NVARCHAR(500) NULL");
        addColumnIfMissing("banners", "position", "NVARCHAR(50) NOT NULL DEFAULT 'home'");
        addColumnIfMissing("banners", "sort_order", "INT NOT NULL DEFAULT 0");
        addColumnIfMissing("banners", "is_active", "BIT NOT NULL DEFAULT 1");

        addColumnIfMissing("store_settings", "setting_key", "NVARCHAR(100) NULL");
        addColumnIfMissing("store_settings", "setting_value", "NVARCHAR(1000) NULL");
        addColumnIfMissing("store_settings", "note", "NVARCHAR(255) NULL");

        addColumnIfMissing("activity_logs", "actor", "NVARCHAR(150) NULL");
        addColumnIfMissing("activity_logs", "action", "NVARCHAR(150) NULL");
        addColumnIfMissing("activity_logs", "target", "NVARCHAR(150) NULL");
        addColumnIfMissing("activity_logs", "detail", "NVARCHAR(500) NULL");
        addColumnIfMissing("activity_logs", "created_at", "DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()");

        addColumnIfMissing("wishlists", "user_id", "INT NULL");
        addColumnIfMissing("wishlists", "product_id", "INT NULL");
        addColumnIfMissing("wishlists", "added_at", "DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()");

        addColumnIfMissing("product_variants", "cost_price", "DECIMAL(15,2) NULL");
        addColumnIfMissing("product_variants", "discount_percent", "DECIMAL(5,2) NOT NULL DEFAULT 0");
        execute("""
                IF OBJECT_ID('product_variants', 'U') IS NOT NULL
                   AND COL_LENGTH('product_variants', 'discount_percent') IS NOT NULL
                   AND COL_LENGTH('product_variants', 'sale_price') IS NOT NULL
                UPDATE product_variants
                SET discount_percent = ROUND(((price - sale_price) * 100.0) / price, 2)
                WHERE (discount_percent IS NULL OR discount_percent = 0)
                  AND price > 0
                  AND sale_price IS NOT NULL
                  AND sale_price < price
                """);
    }

    private void addColumnIfMissing(String table, String column, String definition) {
        execute("""
                IF OBJECT_ID('%s', 'U') IS NOT NULL AND COL_LENGTH('%s', '%s') IS NULL
                ALTER TABLE %s ADD %s %s
                """.formatted(table, table, column, table, column, definition));
    }

    private void seedCoreData() {
        Role adminRole = roleRepository.findByNameIgnoreCase("admin")
                .orElseGet(() -> roleRepository.save(role("admin", "Quan tri he thong")));
        Role userRole = roleRepository.findByNameIgnoreCase("user")
                .orElseGet(() -> roleRepository.save(role("user", "Khach hang")));

        if (!userRepository.existsByEmailIgnoreCase("admin@phonestore.vn")) {
            userRepository.save(user(adminRole, "PhoneStore Admin", "admin@phonestore.vn", "0900000001"));
        }
        if (!userRepository.existsByEmailIgnoreCase("user@phonestore.vn")) {
            userRepository.save(user(userRole, "Khach hang mau", "user@phonestore.vn", "0900000002"));
        }

        if (categoryRepository.count() == 0) {
            categoryRepository.saveAll(List.of(
                    category("Dien thoai", "dien-thoai", "Smartphone chinh hang"),
                    category("Phu kien", "phu-kien", "Phu kien dien thoai"),
                    category("May tinh bang", "may-tinh-bang", "Tablet va thiet bi di dong")
            ));
        }

        if (brandRepository.count() == 0) {
            brandRepository.saveAll(List.of(
                    brand("Apple"), brand("Samsung"), brand("Xiaomi"), brand("OPPO"), brand("Vivo"), brand("Realme")
            ));
        }

        if (productRepository.count() == 0) {
            List<Brand> brands = brandRepository.findAll();
            List<Category> categories = categoryRepository.findAll();
            Integer phoneCategoryId = categories.get(0).getId();
            Product iphone = product(brands.get(0).getId(), phoneCategoryId, "iPhone 15 Pro Max", "iphone-15-pro-max",
                    "Titanium, camera 48MP, chip A17 Pro.", BigDecimal.valueOf(29490000),
                    "https://images.unsplash.com/photo-1695048133142-1a20484d2569?auto=format&fit=crop&w=720&q=80", true);
            Product galaxy = product(brands.get(1).getId(), phoneCategoryId, "Samsung Galaxy S24 Ultra", "samsung-galaxy-s24-ultra",
                    "Man hinh lon, S Pen, camera 200MP.", BigDecimal.valueOf(24990000),
                    "https://images.unsplash.com/photo-1610945265064-0e34e5519bbf?auto=format&fit=crop&w=720&q=80", true);
            Product xiaomi = product(brands.get(2).getId(), phoneCategoryId, "Xiaomi 14", "xiaomi-14",
                    "Hieu nang cao, sac nhanh, camera Leica.", BigDecimal.valueOf(16990000),
                    "https://images.unsplash.com/photo-1598327105666-5b89351aff97?auto=format&fit=crop&w=720&q=80", true);
            productRepository.saveAll(List.of(iphone, galaxy, xiaomi));
            seedProductChildren(iphone, "IP15PM-256-TI", "Titanium", BigDecimal.valueOf(29490000), 18);
            seedProductChildren(galaxy, "S24U-256-BK", "Den", BigDecimal.valueOf(24990000), 12);
            seedProductChildren(xiaomi, "MI14-256-GR", "Xanh", BigDecimal.valueOf(16990000), 20);
        }
    }

    private void seedProductChildren(Product product, String sku, String color, BigDecimal price, int stock) {
        ProductVariant variant = new ProductVariant();
        variant.setProductId(product.getId());
        variant.setColor(color);
        variant.setStorage("256GB");
        variant.setRam("8GB");
        variant.setPrice(price);
        variant.setCostPrice(price.subtract(BigDecimal.valueOf(3000000)));
        variant.setDiscountPercent(BigDecimal.valueOf(5));
        variant.setSalePrice(price.multiply(BigDecimal.valueOf(95)).divide(BigDecimal.valueOf(100)));
        variant.setStock(stock);
        variant.setSku(sku);
        variant.setIsActive(true);
        variantRepository.save(variant);

        ProductImage image = new ProductImage();
        image.setProductId(product.getId());
        image.setImageUrl(product.getThumbnail());
        image.setAltText(product.getName());
        image.setSortOrder(1);
        image.setIsMain(true);
        imageRepository.save(image);

        ProductSpec screen = new ProductSpec();
        screen.setProductId(product.getId());
        screen.setSpecKey("Man hinh");
        screen.setSpecValue("OLED 120Hz");
        screen.setSortOrder(1);
        specRepository.save(screen);

        ProductSpec camera = new ProductSpec();
        camera.setProductId(product.getId());
        camera.setSpecKey("Camera");
        camera.setSpecValue("Camera chinh 48MP tro len");
        camera.setSortOrder(2);
        specRepository.save(camera);
    }

    private void seedOperationalTables() {
        insertIfEmpty("promotions", """
                INSERT INTO promotions (code, name, discount_percent, start_at, end_at, is_active)
                VALUES ('PHONE10', N'Giam 10% cho don dien thoai', 10, SYSUTCDATETIME(), DATEADD(day, 30, SYSUTCDATETIME()), 1)
                """);
        insertIfEmpty("posts", """
                INSERT INTO posts (author_id, title, slug, thumbnail, summary, content, status, is_published, published_at)
                VALUES ((SELECT TOP 1 id FROM users ORDER BY id),
                        N'Kinh nghiem chon smartphone 2026',
                        'kinh-nghiem-chon-smartphone-2026',
                        'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=900&q=80',
                        N'Goi y nhanh de chon dien thoai phu hop nhu cau.',
                        N'Noi dung bai viet mau cho PhoneStore.',
                        'published',
                        1,
                        SYSUTCDATETIME())
                """);
        insertIfEmpty("banners", """
                INSERT INTO banners (title, image_url, link_url, position, sort_order, is_active)
                VALUES (N'PhoneStore Summer Sale',
                        'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?auto=format&fit=crop&w=1200&q=80',
                        '/products', 'home', 1, 1)
                """);
        insertIfEmpty("store_settings", """
                INSERT INTO store_settings (setting_key, setting_value, note)
                VALUES ('store_name', 'PhoneStore', N'Ten hien thi cua cua hang'),
                       ('support_phone', '0900000000', N'So hotline ho tro'),
                       ('shipping_fee', '30000', N'Phi giao hang mac dinh')
                """);
        insertIfEmpty("activity_logs", """
                INSERT INTO activity_logs (actor, action, target, detail)
                VALUES ('system', 'bootstrap', 'database', N'Tu dong tao bang va chen du lieu mau')
                """);
    }

    private Role role(String name, String description) {
        Role role = new Role();
        role.setName(name);
        role.setDescription(description);
        return role;
    }

    private User user(Role role, String fullName, String email, String phone) {
        User user = new User();
        user.setRole(role);
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPasswordHash(passwordEncoder.encode("123456"));
        user.setIsActive(true);
        return user;
    }

    private Brand brand(String name) {
        Brand brand = new Brand();
        brand.setName(name);
        brand.setIsActive(true);
        return brand;
    }

    private Category category(String name, String slug, String description) {
        Category category = new Category();
        category.setName(name);
        category.setSlug(slug);
        category.setDescription(description);
        category.setIsActive(true);
        return category;
    }

    private Product product(Integer brandId, Integer categoryId, String name, String slug, String description,
                            BigDecimal price, String thumbnail, boolean featured) {
        Product product = new Product();
        product.setBrandId(brandId);
        product.setCategoryId(categoryId);
        product.setName(name);
        product.setSlug(slug);
        product.setDescription(description);
        product.setBasePrice(price);
        product.setThumbnail(thumbnail);
        product.setIsActive(true);
        product.setIsFeatured(featured);
        product.setViewCount(0);
        return product;
    }

    private void insertIfEmpty(String table, String sql) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class);
        if (count == null || count == 0) {
            execute(sql);
        }
    }

    private void execute(String sql) {
        jdbcTemplate.execute(sql);
    }
}
