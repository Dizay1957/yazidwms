package com.yazidwms.auth.controller;

import com.yazidwms.auth.dto.AuthDtos;
import com.yazidwms.auth.service.AuthService;
import com.yazidwms.common.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    ResponseEntity<ApiResponse<AuthDtos.ActivationResponse>> register(@Valid @RequestBody AuthDtos.RegisterRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Registration created", authService.register(request)));
    }

    @PostMapping("/activate")
    ResponseEntity<ApiResponse<Void>> activate(@RequestParam String token) {
        authService.activate(token);
        return ResponseEntity.ok(ApiResponse.ok("User activated", null));
    }

    @PostMapping("/login")
    ResponseEntity<ApiResponse<AuthDtos.TokenResponse>> login(@Valid @RequestBody AuthDtos.LoginRequest request, HttpServletRequest servletRequest) {
        return ResponseEntity.ok(ApiResponse.ok("Login successful", authService.login(request, clientIp(servletRequest))));
    }

    @PostMapping("/refresh")
    ResponseEntity<ApiResponse<AuthDtos.TokenResponse>> refresh(@Valid @RequestBody AuthDtos.RefreshRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Token refreshed", authService.refresh(request)));
    }

    @PostMapping("/logout")
    ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody AuthDtos.LogoutRequest request, HttpServletRequest servletRequest) {
        authService.logout(request.refreshToken(), clientIp(servletRequest));
        return ResponseEntity.ok(ApiResponse.ok("Logout successful", null));
    }

    @PostMapping("/change-password")
    ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody AuthDtos.ChangePasswordRequest request) {
        authService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.ok("Password changed", null));
    }

    @PostMapping("/reset-password")
    ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody AuthDtos.ResetPasswordRequest request) {
        authService.requestPasswordReset(request);
        return ResponseEntity.ok(ApiResponse.ok("Password reset instructions sent", null));
    }

    @PostMapping("/reset-password/confirm")
    ResponseEntity<ApiResponse<Void>> confirmResetPassword(@Valid @RequestBody AuthDtos.ConfirmResetPasswordRequest request) {
        authService.confirmPasswordReset(request);
        return ResponseEntity.ok(ApiResponse.ok("Password reset confirmed", null));
    }

    private String clientIp(HttpServletRequest request) {
        var forwarded = request.getHeader("X-Forwarded-For");
        return forwarded == null || forwarded.isBlank() ? request.getRemoteAddr() : forwarded.split(",")[0].trim();
    }
}
