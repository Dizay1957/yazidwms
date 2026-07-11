package com.yazidwms.customer.controller;

import com.yazidwms.common.api.ApiResponse;
import com.yazidwms.common.api.PageResponse;
import com.yazidwms.customer.dto.CustomerDtos;
import com.yazidwms.customer.service.CustomerService;
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
@RequestMapping("/api/v1/customers")
public class CustomerController {
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    ResponseEntity<ApiResponse<CustomerDtos.CustomerResponse>> create(@Valid @RequestBody CustomerDtos.CustomerRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Customer created", customerService.create(request)));
    }

    @GetMapping
    ResponseEntity<ApiResponse<PageResponse<CustomerDtos.CustomerResponse>>> search(@RequestParam(required = false) String q, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Customers fetched", customerService.search(q, pageable)));
    }

    @GetMapping("/{id}")
    ResponseEntity<ApiResponse<CustomerDtos.CustomerResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Customer fetched", customerService.get(id)));
    }

    @PutMapping("/{id}")
    ResponseEntity<ApiResponse<CustomerDtos.CustomerResponse>> update(@PathVariable Long id, @Valid @RequestBody CustomerDtos.CustomerRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Customer updated", customerService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        customerService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Customer deleted", null));
    }
}
