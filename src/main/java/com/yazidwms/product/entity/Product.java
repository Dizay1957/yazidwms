package com.yazidwms.product.entity;

import com.yazidwms.category.entity.Category;
import com.yazidwms.common.entity.BaseEntity;
import com.yazidwms.supplier.entity.Supplier;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "products",
        indexes = {
                @Index(name = "idx_products_sku", columnList = "sku"),
                @Index(name = "idx_products_barcode", columnList = "barcode")
        }
)
public class Product extends BaseEntity {

    @Column(nullable = false, unique = true, length = 80)
    private String sku;

    @Column(nullable = false, unique = true, length = 80)
    private String barcode;

    @Column(nullable = false, length = 180)
    private String name;

    @Column(length = 1000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal purchasePrice = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal sellingPrice = BigDecimal.ZERO;

    @Column(nullable = false, length = 30)
    private String unit;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal weight = BigDecimal.ZERO;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private int minimumQuantity;

    @Column(nullable = false)
    private int maximumQuantity;

    public boolean isLowStock() {
        return quantity <= minimumQuantity;
    }
}
