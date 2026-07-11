package com.yazidwms.customer.mapper;

import com.yazidwms.customer.dto.CustomerDtos.CustomerResponse;
import com.yazidwms.customer.entity.Customer;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    default CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getCustomerType(),
                customer.getCompanyName(),
                customer.getFullName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getCountry(),
                customer.getCity(),
                customer.getAddress(),
                customer.getStatus(),
                customer.isActive()
        );
    }
}
