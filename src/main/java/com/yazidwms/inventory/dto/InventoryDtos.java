package com.yazidwms.inventory.dto;

import com.yazidwms.stockmovement.entity.StockMovementType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public final class InventoryDtos {
    private InventoryDtos() {
    }

    public record InventoryAdjustmentRequest(@NotNull Long productId, @NotNull Long binId, @Min(0) int newQuantity,
                                             @NotBlank String reason, String notes) {
    }

    public record TransferRequest(@NotNull Long productId, @NotNull Long fromBinId, @NotNull Long toBinId, @Min(1) int quantity,
                                  @NotBlank String reason, String notes) {
    }

    public record InventoryResponse(Long id, Long productId, String sku, String productName, Long binId, String binCode, int quantity) {
    }

    public record StockMovementResponse(Long id, StockMovementType type, Long productId, String sku, Long fromBinId, Long toBinId,
                                        int quantity, Instant timestamp, String reference, String reason,
                                        int previousQuantity, int newQuantity, String notes) {
    }
}
