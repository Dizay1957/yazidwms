package com.yazidwms.warehouse.repository;

import com.yazidwms.warehouse.entity.Warehouse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    boolean existsByCodeIgnoreCase(String code);
    long countByDeletedFalse();
    Page<Warehouse> findByDeletedFalse(Pageable pageable);
    Page<Warehouse> findByDeletedFalseAndNameContainingIgnoreCaseOrDeletedFalseAndCodeContainingIgnoreCase(String name, String code, Pageable pageable);
}
