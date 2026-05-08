package com.example.webbanphone.services;

import com.example.webbanphone.dto.category.CategoryRequest;
import com.example.webbanphone.dto.category.CategoryResponse;
import com.example.webbanphone.entities.Category;
import com.example.webbanphone.repositories.CategoryRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))
                .stream().map(this::toResponse).toList();
    }

    public CategoryResponse getById(Integer id) {
        return toResponse(findOrThrow(id));
    }

    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsBySlug(request.slug())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Slug đã tồn tại");
        }
        Category entity = new Category();
        entity.setName(request.name());
        entity.setSlug(request.slug());
        entity.setDescription(request.description());
        entity.setIsActive(request.isActive());
        return toResponse(categoryRepository.save(entity));
    }

    public CategoryResponse update(Integer id, CategoryRequest request) {
        if (categoryRepository.existsBySlugAndIdNot(request.slug(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Slug đã tồn tại");
        }
        Category entity = findOrThrow(id);
        entity.setName(request.name());
        entity.setSlug(request.slug());
        entity.setDescription(request.description());
        entity.setIsActive(request.isActive());
        return toResponse(categoryRepository.save(entity));
    }

    public void delete(Integer id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Danh mục không tồn tại");
        }
        categoryRepository.deleteById(id);
    }

    private Category findOrThrow(Integer id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Danh mục không tồn tại"));
    }

    private CategoryResponse toResponse(Category c) {
        return new CategoryResponse(c.getId(), c.getName(), c.getSlug(), c.getDescription(), c.getIsActive());
    }
}
