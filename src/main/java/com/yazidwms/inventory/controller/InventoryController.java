package com.yazidwms.inventory.controller;

import com.yazidwms.common.api.ApiResponse;
import com.yazidwms.common.api.PageResponse;
import com.yazidwms.inventory.dto.InventoryDtos;
import com.yazidwms.inventory.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {
    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    ResponseEntity<ApiResponse<PageResponse<InventoryDtos.InventoryResponse>>> list(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Inventory fetched", inventoryService.list(pageable)));
    }

    @PatchMapping("/adjust")
    ResponseEntity<ApiResponse<InventoryDtos.InventoryResponse>> adjust(@Valid @RequestBody InventoryDtos.InventoryAdjustmentRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Inventory adjusted", inventoryService.adjust(request)));
    }

    @PostMapping("/transfer")
    ResponseEntity<ApiResponse<Void>> transfer(@Valid @RequestBody InventoryDtos.TransferRequest request) {
        inventoryService.transfer(request);
        return ResponseEntity.ok(ApiResponse.ok("Inventory transferred", null));
    }

    @GetMapping("/movements")
    ResponseEntity<ApiResponse<PageResponse<InventoryDtos.StockMovementResponse>>> movements(@RequestParam(required = false) Long productId, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Movements fetched", inventoryService.movements(productId, pageable)));
    }
}
