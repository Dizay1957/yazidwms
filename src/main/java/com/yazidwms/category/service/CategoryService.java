package com.yazidwms.category.service;

import com.yazidwms.category.dto.CategoryDtos.CategoryRequest;
import com.yazidwms.category.dto.CategoryDtos.CategoryResponse;
import com.yazidwms.category.entity.Category;
import com.yazidwms.category.mapper.CategoryMapper;
import com.yazidwms.category.repository.CategoryRepository;
import com.yazidwms.common.api.PageResponse;
import com.yazidwms.exception.BusinessException;
import com.yazidwms.exception.NotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.name())) {
            throw new BusinessException("Category name already exists");
        }
        var category = new Category();
        apply(request, category);
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    public PageResponse<CategoryResponse> search(String q, Pageable pageable) {
        var page = q == null || q.isBlank()
                ? categoryRepository.findByDeletedFalse(pageable)
                : categoryRepository.findByDeletedFalseAndNameContainingIgnoreCase(q, pageable);
        return PageResponse.from(page.map(categoryMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public CategoryResponse get(Long id) {
        return categoryMapper.toResponse(findActive(id));
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        var category = findActive(id);
        apply(request, category);
        return categoryMapper.toResponse(category);
    }

    @Transactional
    public void delete(Long id) {
        var category = findActive(id);
        category.setDeleted(true);
        category.setActive(false);
    }

    public Category findActive(Long id) {
        var category = categoryRepository.findById(id).orElseThrow(() -> new NotFoundException("Category", id));
        if (category.isDeleted()) {
            throw new NotFoundException("Category", id);
        }
        return category;
    }

    private void apply(CategoryRequest request, Category category) {
        category.setName(request.name());
        category.setDescription(request.description());
        if (request.parentId() != null) {
            if (category.getId() != null && category.getId().equals(request.parentId())) {
                throw new BusinessException("Category cannot be its own parent");
            }
            category.setParent(findActive(request.parentId()));
        } else {
            category.setParent(null);
        }
    }
}
