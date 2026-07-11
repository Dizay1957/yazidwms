package com.yazidwms.purchaseorder.controller;

import com.yazidwms.common.api.ApiResponse;
import com.yazidwms.common.api.PageResponse;
import com.yazidwms.purchaseorder.dto.PurchaseOrderDtos;
import com.yazidwms.purchaseorder.service.PurchaseOrderService;
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
@RequestMapping("/api/v1/purchase-orders")
public class PurchaseOrderController {
    private final PurchaseOrderService purchaseOrderService;

    public PurchaseOrderController(PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }

    @PostMapping
    ResponseEntity<ApiResponse<PurchaseOrderDtos.PurchaseOrderResponse>> create(@Valid @RequestBody PurchaseOrderDtos.PurchaseOrderRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Purchase order created", purchaseOrderService.create(request)));
    }

    @GetMapping
    ResponseEntity<ApiResponse<PageResponse<PurchaseOrderDtos.PurchaseOrderResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Purchase orders fetched", purchaseOrderService.list(pageable)));
    }

    @GetMapping("/{id}")
    ResponseEntity<ApiResponse<PurchaseOrderDtos.PurchaseOrderResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Purchase order fetched", purchaseOrderService.get(id)));
    }

    @PatchMapping("/{id}/confirm")
    ResponseEntity<ApiResponse<PurchaseOrderDtos.PurchaseOrderResponse>> confirm(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Purchase order confirmed", purchaseOrderService.confirm(id)));
    }

    @PatchMapping("/{id}/receive")
    ResponseEntity<ApiResponse<PurchaseOrderDtos.PurchaseOrderResponse>> receive(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Purchase order received", purchaseOrderService.receive(id)));
    }

    @PatchMapping("/{id}/cancel")
    ResponseEntity<ApiResponse<PurchaseOrderDtos.PurchaseOrderResponse>> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Purchase order cancelled", purchaseOrderService.cancel(id)));
    }
}
