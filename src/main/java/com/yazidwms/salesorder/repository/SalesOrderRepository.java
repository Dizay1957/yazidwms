package com.yazidwms.salesorder.repository;

import com.yazidwms.salesorder.entity.SalesOrder;
import com.yazidwms.salesorder.entity.SalesOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {
    boolean existsByOrderNumberIgnoreCase(String orderNumber);
    Page<SalesOrder> findByDeletedFalse(Pageable pageable);
    long countByStatus(SalesOrderStatus status);

    @EntityGraph(attributePaths = {"items", "items.product", "items.bin", "customer"})
    Optional<SalesOrder> findWithItemsById(Long id);

    @Query("select s.status, count(s) from SalesOrder s where s.deleted = false group by s.status")
    List<Object[]> countByStatusGrouped();
}
