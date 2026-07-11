package com.yazidwms.stockmovement.entity;

import com.yazidwms.common.entity.BaseEntity;
import com.yazidwms.product.entity.Product;
import com.yazidwms.user.entity.User;
import com.yazidwms.warehouse.entity.WarehouseBin;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "stock_movements", indexes = @Index(name = "idx_stock_movement_product_time", columnList = "product_id,timestamp"))
public class StockMovement extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StockMovementType type;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_bin_id")
    private WarehouseBin fromBin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_bin_id")
    private WarehouseBin toBin;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(nullable = false, length = 120)
    private String reference;

    @Column(nullable = false, length = 300)
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by_id")
    private User performedBy;

    @Column(nullable = false)
    private int previousQuantity;

    @Column(nullable = false)
    private int newQuantity;

    @Column(length = 1000)
    private String notes;
}
