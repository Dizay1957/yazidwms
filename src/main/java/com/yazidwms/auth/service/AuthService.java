package com.yazidwms.auth.service;

import com.yazidwms.audit.service.AuditService;
import com.yazidwms.auth.dto.AuthDtos.ActivationResponse;
import com.yazidwms.auth.dto.AuthDtos.ChangePasswordRequest;
import com.yazidwms.auth.dto.AuthDtos.ConfirmResetPasswordRequest;
import com.yazidwms.auth.dto.AuthDtos.LoginRequest;
import com.yazidwms.auth.dto.AuthDtos.RefreshRequest;
import com.yazidwms.auth.dto.AuthDtos.RegisterRequest;
import com.yazidwms.auth.dto.AuthDtos.ResetPasswordRequest;
import com.yazidwms.auth.dto.AuthDtos.TokenResponse;
import com.yazidwms.auth.entity.ActivationToken;
import com.yazidwms.auth.entity.PasswordResetToken;
import com.yazidwms.auth.entity.RefreshToken;
import com.yazidwms.auth.repository.ActivationTokenRepository;
import com.yazidwms.auth.repository.PasswordResetTokenRepository;
import com.yazidwms.auth.repository.RefreshTokenRepository;
import com.yazidwms.exception.BusinessException;
import com.yazidwms.notification.service.NotificationService;
import com.yazidwms.role.entity.RoleName;
import com.yazidwms.role.repository.RoleRepository;
import com.yazidwms.security.JwtService;
import com.yazidwms.security.UserPrincipal;
import com.yazidwms.user.entity.User;
import com.yazidwms.user.entity.UserStatus;
import com.yazidwms.user.repository.UserRepository;
import com.yazidwms.user.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final ActivationTokenRepository activationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final NotificationService notificationService;
    private final AuditService auditService;
    private final long refreshTokenDays;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService, UserRepository userRepository,
                       RoleRepository roleRepository, RefreshTokenRepository refreshTokenRepository,
                       PasswordResetTokenRepository passwordResetTokenRepository, ActivationTokenRepository activationTokenRepository,
                       PasswordEncoder passwordEncoder, UserService userService, NotificationService notificationService,
                       AuditService auditService, @Value("${app.security.refresh-token-days}") long refreshTokenDays) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.activationTokenRepository = activationTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
        this.notificationService = notificationService;
        this.auditService = auditService;
        this.refreshTokenDays = refreshTokenDays;
    }

    @Transactional
    public ActivationResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new BusinessException("Email is already registered");
        }
        userService.validatePassword(request.password());
        var user = new User();
        user.setFullName(request.fullName());
        user.setEmail(request.email().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setStatus(UserStatus.PENDING_ACTIVATION);
        user.getRoles().add(roleRepository.findByName(RoleName.VIEWER).orElseThrow());
        userRepository.save(user);
        var token = activationToken(user);
        notificationService.userCreated(user.getEmail(), token.getToken());
        auditService.log("USER_REGISTERED", "User", user.getId(), "Self registration", null);
        return new ActivationResponse(token.getToken());
    }

    @Transactional
    public void activate(String token) {
        var activation = activationTokenRepository.findByTokenAndUsedFalse(token)
                .orElseThrow(() -> new BusinessException("Invalid activation token"));
        if (activation.getExpiresAt().isBefore(Instant.now())) {
            throw new BusinessException("Activation token expired");
        }
        activation.setUsed(true);
        activation.getUser().setStatus(UserStatus.ACTIVE);
        activation.getUser().setActive(true);
        auditService.log("USER_ACTIVATED", "User", activation.getUser().getId(), "User activated", null);
    }

    @Transactional
    public TokenResponse login(LoginRequest request, String ipAddress) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        var user = userRepository.findByEmailIgnoreCaseAndDeletedFalse(request.email()).orElseThrow();
        user.setLastLoginAt(Instant.now());
        var principal = new UserPrincipal(user);
        var refreshToken = refreshToken(user);
        auditService.log("USER_LOGIN", "User", user.getId(), "Login", ipAddress);
        return new TokenResponse(jwtService.generateAccessToken(principal), refreshToken.getToken(), "Bearer", user.getId(), user.getEmail(), principal.roleNames());
    }

    @Transactional
    public TokenResponse refresh(RefreshRequest request) {
        var stored = refreshTokenRepository.findByTokenAndRevokedFalse(request.refreshToken())
                .orElseThrow(() -> new BusinessException("Invalid refresh token"));
        if (stored.getExpiresAt().isBefore(Instant.now())) {
            stored.setRevoked(true);
            throw new BusinessException("Refresh token expired");
        }
        var principal = new UserPrincipal(stored.getUser());
        return new TokenResponse(jwtService.generateAccessToken(principal), stored.getToken(), "Bearer", stored.getUser().getId(), stored.getUser().getEmail(), principal.roleNames());
    }

    @Transactional
    public void logout(String refreshToken, String ipAddress) {
        refreshTokenRepository.findByTokenAndRevokedFalse(refreshToken).ifPresent(token -> {
            token.setRevoked(true);
            auditService.log("USER_LOGOUT", "User", token.getUser().getId(), "Logout", ipAddress);
        });
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        userService.updatePassword(new com.yazidwms.user.dto.UserDtos.PasswordUpdateRequest(request.currentPassword(), request.newPassword()));
    }

    @Transactional
    public void requestPasswordReset(ResetPasswordRequest request) {
        userRepository.findByEmailIgnoreCaseAndDeletedFalse(request.email()).ifPresent(user -> {
            var token = new PasswordResetToken();
            token.setToken(UUID.randomUUID().toString());
            token.setUser(user);
            token.setExpiresAt(Instant.now().plusSeconds(3600));
            passwordResetTokenRepository.save(token);
            notificationService.passwordReset(user.getEmail(), token.getToken());
            auditService.log("PASSWORD_RESET_REQUESTED", "User", user.getId(), "Password reset requested", null);
        });
    }

    @Transactional
    public void confirmPasswordReset(ConfirmResetPasswordRequest request) {
        var token = passwordResetTokenRepository.findByTokenAndUsedFalse(request.token())
                .orElseThrow(() -> new BusinessException("Invalid reset token"));
        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new BusinessException("Reset token expired");
        }
        userService.validatePassword(request.newPassword());
        token.setUsed(true);
        token.getUser().setPasswordHash(passwordEncoder.encode(request.newPassword()));
        auditService.log("PASSWORD_RESET_CONFIRMED", "User", token.getUser().getId(), "Password reset confirmed", null);
    }

    private RefreshToken refreshToken(User user) {
        var token = new RefreshToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(user);
        token.setExpiresAt(Instant.now().plusSeconds(refreshTokenDays * 86_400));
        return refreshTokenRepository.save(token);
    }

    private ActivationToken activationToken(User user) {
        var token = new ActivationToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(user);
        token.setExpiresAt(Instant.now().plusSeconds(86_400));
        return activationTokenRepository.save(token);
    }
}
