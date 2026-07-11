package com.yazidwms.user.controller;

import com.yazidwms.common.api.ApiResponse;
import com.yazidwms.user.dto.UserDtos;
import com.yazidwms.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    ResponseEntity<ApiResponse<UserDtos.UserResponse>> me() {
        return ResponseEntity.ok(ApiResponse.ok("Profile fetched", userService.get(userService.currentUser().getId())));
    }

    @PatchMapping
    ResponseEntity<ApiResponse<UserDtos.UserResponse>> update(@Valid @RequestBody UserDtos.ProfileUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Profile updated", userService.updateProfile(request)));
    }

    @PatchMapping("/password")
    ResponseEntity<ApiResponse<Void>> password(@Valid @RequestBody UserDtos.PasswordUpdateRequest request) {
        userService.updatePassword(request);
        return ResponseEntity.ok(ApiResponse.ok("Password updated", null));
    }
}
