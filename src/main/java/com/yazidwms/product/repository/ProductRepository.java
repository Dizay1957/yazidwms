package com.yazidwms.product.repository;

import com.yazidwms.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsBySkuIgnoreCase(String sku);
    boolean existsByBarcodeIgnoreCase(String barcode);
    long countByDeletedFalse();
    long countByDeletedFalseAndQuantityLessThanEqual(int quantity);
    Page<Product> findByDeletedFalse(Pageable pageable);
    Page<Product> findByDeletedFalseAndNameContainingIgnoreCaseOrDeletedFalseAndSkuContainingIgnoreCaseOrDeletedFalseAndBarcodeContainingIgnoreCase(String name, String sku, String barcode, Pageable pageable);
    List<Product> findTop10ByDeletedFalseOrderByUpdatedAtDesc();

    @Query("select coalesce(sum(p.purchasePrice * p.quantity), 0) from Product p where p.deleted = false")
    BigDecimal inventoryValue();

    @Query("select coalesce(sum(p.quantity), 0) from Product p where p.deleted = false")
    Long inventoryCount();
}
