package com.yazidwms.salesorder.dto;

import com.yazidwms.salesorder.entity.SalesOrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class SalesOrderDtos {
    private SalesOrderDtos() {
    }

    public record SalesOrderItemRequest(@NotNull Long productId, @NotNull Long binId, @Min(1) int quantity, @NotNull BigDecimal unitPrice) {
    }

    public record SalesOrderRequest(@NotBlank String orderNumber, @NotNull Long customerId, @Valid @NotEmpty List<SalesOrderItemRequest> items) {
    }

    public record SalesOrderItemResponse(Long productId, String sku, Long binId, String binCode, int quantity, BigDecimal unitPrice, BigDecimal lineTotal) {
    }

    public record SalesOrderResponse(Long id, String orderNumber, Long customerId, String customerName, SalesOrderStatus status,
                                     BigDecimal totalAmount, Instant confirmedAt, Instant shippedAt, List<SalesOrderItemResponse> items) {
    }
}
