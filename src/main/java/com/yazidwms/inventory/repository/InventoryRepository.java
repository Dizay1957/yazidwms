package com.yazidwms.inventory.repository;

import com.yazidwms.inventory.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    @Lock(LockModeType.OPTIMISTIC)
    Optional<Inventory> findByProductIdAndBinIdAndDeletedFalse(Long productId, Long binId);
    List<Inventory> findByProductIdAndDeletedFalse(Long productId);
    List<Inventory> findByBinIdAndDeletedFalse(Long binId);
}
