package com.example.webbanphone.services;

import com.example.webbanphone.dto.product.BrandResponse;
import com.example.webbanphone.dto.product.CategoryResponse;
import com.example.webbanphone.dto.product.ProductDetailResponse;
import com.example.webbanphone.dto.product.ProductImageRequest;
import com.example.webbanphone.dto.product.ProductImageResponse;
import com.example.webbanphone.dto.product.ProductRequest;
import com.example.webbanphone.dto.product.ProductResponse;
import com.example.webbanphone.dto.product.ProductSpecRequest;
import com.example.webbanphone.dto.product.ProductSpecResponse;
import com.example.webbanphone.dto.product.ProductVariantRequest;
import com.example.webbanphone.dto.product.ProductVariantResponse;
import com.example.webbanphone.entities.Brand;
import com.example.webbanphone.entities.Category;
import com.example.webbanphone.entities.Product;
import com.example.webbanphone.entities.ProductImage;
import com.example.webbanphone.entities.ProductSpec;
import com.example.webbanphone.entities.ProductVariant;
import com.example.webbanphone.repositories.BrandRepository;
import com.example.webbanphone.repositories.CategoryRepository;
import com.example.webbanphone.repositories.ProductImageRepository;
import com.example.webbanphone.repositories.ProductRepository;
import com.example.webbanphone.repositories.ProductSpecRepository;
import com.example.webbanphone.repositories.ProductVariantRepository;
import com.example.webbanphone.repositories.ReviewRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class ProductService {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductImageRepository imageRepository;
    private final ProductSpecRepository specRepository;
    private final ReviewRepository reviewRepository;

    public ProductService(
            ProductRepository productRepository,
            BrandRepository brandRepository,
            CategoryRepository categoryRepository,
            ProductVariantRepository variantRepository,
            ProductImageRepository imageRepository,
            ProductSpecRepository specRepository,
            ReviewRepository reviewRepository
    ) {
        this.productRepository = productRepository;
        this.brandRepository = brandRepository;
        this.categoryRepository = categoryRepository;
        this.variantRepository = variantRepository;
        this.imageRepository = imageRepository;
        this.specRepository = specRepository;
        this.reviewRepository = reviewRepository;
    }

    public List<ProductResponse> getAllProducts() {
        return getProducts(null, null, null, null, null, null, null, "newest");
    }

    public List<ProductResponse> getProducts(
            String q,
            Integer brandId,
            Integer categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean inStock,
            Boolean featured,
            String sort
    ) {
        String keyword = normalizeSearch(q);
        return productRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .filter(product -> keyword == null || matchesKeyword(product, keyword))
                .filter(product -> brandId == null || brandId.equals(product.getBrandId()))
                .filter(product -> categoryId == null || categoryId.equals(product.getCategoryId()))
                .map(this::toResponse)
                .filter(product -> minPrice == null || product.basePrice().compareTo(minPrice) >= 0)
                .filter(product -> maxPrice == null || product.basePrice().compareTo(maxPrice) <= 0)
                .filter(product -> inStock == null || !inStock || product.totalStock() > 0)
                .filter(product -> featured == null || featured.equals(product.isFeatured()))
                .sorted((left, right) -> compareProducts(left, right, sort))
                .toList();
    }

    public ProductDetailResponse getProductDetail(Integer id) {
        Product product = findProduct(id);
        return toDetailResponse(product);
    }

    public List<BrandResponse> getBrands() {
        return brandRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))
                .stream()
                .map(brand -> new BrandResponse(
                        brand.getId(),
                        brand.getName(),
                        brand.getLogo(),
                        brand.getIsActive()
                ))
                .toList();
    }

    public List<CategoryResponse> getCategories() {
        return categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))
                .stream()
                .map(category -> new CategoryResponse(
                        category.getId(),
                        category.getParentId(),
                        category.getName(),
                        category.getSlug(),
                        category.getDescription(),
                        category.getIsActive()
                ))
                .toList();
    }

    @Transactional
    public ProductDetailResponse createProduct(ProductRequest request) {
        validateProductRequest(request, null);

        Product product = new Product();
        applyProductFields(product, request);
        product.setViewCount(0);

        Product savedProduct = productRepository.save(product);
        replaceChildren(savedProduct.getId(), request);

        return toDetailResponse(savedProduct);
    }

    @Transactional
    public ProductDetailResponse updateProduct(Integer id, ProductRequest request) {
        Product product = findProduct(id);
        validateProductRequest(request, id);

        applyProductFields(product, request);
        Product savedProduct = productRepository.save(product);
        replaceChildren(savedProduct.getId(), request);

        return toDetailResponse(savedProduct);
    }

    @Transactional
    public void deactivateProduct(Integer id) {
        Product product = findProduct(id);
        product.setIsActive(false);
        productRepository.save(product);
    }

    private void validateProductRequest(ProductRequest request, Integer currentProductId) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product request is required");
        }

        if (request.brandId() == null || !brandRepository.existsById(request.brandId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valid brandId is required");
        }

        if (request.categoryId() == null || !categoryRepository.existsById(request.categoryId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valid categoryId is required");
        }

        if (isBlank(request.name())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product name is required");
        }

        if (isBlank(request.slug())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product slug is required");
        }

        String normalizedSlug = request.slug().trim().toLowerCase(Locale.ROOT);
        boolean slugExists = currentProductId == null
                ? productRepository.existsBySlugIgnoreCase(normalizedSlug)
                : productRepository.existsBySlugIgnoreCaseAndIdNot(normalizedSlug, currentProductId);
        if (slugExists) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Product slug already exists");
        }

        if (request.basePrice() != null && request.basePrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "basePrice must be greater than or equal to 0");
        }

        if (request.variants() != null) {
            Set<String> requestSkus = new HashSet<>();
            for (ProductVariantRequest variant : request.variants()) {
                if (variant.price() == null || variant.price().compareTo(BigDecimal.ZERO) < 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Each variant requires a valid price");
                }

                if (variant.costPrice() != null && variant.costPrice().compareTo(BigDecimal.ZERO) < 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Variant cost price must be greater than or equal to 0");
                }

                BigDecimal discountPercent = normalizeDiscountPercent(variant.discountPercent());
                if (discountPercent.compareTo(BigDecimal.ZERO) < 0 || discountPercent.compareTo(ONE_HUNDRED) > 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Variant discount percent must be between 0 and 100");
                }

                if (variant.stock() != null && variant.stock() < 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Variant stock must be greater than or equal to 0");
                }

                String normalizedSku = normalizeSku(variant.sku());
                if (normalizedSku == null) {
                    continue;
                }

                if (!requestSkus.add(normalizedSku)) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicate SKU in product variants");
                }

                boolean skuExists = currentProductId == null
                        ? variantRepository.existsBySkuIgnoreCase(normalizedSku)
                        : variantRepository.existsBySkuIgnoreCaseAndProductIdNot(normalizedSku, currentProductId);
                if (skuExists) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Product variant SKU already exists");
                }
            }
        }
    }

    private void applyProductFields(Product product, ProductRequest request) {
        product.setBrandId(request.brandId());
        product.setCategoryId(request.categoryId());
        product.setName(request.name().trim());
        product.setSlug(request.slug().trim().toLowerCase(Locale.ROOT));
        product.setDescription(trimToNull(request.description()));
        product.setBasePrice(resolveBasePrice(request));
        product.setThumbnail(trimToNull(request.thumbnail()));
        product.setIsActive(request.isActive() == null || request.isActive());
        product.setIsFeatured(Boolean.TRUE.equals(request.isFeatured()));
    }

    private BigDecimal resolveBasePrice(ProductRequest request) {
        if (request.basePrice() != null) {
            return request.basePrice();
        }

        if (request.variants() != null) {
            return request.variants()
                    .stream()
                    .filter(variant -> variant.price() != null)
                    .map(this::effectiveVariantPrice)
                    .findFirst()
                    .orElse(BigDecimal.ZERO);
        }

        return BigDecimal.ZERO;
    }

    private void replaceChildren(Integer productId, ProductRequest request) {
        imageRepository.deleteByProductId(productId);
        specRepository.deleteByProductId(productId);
        variantRepository.deleteByProductId(productId);
        imageRepository.flush();
        specRepository.flush();
        variantRepository.flush();

        if (request.variants() != null) {
            request.variants().forEach(variantRequest -> saveVariant(productId, variantRequest));
        }

        if (request.images() != null) {
            request.images().forEach(imageRequest -> saveImage(productId, imageRequest));
        }

        if (request.specs() != null) {
            request.specs().forEach(specRequest -> saveSpec(productId, specRequest));
        }
    }

    private void saveVariant(Integer productId, ProductVariantRequest request) {
        ProductVariant variant = new ProductVariant();
        variant.setProductId(productId);
        variant.setColor(trimToNull(request.color()));
        variant.setStorage(trimToNull(request.storage()));
        variant.setRam(trimToNull(request.ram()));
        variant.setPrice(request.price());
        variant.setCostPrice(request.costPrice());
        variant.setDiscountPercent(resolveDiscountPercent(request));
        variant.setSalePrice(resolveSalePrice(request));
        variant.setStock(request.stock() == null ? 0 : request.stock());
        variant.setSku(trimToNull(request.sku()));
        variant.setIsActive(request.isActive() == null || request.isActive());
        variantRepository.save(variant);
    }

    private void saveImage(Integer productId, ProductImageRequest request) {
        if (request == null || isBlank(request.imageUrl())) {
            return;
        }

        ProductImage image = new ProductImage();
        image.setProductId(productId);
        image.setImageUrl(request.imageUrl().trim());
        image.setAltText(trimToNull(request.altText()));
        image.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        image.setIsMain(request.isMain() == null ? image.getSortOrder() == 0 : request.isMain());
        imageRepository.save(image);
    }

    private void saveSpec(Integer productId, ProductSpecRequest request) {
        if (request == null || isBlank(request.specKey()) || isBlank(request.specValue())) {
            return;
        }

        ProductSpec spec = new ProductSpec();
        spec.setProductId(productId);
        spec.setSpecKey(request.specKey().trim());
        spec.setSpecValue(request.specValue().trim());
        spec.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        specRepository.save(spec);
    }

    private Product findProduct(Integer id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    private ProductResponse toResponse(Product product) {
        List<ProductVariant> variants = variantRepository.findByProductIdOrderByIdAsc(product.getId());
        ProductPricing pricing = pricingFor(product, variants);
        ProductRating rating = ratingFor(product.getId());
        return new ProductResponse(
                product.getId(),
                product.getBrandId(),
                product.getCategoryId(),
                product.getName(),
                product.getSlug(),
                product.getDescription(),
                pricing.basePrice(),
                pricing.originalPrice(),
                pricing.salePrice(),
                pricing.discountPercent(),
                product.getThumbnail(),
                product.getIsActive(),
                product.getIsFeatured(),
                product.getViewCount(),
                variants.stream()
                        .mapToInt(variant -> variant.getStock() == null ? 0 : variant.getStock())
                        .sum(),
                rating.averageRating(),
                rating.reviewCount()
        );
    }

    private ProductDetailResponse toDetailResponse(Product product) {
        List<ProductVariant> variants = variantRepository.findByProductIdOrderByIdAsc(product.getId());
        ProductPricing pricing = pricingFor(product, variants);
        ProductRating rating = ratingFor(product.getId());
        return new ProductDetailResponse(
                product.getId(),
                product.getBrandId(),
                product.getCategoryId(),
                product.getName(),
                product.getSlug(),
                product.getDescription(),
                pricing.basePrice(),
                pricing.originalPrice(),
                pricing.salePrice(),
                pricing.discountPercent(),
                product.getThumbnail(),
                product.getIsActive(),
                product.getIsFeatured(),
                product.getViewCount(),
                rating.averageRating(),
                rating.reviewCount(),
                variants.stream()
                        .map(this::toVariantResponse)
                        .toList(),
                imageRepository.findByProductIdOrderBySortOrderAscIdAsc(product.getId())
                        .stream()
                        .map(this::toImageResponse)
                        .toList(),
                specRepository.findByProductIdOrderBySortOrderAscIdAsc(product.getId())
                        .stream()
                        .map(this::toSpecResponse)
                        .toList()
        );
    }

    private ProductVariantResponse toVariantResponse(ProductVariant variant) {
        BigDecimal salePrice = effectiveSalePrice(
                variant.getPrice(),
                variant.getSalePrice(),
                variant.getDiscountPercent()
        );
        return new ProductVariantResponse(
                variant.getId(),
                variant.getColor(),
                variant.getStorage(),
                variant.getRam(),
                variant.getPrice(),
                salePrice,
                variant.getCostPrice(),
                effectiveDiscountPercent(variant.getPrice(), salePrice, variant.getDiscountPercent()),
                variant.getStock(),
                variant.getSku(),
                variant.getIsActive()
        );
    }

    private ProductImageResponse toImageResponse(ProductImage image) {
        return new ProductImageResponse(
                image.getId(),
                image.getImageUrl(),
                image.getAltText(),
                image.getSortOrder(),
                image.getIsMain()
        );
    }

    private ProductSpecResponse toSpecResponse(ProductSpec spec) {
        return new ProductSpecResponse(
                spec.getId(),
                spec.getSpecKey(),
                spec.getSpecValue(),
                spec.getSortOrder()
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean matchesKeyword(Product product, String keyword) {
        return safeSearch(product.getName()).contains(keyword)
                || safeSearch(product.getSlug()).contains(keyword)
                || safeSearch(product.getDescription()).contains(keyword);
    }

    private String normalizeSearch(String value) {
        if (isBlank(value)) {
            return null;
        }

        return value.trim().toLowerCase(Locale.ROOT);
    }

    private String safeSearch(String value) {
        String normalized = normalizeSearch(value);
        return normalized == null ? "" : normalized;
    }

    private int compareProducts(ProductResponse left, ProductResponse right, String sort) {
        String normalizedSort = sort == null ? "newest" : sort.trim().toLowerCase(Locale.ROOT);
        return switch (normalizedSort) {
            case "price_asc" -> left.basePrice().compareTo(right.basePrice());
            case "price_desc" -> right.basePrice().compareTo(left.basePrice());
            case "popular" -> Integer.compare(right.viewCount() == null ? 0 : right.viewCount(), left.viewCount() == null ? 0 : left.viewCount());
            case "stock" -> Integer.compare(right.totalStock(), left.totalStock());
            default -> Integer.compare(right.id(), left.id());
        };
    }

    private String trimToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private String normalizeSku(String value) {
        return isBlank(value) ? null : value.trim().toLowerCase(Locale.ROOT);
    }

    private BigDecimal effectiveVariantPrice(ProductVariantRequest variant) {
        BigDecimal salePrice = resolveSalePrice(variant);
        return salePrice == null ? variant.price() : salePrice;
    }

    private BigDecimal resolveSalePrice(ProductVariantRequest request) {
        BigDecimal price = request.price();
        if (price == null) {
            return null;
        }

        BigDecimal discountPercent = normalizeDiscountPercent(request.discountPercent());
        if (discountPercent.compareTo(BigDecimal.ZERO) > 0) {
            return applyDiscount(price, discountPercent);
        }

        BigDecimal salePrice = request.salePrice();
        if (salePrice != null && salePrice.compareTo(BigDecimal.ZERO) >= 0 && salePrice.compareTo(price) < 0) {
            return salePrice.setScale(2, RoundingMode.HALF_UP);
        }

        return null;
    }

    private BigDecimal resolveDiscountPercent(ProductVariantRequest request) {
        BigDecimal discountPercent = normalizeDiscountPercent(request.discountPercent());
        if (discountPercent.compareTo(BigDecimal.ZERO) > 0) {
            return discountPercent;
        }

        return calculateDiscountPercent(request.price(), request.salePrice());
    }

    private ProductPricing pricingFor(Product product, List<ProductVariant> variants) {
        ProductVariant variant = variants.stream()
                .filter(item -> !Boolean.FALSE.equals(item.getIsActive()))
                .findFirst()
                .orElse(null);
        if (variant == null) {
            BigDecimal basePrice = product.getBasePrice() == null ? BigDecimal.ZERO : product.getBasePrice();
            return new ProductPricing(basePrice, basePrice, null, BigDecimal.ZERO);
        }

        BigDecimal originalPrice = variant.getPrice() == null ? BigDecimal.ZERO : variant.getPrice();
        BigDecimal salePrice = effectiveSalePrice(originalPrice, variant.getSalePrice(), variant.getDiscountPercent());
        BigDecimal discountPercent = effectiveDiscountPercent(originalPrice, salePrice, variant.getDiscountPercent());
        BigDecimal basePrice = salePrice == null ? originalPrice : salePrice;

        return new ProductPricing(basePrice, originalPrice, salePrice, discountPercent);
    }

    private BigDecimal effectiveSalePrice(BigDecimal price, BigDecimal salePrice, BigDecimal discountPercent) {
        if (price == null) {
            return null;
        }

        BigDecimal normalizedDiscount = normalizeDiscountPercent(discountPercent);
        if (normalizedDiscount.compareTo(BigDecimal.ZERO) > 0) {
            return applyDiscount(price, normalizedDiscount);
        }

        if (salePrice != null && salePrice.compareTo(BigDecimal.ZERO) >= 0 && salePrice.compareTo(price) < 0) {
            return salePrice.setScale(2, RoundingMode.HALF_UP);
        }

        return null;
    }

    private BigDecimal effectiveDiscountPercent(BigDecimal price, BigDecimal salePrice, BigDecimal discountPercent) {
        BigDecimal normalizedDiscount = normalizeDiscountPercent(discountPercent);
        if (normalizedDiscount.compareTo(BigDecimal.ZERO) > 0) {
            return normalizedDiscount;
        }

        return calculateDiscountPercent(price, salePrice);
    }

    private BigDecimal calculateDiscountPercent(BigDecimal price, BigDecimal salePrice) {
        if (price == null || salePrice == null || price.compareTo(BigDecimal.ZERO) <= 0 || salePrice.compareTo(price) >= 0) {
            return BigDecimal.ZERO;
        }

        return price.subtract(salePrice)
                .multiply(ONE_HUNDRED)
                .divide(price, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal applyDiscount(BigDecimal price, BigDecimal discountPercent) {
        return price.multiply(ONE_HUNDRED.subtract(discountPercent))
                .divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal normalizeDiscountPercent(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value.setScale(2, RoundingMode.HALF_UP);
    }

    private ProductRating ratingFor(Integer productId) {
        long reviewCount = reviewRepository.countByProductIdAndIsApprovedTrue(productId);
        if (reviewCount == 0) {
            return new ProductRating(0, 0);
        }

        double averageRating = reviewRepository.averageRatingByProductId(productId);
        return new ProductRating(Math.round(averageRating * 10.0) / 10.0, reviewCount);
    }

    private record ProductPricing(
            BigDecimal basePrice,
            BigDecimal originalPrice,
            BigDecimal salePrice,
            BigDecimal discountPercent
    ) {
    }

    private record ProductRating(double averageRating, long reviewCount) {
    }
}
