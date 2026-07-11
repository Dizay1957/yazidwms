package com.yazidwms.salesorder.mapper;

import com.yazidwms.salesorder.dto.SalesOrderDtos.SalesOrderItemResponse;
import com.yazidwms.salesorder.dto.SalesOrderDtos.SalesOrderResponse;
import com.yazidwms.salesorder.entity.SalesOrder;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SalesOrderMapper {
    default SalesOrderResponse toResponse(SalesOrder order) {
        return new SalesOrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomer().getId(),
                order.getCustomer().getFullName(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getConfirmedAt(),
                order.getShippedAt(),
                order.getItems().stream()
                        .map(item -> new SalesOrderItemResponse(
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
