package com.yazidwms.supplier.dto;

import com.yazidwms.supplier.entity.BusinessStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public final class SupplierDtos {
    private SupplierDtos() {
    }

    public record SupplierRequest(
            @NotBlank String companyName,
            String contactName,
            @Email @NotBlank String email,
            String phone,
            String taxNumber,
            String country,
            String city,
            String address,
            BusinessStatus status
    ) {
    }

    public record SupplierResponse(Long id, String companyName, String contactName, String email, String phone, String taxNumber,
                                   String country, String city, String address, BusinessStatus status, boolean active) {
    }
}
