package com.yazidwms.customer.repository;

import com.yazidwms.customer.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    boolean existsByEmailIgnoreCase(String email);
    long countByDeletedFalse();
    Page<Customer> findByDeletedFalse(Pageable pageable);
    Page<Customer> findByDeletedFalseAndFullNameContainingIgnoreCaseOrDeletedFalseAndEmailContainingIgnoreCase(String name, String email, Pageable pageable);
}
