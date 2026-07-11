package com.yazidwms.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public final class AuthDtos {
    private AuthDtos() {
    }

    public record RegisterRequest(
            @NotBlank String fullName,
            @Email @NotBlank String email,
            @Size(min = 10, message = "Password must contain at least 10 characters") String password
    ) {
    }

    public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {
    }

    public record TokenResponse(String accessToken, String refreshToken, String tokenType, Long userId, String email, Set<String> roles) {
    }

    public record RefreshRequest(@NotBlank String refreshToken) {
    }

    public record LogoutRequest(@NotBlank String refreshToken) {
    }

    public record ChangePasswordRequest(@NotBlank String currentPassword, @Size(min = 10) String newPassword) {
    }

    public record ResetPasswordRequest(@Email @NotBlank String email) {
    }

    public record ConfirmResetPasswordRequest(@NotBlank String token, @Size(min = 10) String newPassword) {
    }

    public record ActivationResponse(String activationToken) {
    }
}
