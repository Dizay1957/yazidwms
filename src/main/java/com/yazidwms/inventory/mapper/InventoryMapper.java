package com.yazidwms.inventory.mapper;

import com.yazidwms.inventory.dto.InventoryDtos.InventoryResponse;
import com.yazidwms.inventory.dto.InventoryDtos.StockMovementResponse;
import com.yazidwms.inventory.entity.Inventory;
import com.yazidwms.stockmovement.entity.StockMovement;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InventoryMapper {
    default InventoryResponse toResponse(Inventory inventory) {
        return new InventoryResponse(
                inventory.getId(),
                inventory.getProduct().getId(),
                inventory.getProduct().getSku(),
                inventory.getProduct().getName(),
                inventory.getBin().getId(),
                inventory.getBin().getCode(),
                inventory.getQuantity()
        );
    }

    default StockMovementResponse toResponse(StockMovement movement) {
        return new StockMovementResponse(
                movement.getId(),
                movement.getType(),
                movement.getProduct().getId(),
                movement.getProduct().getSku(),
                movement.getFromBin() == null ? null : movement.getFromBin().getId(),
                movement.getToBin() == null ? null : movement.getToBin().getId(),
                movement.getQuantity(),
                movement.getTimestamp(),
                movement.getReference(),
                movement.getReason(),
                movement.getPreviousQuantity(),
                movement.getNewQuantity(),
                movement.getNotes()
        );
    }
}
