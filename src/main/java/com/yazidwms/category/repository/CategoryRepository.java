package com.yazidwms.category.repository;

import com.yazidwms.category.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByNameIgnoreCase(String name);
    Page<Category> findByDeletedFalse(Pageable pageable);
    Page<Category> findByDeletedFalseAndNameContainingIgnoreCase(String name, Pageable pageable);
}
