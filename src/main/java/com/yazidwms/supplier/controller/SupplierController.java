package com.yazidwms.supplier.controller;

import com.yazidwms.common.api.ApiResponse;
import com.yazidwms.common.api.PageResponse;
import com.yazidwms.supplier.dto.SupplierDtos;
import com.yazidwms.supplier.service.SupplierService;
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
@RequestMapping("/api/v1/suppliers")
public class SupplierController {
    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @PostMapping
    ResponseEntity<ApiResponse<SupplierDtos.SupplierResponse>> create(@Valid @RequestBody SupplierDtos.SupplierRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Supplier created", supplierService.create(request)));
    }

    @GetMapping
    ResponseEntity<ApiResponse<PageResponse<SupplierDtos.SupplierResponse>>> search(@RequestParam(required = false) String q, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Suppliers fetched", supplierService.search(q, pageable)));
    }

    @GetMapping("/{id}")
    ResponseEntity<ApiResponse<SupplierDtos.SupplierResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Supplier fetched", supplierService.get(id)));
    }

    @PutMapping("/{id}")
    ResponseEntity<ApiResponse<SupplierDtos.SupplierResponse>> update(@PathVariable Long id, @Valid @RequestBody SupplierDtos.SupplierRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Supplier updated", supplierService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        supplierService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Supplier deleted", null));
    }
}
