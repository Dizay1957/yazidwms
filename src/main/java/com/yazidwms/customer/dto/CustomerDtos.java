package com.yazidwms.customer.dto;

import com.yazidwms.customer.entity.CustomerType;
import com.yazidwms.supplier.entity.BusinessStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public final class CustomerDtos {
    private CustomerDtos() {
    }

    public record CustomerRequest(
            CustomerType customerType,
            String companyName,
            @NotBlank String fullName,
            @Email @NotBlank String email,
            String phone,
            String country,
            String city,
            String address,
            BusinessStatus status
    ) {
    }

    public record CustomerResponse(Long id, CustomerType customerType, String companyName, String fullName, String email, String phone,
                                   String country, String city, String address, BusinessStatus status, boolean active) {
    }
}
