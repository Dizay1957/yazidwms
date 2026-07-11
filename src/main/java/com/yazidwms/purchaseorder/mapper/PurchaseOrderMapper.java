package com.yazidwms.purchaseorder.mapper;

import com.yazidwms.purchaseorder.dto.PurchaseOrderDtos.PurchaseOrderItemResponse;
import com.yazidwms.purchaseorder.dto.PurchaseOrderDtos.PurchaseOrderResponse;
import com.yazidwms.purchaseorder.entity.PurchaseOrder;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PurchaseOrderMapper {
    default PurchaseOrderResponse toResponse(PurchaseOrder order) {
        return new PurchaseOrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getSupplier().getId(),
                order.getSupplier().getCompanyName(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getConfirmedAt(),
                order.getReceivedAt(),
                order.getItems().stream()
                        .map(item -> new PurchaseOrderItemResponse(
                                item.getProduct().getId(),
                                item.getProduct().getSku(),
                                item.getBin().getId(),
                                item.getBin().getCode(),
                                item.getQuantity(),
                                item.getUnitPrice(),
                                item.getLineTotal()))
                        .toList()
        );
    }
}
