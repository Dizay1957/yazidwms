package com.yazidwms.purchaseorder.repository;

import com.yazidwms.purchaseorder.entity.PurchaseOrder;
import com.yazidwms.purchaseorder.entity.PurchaseOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    boolean existsByOrderNumberIgnoreCase(String orderNumber);
    Page<PurchaseOrder> findByDeletedFalse(Pageable pageable);
    long countByStatus(PurchaseOrderStatus status);

    @EntityGraph(attributePaths = {"items", "items.product", "items.bin", "supplier"})
    Optional<PurchaseOrder> findWithItemsById(Long id);

    @EntityGraph(attributePaths = {"items", "items.product", "items.bin", "supplier"})
    List<PurchaseOrder> findByDeletedFalseOrderByCreatedAtDesc();

    @Query("select p.status, count(p) from PurchaseOrder p where p.deleted = false group by p.status")
    List<Object[]> countByStatusGrouped();

    @Query("""
            select year(p.createdAt), month(p.createdAt), coalesce(sum(p.totalAmount), 0)
            from PurchaseOrder p
            where p.deleted = false and p.createdAt >= :from
            group by year(p.createdAt), month(p.createdAt)
            order by year(p.createdAt), month(p.createdAt)
            """)
    List<Object[]> sumMonthlyTotals(@Param("from") Instant from);
}
