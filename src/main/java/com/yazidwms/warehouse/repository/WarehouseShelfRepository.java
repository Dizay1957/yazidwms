package com.yazidwms.warehouse.repository;

import com.yazidwms.warehouse.entity.WarehouseShelf;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WarehouseShelfRepository extends JpaRepository<WarehouseShelf, Long> {
    List<WarehouseShelf> findByAisleIdAndDeletedFalse(Long aisleId);
}
