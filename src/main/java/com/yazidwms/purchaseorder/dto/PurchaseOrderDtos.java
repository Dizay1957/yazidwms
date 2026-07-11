package com.yazidwms.purchaseorder.dto;

import com.yazidwms.purchaseorder.entity.PurchaseOrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class PurchaseOrderDtos {
    private PurchaseOrderDtos() {
    }

    public record PurchaseOrderItemRequest(@NotNull Long productId, @NotNull Long binId, @Min(1) int quantity, @NotNull BigDecimal unitPrice) {
    }

    public record PurchaseOrderRequest(@NotBlank String orderNumber, @NotNull Long supplierId, @Valid @NotEmpty List<PurchaseOrderItemRequest> items) {
    }

    public record PurchaseOrderItemResponse(Long productId, String sku, Long binId, String binCode, int quantity, BigDecimal unitPrice, BigDecimal lineTotal) {
    }

    public record PurchaseOrderResponse(Long id, String orderNumber, Long supplierId, String supplierName, PurchaseOrderStatus status,
                                        BigDecimal totalAmount, Instant confirmedAt, Instant receivedAt, List<PurchaseOrderItemResponse> items) {
    }
}
