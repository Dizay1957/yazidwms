package com.yazidwms.customer.entity;

import com.yazidwms.common.entity.BaseEntity;
import com.yazidwms.supplier.entity.BusinessStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "customers")
public class Customer extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CustomerType customerType = CustomerType.COMPANY;

    @Column(length = 160)
    private String companyName;

    @Column(nullable = false, length = 160)
    private String fullName;

    @Column(nullable = false, unique = true, length = 180)
    private String email;

    @Column(length = 40)
    private String phone;

    @Column(length = 80)
    private String country;

    @Column(length = 80)
    private String city;

    @Column(length = 300)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BusinessStatus status = BusinessStatus.ACTIVE;
}
