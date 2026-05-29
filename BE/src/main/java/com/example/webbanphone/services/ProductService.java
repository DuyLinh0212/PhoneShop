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
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductImageRepository imageRepository;
    private final ProductSpecRepository specRepository;

    public ProductService(
            ProductRepository productRepository,
            BrandRepository brandRepository,
            CategoryRepository categoryRepository,
            ProductVariantRepository variantRepository,
            ProductImageRepository imageRepository,
            ProductSpecRepository specRepository
    ) {
        this.productRepository = productRepository;
        this.brandRepository = brandRepository;
        this.categoryRepository = categoryRepository;
        this.variantRepository = variantRepository;
        this.imageRepository = imageRepository;
        this.specRepository = specRepository;
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .map(this::toResponse)
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
                    .map(variant -> variant.salePrice() != null ? variant.salePrice() : variant.price())
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
        variant.setSalePrice(request.salePrice());
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
        return new ProductResponse(
                product.getId(),
                product.getBrandId(),
                product.getCategoryId(),
                product.getName(),
                product.getSlug(),
                product.getDescription(),
                product.getBasePrice(),
                product.getThumbnail(),
                product.getIsActive(),
                product.getIsFeatured(),
                product.getViewCount(),
                variantRepository.findByProductIdOrderByIdAsc(product.getId())
                        .stream()
                        .mapToInt(variant -> variant.getStock() == null ? 0 : variant.getStock())
                        .sum()
        );
    }

    private ProductDetailResponse toDetailResponse(Product product) {
        return new ProductDetailResponse(
                product.getId(),
                product.getBrandId(),
                product.getCategoryId(),
                product.getName(),
                product.getSlug(),
                product.getDescription(),
                product.getBasePrice(),
                product.getThumbnail(),
                product.getIsActive(),
                product.getIsFeatured(),
                product.getViewCount(),
                variantRepository.findByProductIdOrderByIdAsc(product.getId())
                        .stream()
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
        return new ProductVariantResponse(
                variant.getId(),
                variant.getColor(),
                variant.getStorage(),
                variant.getRam(),
                variant.getPrice(),
                variant.getSalePrice(),
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
                image.getSortOrder()
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

    private String trimToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private String normalizeSku(String value) {
        return isBlank(value) ? null : value.trim().toLowerCase(Locale.ROOT);
    }
}
