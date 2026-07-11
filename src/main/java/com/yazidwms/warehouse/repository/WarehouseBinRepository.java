package com.yazidwms.warehouse.repository;

import com.yazidwms.warehouse.entity.WarehouseBin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WarehouseBinRepository extends JpaRepository<WarehouseBin, Long> {
    boolean existsByCodeIgnoreCase(String code);
    Optional<WarehouseBin> findByCodeIgnoreCaseAndDeletedFalse(String code);
    List<WarehouseBin> findByShelfIdAndDeletedFalse(Long shelfId);
}
