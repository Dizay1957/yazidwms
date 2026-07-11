package com.yazidwms.salesorder.controller;

import com.yazidwms.common.api.ApiResponse;
import com.yazidwms.common.api.PageResponse;
import com.yazidwms.salesorder.dto.SalesOrderDtos;
import com.yazidwms.salesorder.service.SalesOrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sales-orders")
public class SalesOrderController {
    private final SalesOrderService salesOrderService;

    public SalesOrderController(SalesOrderService salesOrderService) {
        this.salesOrderService = salesOrderService;
    }

    @PostMapping
    ResponseEntity<ApiResponse<SalesOrderDtos.SalesOrderResponse>> create(@Valid @RequestBody SalesOrderDtos.SalesOrderRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Sales order created", salesOrderService.create(request)));
    }

    @GetMapping
    ResponseEntity<ApiResponse<PageResponse<SalesOrderDtos.SalesOrderResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Sales orders fetched", salesOrderService.list(pageable)));
    }

    @GetMapping("/{id}")
    ResponseEntity<ApiResponse<SalesOrderDtos.SalesOrderResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Sales order fetched", salesOrderService.get(id)));
    }

    @PatchMapping("/{id}/confirm")
    ResponseEntity<ApiResponse<SalesOrderDtos.SalesOrderResponse>> confirm(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Sales order confirmed", salesOrderService.confirm(id)));
    }

    @PatchMapping("/{id}/ship")
    ResponseEntity<ApiResponse<SalesOrderDtos.SalesOrderResponse>> ship(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Sales order shipped", salesOrderService.ship(id)));
    }

    @PatchMapping("/{id}/cancel")
    ResponseEntity<ApiResponse<SalesOrderDtos.SalesOrderResponse>> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Sales order cancelled", salesOrderService.cancel(id)));
    }
}
