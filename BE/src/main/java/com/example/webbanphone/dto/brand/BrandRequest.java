package com.example.webbanphone.dto.brand;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BrandRequest(
        @NotBlank @Size(max = 150) String name,
        @NotBlank @Size(max = 200) String slug,
        @Size(max = 500) String logo,
        @NotNull Boolean isActive
) {}
