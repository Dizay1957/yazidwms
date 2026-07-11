package com.yazidwms.inventory.entity;

import com.yazidwms.common.entity.BaseEntity;
import com.yazidwms.product.entity.Product;
import com.yazidwms.warehouse.entity.WarehouseBin;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "inventories",
        uniqueConstraints = @UniqueConstraint(name = "uk_inventory_product_bin", columnNames = {"product_id", "bin_id"}),
        indexes = @Index(name = "idx_inventory_product", columnList = "product_id")
)
public class Inventory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bin_id", nullable = false)
    private WarehouseBin bin;

    private int quantity;
}
