package com.yazidwms.warehouse.controller;

import com.yazidwms.common.api.ApiResponse;
import com.yazidwms.common.api.PageResponse;
import com.yazidwms.warehouse.dto.WarehouseDtos;
import com.yazidwms.warehouse.service.WarehouseService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/warehouses")
public class WarehouseController {
    private final WarehouseService warehouseService;

    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @PostMapping
    ResponseEntity<ApiResponse<WarehouseDtos.WarehouseResponse>> create(@Valid @RequestBody WarehouseDtos.WarehouseRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Warehouse created", warehouseService.createWarehouse(request)));
    }

    @GetMapping
    ResponseEntity<ApiResponse<PageResponse<WarehouseDtos.WarehouseResponse>>> search(@RequestParam(required = false) String q, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Warehouses fetched", warehouseService.search(q, pageable)));
    }

    @PostMapping("/zones")
    ResponseEntity<ApiResponse<WarehouseDtos.LocationResponse>> zone(@Valid @RequestBody WarehouseDtos.ZoneRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Zone created", warehouseService.createZone(request)));
    }

    @PostMapping("/aisles")
    ResponseEntity<ApiResponse<WarehouseDtos.LocationResponse>> aisle(@Valid @RequestBody WarehouseDtos.AisleRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Aisle created", warehouseService.createAisle(request)));
    }

    @PostMapping("/shelves")
    ResponseEntity<ApiResponse<WarehouseDtos.LocationResponse>> shelf(@Valid @RequestBody WarehouseDtos.ShelfRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Shelf created", warehouseService.createShelf(request)));
    }

    @PostMapping("/bins")
    ResponseEntity<ApiResponse<WarehouseDtos.BinResponse>> bin(@Valid @RequestBody WarehouseDtos.BinRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Bin created", warehouseService.createBin(request)));
    }

    @GetMapping("/{warehouseId}/zones")
    ResponseEntity<ApiResponse<List<WarehouseDtos.LocationResponse>>> zones(@PathVariable Long warehouseId) {
        return ResponseEntity.ok(ApiResponse.ok("Zones fetched", warehouseService.zones(warehouseId)));
    }

    @GetMapping("/zones/{zoneId}/aisles")
    ResponseEntity<ApiResponse<List<WarehouseDtos.LocationResponse>>> aisles(@PathVariable Long zoneId) {
        return ResponseEntity.ok(ApiResponse.ok("Aisles fetched", warehouseService.aisles(zoneId)));
    }

    @GetMapping("/aisles/{aisleId}/shelves")
    ResponseEntity<ApiResponse<List<WarehouseDtos.LocationResponse>>> shelves(@PathVariable Long aisleId) {
        return ResponseEntity.ok(ApiResponse.ok("Shelves fetched", warehouseService.shelves(aisleId)));
    }

    @GetMapping("/shelves/{shelfId}/bins")
    ResponseEntity<ApiResponse<List<WarehouseDtos.BinResponse>>> bins(@PathVariable Long shelfId) {
        return ResponseEntity.ok(ApiResponse.ok("Bins fetched", warehouseService.bins(shelfId)));
    }
}
