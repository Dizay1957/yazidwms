package com.yazidwms.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public final class ProductDtos {
    private ProductDtos() {
    }

    public record ProductRequest(
            @NotBlank String sku,
            @NotBlank String barcode,
            @NotBlank String name,
            String description,
            @NotNull Long categoryId,
            @NotNull Long supplierId,
            @DecimalMin("0.00") BigDecimal purchasePrice,
            @DecimalMin("0.00") BigDecimal sellingPrice,
            @NotBlank String unit,
            @DecimalMin("0.00") BigDecimal weight,
            @Min(0) int minimumQuantity,
            @Min(0) int maximumQuantity,
            Boolean active
    ) {
    }

    public record ProductResponse(
            Long id,
            String sku,
            String barcode,
            String name,
            String description,
            Long categoryId,
            String categoryName,
            Long supplierId,
            String supplierName,
            BigDecimal purchasePrice,
            BigDecimal sellingPrice,
            String unit,
            BigDecimal weight,
            int quantity,
            int minimumQuantity,
            int maximumQuantity,
            boolean active,
            boolean lowStock
    ) {
    }
}
