package com.yazidwms.warehouse.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class WarehouseDtos {
    private WarehouseDtos() {
    }

    public record WarehouseRequest(@NotBlank String code, @NotBlank String name, String country, String city, String address) {
    }

    public record WarehouseResponse(Long id, String code, String name, String country, String city, String address, boolean active) {
    }

    public record ZoneRequest(@NotBlank String code, @NotBlank String name, @NotNull Long warehouseId) {
    }

    public record AisleRequest(@NotBlank String code, @NotNull Long zoneId) {
    }

    public record ShelfRequest(@NotBlank String code, @NotNull Long aisleId) {
    }

    public record BinRequest(@NotBlank String code, @Min(1) int capacity, @NotNull Long shelfId) {
    }

    public record LocationResponse(Long id, String code, String name, Long parentId) {
    }

    public record BinResponse(Long id, String code, int capacity, Long shelfId, boolean active) {
    }
}
