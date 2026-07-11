package com.yazidwms.user.dto;

import com.yazidwms.role.entity.RoleName;
import com.yazidwms.user.entity.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.Set;

public final class UserDtos {
    private UserDtos() {
    }

    public record UserRequest(
            @NotBlank String fullName,
            @Email @NotBlank String email,
            @Size(min = 10) String password,
            String phone,
            @NotEmpty Set<RoleName> roles
    ) {
    }

    public record UserUpdateRequest(@NotBlank String fullName, String phone, @NotEmpty Set<RoleName> roles, UserStatus status) {
    }

    public record ProfileUpdateRequest(@NotBlank String fullName, String phone) {
    }

    public record PasswordUpdateRequest(@NotBlank String currentPassword, @Size(min = 10) String newPassword) {
    }

    public record UserResponse(
            Long id,
            String fullName,
            String email,
            String phone,
            UserStatus status,
            boolean active,
            Instant lastLoginAt,
            Set<String> roles,
            Instant createdAt,
            Instant updatedAt
    ) {
    }
}
