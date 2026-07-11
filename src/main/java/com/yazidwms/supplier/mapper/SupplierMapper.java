package com.yazidwms.supplier.mapper;

import com.yazidwms.supplier.dto.SupplierDtos.SupplierResponse;
import com.yazidwms.supplier.entity.Supplier;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SupplierMapper {
    default SupplierResponse toResponse(Supplier supplier) {
        return new SupplierResponse(
                supplier.getId(),
                supplier.getCompanyName(),
                supplier.getContactName(),
                supplier.getEmail(),
                supplier.getPhone(),
                supplier.getTaxNumber(),
                supplier.getCountry(),
                supplier.getCity(),
                supplier.getAddress(),
                supplier.getStatus(),
                supplier.isActive()
        );
    }
}
