package com.yazidwms.stockmovement.repository;

import com.yazidwms.stockmovement.entity.StockMovement;
import com.yazidwms.stockmovement.entity.StockMovementType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    Page<StockMovement> findByProductId(Long productId, Pageable pageable);
    List<StockMovement> findTop10ByOrderByTimestampDesc();

    @EntityGraph(attributePaths = {"product", "fromBin", "toBin", "performedBy"})
    List<StockMovement> findByDeletedFalseOrderByTimestampDesc();

    @Query("select m.type, count(m) from StockMovement m group by m.type")
    List<Object[]> countByType();

    @Query("""
            select year(m.timestamp), month(m.timestamp), count(m)
            from StockMovement m
            where m.deleted = false and m.timestamp >= :from
            group by year(m.timestamp), month(m.timestamp)
            order by year(m.timestamp), month(m.timestamp)
            """)
    List<Object[]> countMonthlyActivity(@Param("from") Instant from);

    long countByType(StockMovementType type);
}
