package com.Ishwarjit.Wolf_OVRN_backend.service;

import com.Ishwarjit.Wolf_OVRN_backend.dto.CategoryResponse;
import com.Ishwarjit.Wolf_OVRN_backend.repository.CategoryRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.Ishwarjit.Wolf_OVRN_backend.dto.CategoryRequest;
import com.Ishwarjit.Wolf_OVRN_backend.entity.Category;
import com.Ishwarjit.Wolf_OVRN_backend.exception.ResourceNotFoundException;
import com.Ishwarjit.Wolf_OVRN_backend.util.SlugUtils;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public Page<CategoryResponse> listAll(int page, int limit) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(limit, 1));
        return categoryRepository.findAll(pageable)
                .map(CategoryResponse::from);
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        Category category = new Category();
        category.setName(request.getName());
        
        String slug = (request.getSlug() != null && !request.getSlug().isBlank()) 
                ? SlugUtils.generate(request.getSlug()) 
                : SlugUtils.generate(request.getName());
        category.setSlug(slug);
        
        category.setDescription(request.getDescription());
        
        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));
            category.setParent(parent);
        }

        Category saved = categoryRepository.save(category);
        return CategoryResponse.from(saved);
    }

    @Transactional
    public CategoryResponse update(UUID id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (request.getName() != null) {
            category.setName(request.getName());
        }
        
        if (request.getSlug() != null) {
            category.setSlug(SlugUtils.generate(request.getSlug()));
        } else if (request.getName() != null && !request.getName().isBlank() && category.getSlug() == null) {
             category.setSlug(SlugUtils.generate(request.getName()));
        }

        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }

        if (request.getParentId() != null) {
            Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }

        Category updated = categoryRepository.save(category);
        return CategoryResponse.from(updated);
    }

    @Transactional
    public void delete(UUID id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found");
        }
        categoryRepository.deleteById(id);
    }
}
