package com.example.webbanphone.services;

import com.example.webbanphone.dto.brand.BrandRequest;
import com.example.webbanphone.dto.brand.BrandResponse;
import com.example.webbanphone.entities.Brand;
import com.example.webbanphone.repositories.BrandRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class BrandService {

    private final BrandRepository brandRepository;

    public BrandService(BrandRepository brandRepository) {
        this.brandRepository = brandRepository;
    }

    public List<BrandResponse> getAll() {
        return brandRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))
                .stream().map(this::toResponse).toList();
    }

    public BrandResponse getById(Integer id) {
        return toResponse(findOrThrow(id));
    }

    public BrandResponse create(BrandRequest request) {
        if (brandRepository.existsBySlug(request.slug())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Slug đã tồn tại");
        }
        Brand entity = new Brand();
        entity.setName(request.name());
        entity.setSlug(request.slug());
        entity.setLogo(request.logo());
        entity.setIsActive(request.isActive());
        return toResponse(brandRepository.save(entity));
    }

    public BrandResponse update(Integer id, BrandRequest request) {
        if (brandRepository.existsBySlugAndIdNot(request.slug(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Slug đã tồn tại");
        }
        Brand entity = findOrThrow(id);
        entity.setName(request.name());
        entity.setSlug(request.slug());
        entity.setLogo(request.logo());
        entity.setIsActive(request.isActive());
        return toResponse(brandRepository.save(entity));
    }

    public void delete(Integer id) {
        if (!brandRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Thương hiệu không tồn tại");
        }
        brandRepository.deleteById(id);
    }

    private Brand findOrThrow(Integer id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Thương hiệu không tồn tại"));
    }

    private BrandResponse toResponse(Brand b) {
        return new BrandResponse(b.getId(), b.getName(), b.getSlug(), b.getLogo(), b.getIsActive());
    }
}
