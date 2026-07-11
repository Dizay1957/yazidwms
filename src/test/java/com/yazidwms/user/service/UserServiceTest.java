package com.yazidwms.user.service;

import com.yazidwms.audit.service.AuditService;
import com.yazidwms.role.repository.RoleRepository;
import com.yazidwms.user.mapper.UserMapper;
import com.yazidwms.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class UserServiceTest {

    private final UserService userService = new UserService(
            mock(UserRepository.class),
            mock(RoleRepository.class),
            new BCryptPasswordEncoder(),
            mock(UserMapper.class),
            mock(AuditService.class)
    );

    @Test
    void validatePasswordRejectsWeakPasswords() {
        assertThatThrownBy(() -> userService.validatePassword("weak"))
                .hasMessageContaining("Password must be at least 10 characters");
    }

    @Test
    void validatePasswordAcceptsStrongPasswords() {
        assertThatCode(() -> userService.validatePassword("StrongPass1"))
                .doesNotThrowAnyException();
    }
}
