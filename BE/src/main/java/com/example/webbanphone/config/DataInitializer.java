package com.example.webbanphone.config;

import com.example.webbanphone.entities.Brand;
import com.example.webbanphone.entities.Category;
import com.example.webbanphone.entities.Product;
import com.example.webbanphone.repositories.BrandRepository;
import com.example.webbanphone.repositories.CategoryRepository;
import com.example.webbanphone.repositories.ProductRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DataInitializer {

    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public DataInitializer(BrandRepository brandRepository,
                           CategoryRepository categoryRepository,
                           ProductRepository productRepository) {
        this.brandRepository = brandRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seed() {
        if (brandRepository.count() > 0) return;

        List<Brand> brands = brandRepository.saveAll(List.of(
                brand("Apple", "apple", "https://logo.clearbit.com/apple.com"),
                brand("Samsung", "samsung", "https://logo.clearbit.com/samsung.com"),
                brand("Xiaomi", "xiaomi", "https://logo.clearbit.com/xiaomi.com"),
                brand("OPPO", "oppo", "https://logo.clearbit.com/oppo.com"),
                brand("Vivo", "vivo", "https://logo.clearbit.com/vivo.com")
        ));

        List<Category> categories = categoryRepository.saveAll(List.of(
                category("Điện thoại thông minh", "dien-thoai-thong-minh", "Smartphone cao cấp và phổ thông"),
                category("Máy tính bảng", "may-tinh-bang", "Tablet cho học tập và giải trí"),
                category("Phụ kiện", "phu-kien", "Ốp lưng, sạc, tai nghe và các phụ kiện khác"),
                category("Đồng hồ thông minh", "dong-ho-thong-minh", "Smartwatch theo dõi sức khỏe"),
                category("Laptop", "laptop", "Máy tính xách tay")
        ));

        int appleId = brands.get(0).getId();
        int samsungId = brands.get(1).getId();
        int xiaomiId = brands.get(2).getId();
        int oppoId = brands.get(3).getId();
        int vivoId = brands.get(4).getId();

        int phoneId = categories.get(0).getId();
        int tabletId = categories.get(1).getId();
        int watchId = categories.get(3).getId();

        productRepository.saveAll(List.of(
                product(appleId, phoneId, "iPhone 15 Pro Max", "iphone-15-pro-max",
                        "Chip A17 Pro, camera 48MP, Dynamic Island, USB-C, khung Titan",
                        new BigDecimal("34990000"),
                        "https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/iphone-15-pro-max-black-titanium-select?wid=940&hei=1112&fmt=png-alpha", true, true),

                product(appleId, phoneId, "iPhone 15", "iphone-15",
                        "Chip A16 Bionic, Dynamic Island, camera 48MP chính, USB-C",
                        new BigDecimal("22990000"),
                        "https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/iphone-15-black-select-202309?wid=940&hei=1112&fmt=png-alpha", true, true),

                product(samsungId, phoneId, "Samsung Galaxy S24 Ultra", "samsung-galaxy-s24-ultra",
                        "Snapdragon 8 Gen 3, bút S Pen tích hợp, camera 200MP",
                        new BigDecimal("31990000"),
                        "https://images.samsung.com/is/image/samsung/p6pim/vn/2401/gallery/vn-galaxy-s24-ultra-s928-sm-s928bzkgxxv-thumb-539573051", true, true),

                product(samsungId, phoneId, "Samsung Galaxy A55", "samsung-galaxy-a55",
                        "Exynos 1480, màn hình Super AMOLED 6.6 inch, camera 50MP",
                        new BigDecimal("10990000"),
                        "https://images.samsung.com/is/image/samsung/p6pim/vn/sm-a556elbgxxv/gallery/vn-galaxy-a55-5g-sm-a556-sm-a556elbgxxv-thumb-541827893", true, false),

                product(xiaomiId, phoneId, "Xiaomi 14 Ultra", "xiaomi-14-ultra",
                        "Snapdragon 8 Gen 3, camera Leica 1 inch, sạc 90W",
                        new BigDecimal("27990000"),
                        "https://i02.appmifile.com/mi-com-product/fly-birds/xiaomi-14-ultra/m/product-image.png", true, true),

                product(xiaomiId, phoneId, "Redmi Note 13 Pro", "redmi-note-13-pro",
                        "Dimensity 7200 Ultra, camera 200MP, sạc nhanh 67W",
                        new BigDecimal("7490000"),
                        "https://i02.appmifile.com/mi-com-product/fly-birds/redmi-note-13-pro/m/product-image.png", true, false),

                product(oppoId, phoneId, "OPPO Reno 11 Pro", "oppo-reno-11-pro",
                        "Dimensity 8200, camera chân dung AI 50MP, sạc SUPERVOOC 80W",
                        new BigDecimal("12990000"),
                        "https://image.oppo.com/content/dam/oppo/common/mkt/v3-2/reno11-pro-5g/reno11-pro-5g-monet-purple.png", true, false),

                product(vivoId, phoneId, "Vivo V30 Pro", "vivo-v30-pro",
                        "Snapdragon 7 Gen 3, camera selfie ZEISS 50MP, sạc nhanh 80W",
                        new BigDecimal("13490000"),
                        "https://www.vivo.com/content/dam/vivo-product-new/v30-pro/v1/images/viov30pro-color-twilight-purple-pc.png", true, false),

                product(appleId, tabletId, "iPad Pro M4 13 inch", "ipad-pro-m4-13-inch",
                        "Chip M4, màn hình OLED 13 inch, mỏng nhất từ trước đến nay",
                        new BigDecimal("39990000"),
                        "https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/ipad-pro-13-select-wifi-spacegray-202405?wid=940&hei=1112&fmt=png-alpha", true, true),

                product(samsungId, tabletId, "Samsung Galaxy Tab S9+", "samsung-galaxy-tab-s9-plus",
                        "Snapdragon 8 Gen 2, màn hình AMOLED 12.4 inch, bút S Pen",
                        new BigDecimal("21990000"),
                        "https://images.samsung.com/is/image/samsung/p6pim/vn/sm-x816bzkaxsp/gallery/vn-galaxy-tab-s9-plus-sm-x816-sm-x816bzkaxsp-thumb-537603923", true, false),

                product(appleId, watchId, "Apple Watch Series 9 GPS", "apple-watch-series-9-gps",
                        "Chip S9, màn hình luôn bật, Double Tap, đo SpO2 và ECG",
                        new BigDecimal("10990000"),
                        "https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/MQDY3ref_VW_34FR+watch-45-alum-midnight-nc-9s_VW_34FR_WF_CO?wid=750&hei=712&trim=1", true, true),

                product(samsungId, watchId, "Samsung Galaxy Watch 6 Classic", "samsung-galaxy-watch-6-classic",
                        "Exynos W930, vòng quay vật lý, theo dõi sức khỏe toàn diện",
                        new BigDecimal("8490000"),
                        "https://images.samsung.com/is/image/samsung/p6pim/vn/sm-r960nzkaxsp/gallery/vn-galaxy-watch6-classic-47mm-sm-r960-sm-r960nzkaxsp-thumb-537064892", true, false)
        ));
    }

    private Brand brand(String name, String slug, String logo) {
        Brand b = new Brand();
        b.setName(name);
        b.setSlug(slug);
        b.setLogo(logo);
        b.setIsActive(true);
        return b;
    }

    private Category category(String name, String slug, String description) {
        Category c = new Category();
        c.setName(name);
        c.setSlug(slug);
        c.setDescription(description);
        c.setIsActive(true);
        return c;
    }

    private Product product(Integer brandId, Integer categoryId, String name, String slug,
                            String description, BigDecimal price, String thumbnail,
                            boolean isActive, boolean isFeatured) {
        Product p = new Product();
        p.setBrandId(brandId);
        p.setCategoryId(categoryId);
        p.setName(name);
        p.setSlug(slug);
        p.setDescription(description);
        p.setBasePrice(price);
        p.setThumbnail(thumbnail);
        p.setIsActive(isActive);
        p.setIsFeatured(isFeatured);
        p.setViewCount(0);
        return p;
    }
}
