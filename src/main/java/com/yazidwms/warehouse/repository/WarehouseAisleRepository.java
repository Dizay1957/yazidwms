package com.yazidwms.warehouse.repository;

import com.yazidwms.warehouse.entity.WarehouseAisle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WarehouseAisleRepository extends JpaRepository<WarehouseAisle, Long> {
    List<WarehouseAisle> findByZoneIdAndDeletedFalse(Long zoneId);
}
