package com.example.webbanphone.dto.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductRequest(
        @NotNull Integer brandId,
        @NotNull Integer categoryId,
        @NotBlank @Size(max = 255) String name,
        @NotBlank @Size(max = 300) String slug,
        String description,
        @NotNull @DecimalMin("0") BigDecimal basePrice,
        @Size(max = 500) String thumbnail,
        @NotNull Boolean isActive,
        @NotNull Boolean isFeatured
) {}
