package com.yazidwms.product.controller;

import com.yazidwms.common.api.ApiResponse;
import com.yazidwms.common.api.PageResponse;
import com.yazidwms.product.dto.ProductDtos;
import com.yazidwms.product.service.ProductService;
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
@RequestMapping("/api/v1/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    ResponseEntity<ApiResponse<ProductDtos.ProductResponse>> create(@Valid @RequestBody ProductDtos.ProductRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Product created", productService.create(request)));
    }

    @GetMapping
    ResponseEntity<ApiResponse<PageResponse<ProductDtos.ProductResponse>>> search(@RequestParam(required = false) String q, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Products fetched", productService.search(q, pageable)));
    }

    @GetMapping("/{id}")
    ResponseEntity<ApiResponse<ProductDtos.ProductResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Product fetched", productService.get(id)));
    }

    @PutMapping("/{id}")
    ResponseEntity<ApiResponse<ProductDtos.ProductResponse>> update(@PathVariable Long id, @Valid @RequestBody ProductDtos.ProductRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Product updated", productService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Product deleted", null));
    }
}
