package com.yazidwms.category.controller;

import com.yazidwms.category.dto.CategoryDtos;
import com.yazidwms.category.service.CategoryService;
import com.yazidwms.common.api.ApiResponse;
import com.yazidwms.common.api.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    ResponseEntity<ApiResponse<CategoryDtos.CategoryResponse>> create(@Valid @RequestBody CategoryDtos.CategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Category created", categoryService.create(request)));
    }

    @GetMapping
    ResponseEntity<ApiResponse<PageResponse<CategoryDtos.CategoryResponse>>> search(@RequestParam(required = false) String q, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Categories fetched", categoryService.search(q, pageable)));
    }

    @GetMapping("/{id}")
    ResponseEntity<ApiResponse<CategoryDtos.CategoryResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Category fetched", categoryService.get(id)));
    }

    @PutMapping("/{id}")
    ResponseEntity<ApiResponse<CategoryDtos.CategoryResponse>> update(@PathVariable Long id, @Valid @RequestBody CategoryDtos.CategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Category updated", categoryService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Category deleted", null));
    }
}
