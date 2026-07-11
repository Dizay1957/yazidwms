package com.yazidwms.stockmovement.repository;

import com.yazidwms.stockmovement.entity.StockMovement;
import com.yazidwms.stockmovement.entity.StockMovementType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    Page<StockMovement> findByProductId(Long productId, Pageable pageable);
    List<StockMovement> findTop10ByOrderByTimestampDesc();

    @Query("select m.type, count(m) from StockMovement m group by m.type")
    List<Object[]> countByType();

    long countByType(StockMovementType type);
}
