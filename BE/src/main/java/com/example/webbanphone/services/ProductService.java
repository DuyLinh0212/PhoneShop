package com.example.webbanphone.services;

import com.example.webbanphone.dto.product.ProductRequest;
import com.example.webbanphone.dto.product.ProductResponse;
import com.example.webbanphone.entities.Product;
import com.example.webbanphone.repositories.ProductRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
                .stream().map(this::toResponse).toList();
    }

    public ProductResponse getById(Integer id) {
        return toResponse(findOrThrow(id));
    }

    public ProductResponse create(ProductRequest request) {
        Product entity = new Product();
        applyRequest(entity, request);
        entity.setViewCount(0);
        return toResponse(productRepository.save(entity));
    }

    public ProductResponse update(Integer id, ProductRequest request) {
        Product entity = findOrThrow(id);
        applyRequest(entity, request);
        return toResponse(productRepository.save(entity));
    }

    public void delete(Integer id) {
        if (!productRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sản phẩm không tồn tại");
        }
        productRepository.deleteById(id);
    }

    private void applyRequest(Product entity, ProductRequest request) {
        entity.setBrandId(request.brandId());
        entity.setCategoryId(request.categoryId());
        entity.setName(request.name());
        entity.setSlug(request.slug());
        entity.setDescription(request.description());
        entity.setBasePrice(request.basePrice());
        entity.setThumbnail(request.thumbnail());
        entity.setIsActive(request.isActive());
        entity.setIsFeatured(request.isFeatured());
    }

    private Product findOrThrow(Integer id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sản phẩm không tồn tại"));
    }

    private ProductResponse toResponse(Product p) {
        return new ProductResponse(
                p.getId(), p.getBrandId(), p.getCategoryId(), p.getName(), p.getSlug(),
                p.getDescription(), p.getBasePrice(), p.getThumbnail(),
                p.getIsActive(), p.getIsFeatured(), p.getViewCount()
        );
    }
}
