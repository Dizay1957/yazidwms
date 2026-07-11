package com.yazidwms.purchaseorder.repository;

import com.yazidwms.purchaseorder.entity.PurchaseOrder;
import com.yazidwms.purchaseorder.entity.PurchaseOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    boolean existsByOrderNumberIgnoreCase(String orderNumber);
    Page<PurchaseOrder> findByDeletedFalse(Pageable pageable);
    long countByStatus(PurchaseOrderStatus status);

    @EntityGraph(attributePaths = {"items", "items.product", "items.bin", "supplier"})
    Optional<PurchaseOrder> findWithItemsById(Long id);

    @Query("select p.status, count(p) from PurchaseOrder p where p.deleted = false group by p.status")
    List<Object[]> countByStatusGrouped();
}
