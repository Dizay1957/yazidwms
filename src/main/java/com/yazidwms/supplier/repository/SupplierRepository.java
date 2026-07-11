package com.yazidwms.supplier.repository;

import com.yazidwms.supplier.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    boolean existsByEmailIgnoreCase(String email);
    long countByDeletedFalse();
    Page<Supplier> findByDeletedFalse(Pageable pageable);
    Page<Supplier> findByDeletedFalseAndCompanyNameContainingIgnoreCaseOrDeletedFalseAndEmailContainingIgnoreCase(String company, String email, Pageable pageable);
}
