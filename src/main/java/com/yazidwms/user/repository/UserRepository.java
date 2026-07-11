package com.yazidwms.user.repository;

import com.yazidwms.user.entity.User;
import com.yazidwms.user.entity.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCaseAndDeletedFalse(String email);
    boolean existsByEmailIgnoreCase(String email);
    long countByStatusAndDeletedFalse(UserStatus status);
    Page<User> findByDeletedFalseAndFullNameContainingIgnoreCaseOrDeletedFalseAndEmailContainingIgnoreCase(String name, String email, Pageable pageable);
    Page<User> findByDeletedFalse(Pageable pageable);
}
