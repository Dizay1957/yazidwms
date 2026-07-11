package com.yazidwms.warehouse.repository;

import com.yazidwms.warehouse.entity.WarehouseZone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WarehouseZoneRepository extends JpaRepository<WarehouseZone, Long> {
    List<WarehouseZone> findByWarehouseIdAndDeletedFalse(Long warehouseId);
}
