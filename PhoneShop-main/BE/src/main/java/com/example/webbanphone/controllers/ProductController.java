package com.example.webbanphone.controllers;

import com.example.webbanphone.dto.product.BrandResponse;
import com.example.webbanphone.dto.product.CategoryResponse;
import com.example.webbanphone.dto.product.ProductDetailResponse;
import com.example.webbanphone.dto.product.ProductResponse;
import com.example.webbanphone.services.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer brandId,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(required = false) Boolean featured,
            @RequestParam(required = false, defaultValue = "newest") String sort
    ) {
        return ResponseEntity.ok(productService.getProducts(q, brandId, categoryId, minPrice, maxPrice, inStock, featured, sort));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDetailResponse> getProductDetail(@PathVariable Integer id) {
        return ResponseEntity.ok(productService.getProductDetail(id));
    }

    @GetMapping("/brands")
    public ResponseEntity<List<BrandResponse>> getBrands() {
        return ResponseEntity.ok(productService.getBrands());
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponse>> getCategories() {
        return ResponseEntity.ok(productService.getCategories());
    }
}
