package com.yazidwms.auth.service;

import com.yazidwms.audit.service.AuditService;
import com.yazidwms.auth.dto.AuthDtos.RegisterRequest;
import com.yazidwms.auth.repository.ActivationTokenRepository;
import com.yazidwms.auth.repository.PasswordResetTokenRepository;
import com.yazidwms.auth.repository.RefreshTokenRepository;
import com.yazidwms.notification.service.NotificationService;
import com.yazidwms.role.entity.Role;
import com.yazidwms.role.entity.RoleName;
import com.yazidwms.role.repository.RoleRepository;
import com.yazidwms.security.JwtService;
import com.yazidwms.user.entity.UserStatus;
import com.yazidwms.user.repository.UserRepository;
import com.yazidwms.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    @Test
    void registerCreatesPendingViewerAndActivationToken() {
        var userRepository = mock(UserRepository.class);
        var roleRepository = mock(RoleRepository.class);
        var activationTokenRepository = mock(ActivationTokenRepository.class);
        var notificationService = mock(NotificationService.class);
        var userService = mock(UserService.class);
        var viewer = new Role(RoleName.VIEWER, "Viewer role");

        when(userRepository.existsByEmailIgnoreCase("viewer@yazidwms.local")).thenReturn(false);
        when(roleRepository.findByName(RoleName.VIEWER)).thenReturn(Optional.of(viewer));
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(activationTokenRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var service = new AuthService(
                mock(AuthenticationManager.class),
                mock(JwtService.class),
                userRepository,
                roleRepository,
                mock(RefreshTokenRepository.class),
                mock(PasswordResetTokenRepository.class),
                activationTokenRepository,
                new BCryptPasswordEncoder(),
                userService,
                notificationService,
                mock(AuditService.class),
                7
        );

        var response = service.register(new RegisterRequest("Viewer User", "viewer@yazidwms.local", "ViewerPass1"));

        assertThat(response.activationToken()).isNotBlank();
        verify(userRepository).save(org.mockito.ArgumentMatchers.argThat(user ->
                user.getStatus() == UserStatus.PENDING_ACTIVATION && user.getRoles().contains(viewer)));
        verify(notificationService).userCreated(org.mockito.ArgumentMatchers.eq("viewer@yazidwms.local"), org.mockito.ArgumentMatchers.anyString());
    }
}
