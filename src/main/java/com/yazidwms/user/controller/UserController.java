package com.yazidwms.user.controller;

import com.yazidwms.common.api.ApiResponse;
import com.yazidwms.common.api.PageResponse;
import com.yazidwms.user.dto.UserDtos;
import com.yazidwms.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<ApiResponse<UserDtos.UserResponse>> create(@Valid @RequestBody UserDtos.UserRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("User created", userService.create(request)));
    }

    @GetMapping
    ResponseEntity<ApiResponse<PageResponse<UserDtos.UserResponse>>> search(@RequestParam(required = false) String q, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok("Users fetched", userService.search(q, pageable)));
    }

    @GetMapping("/{id}")
    ResponseEntity<ApiResponse<UserDtos.UserResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("User fetched", userService.get(id)));
    }

    @PutMapping("/{id}")
    ResponseEntity<ApiResponse<UserDtos.UserResponse>> update(@PathVariable Long id, @Valid @RequestBody UserDtos.UserUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("User updated", userService.update(id, request)));
    }

    @PatchMapping("/{id}/disable")
    ResponseEntity<ApiResponse<Void>> disable(@PathVariable Long id) {
        userService.disable(id);
        return ResponseEntity.ok(ApiResponse.ok("User disabled", null));
    }

    @PatchMapping("/{id}/enable")
    ResponseEntity<ApiResponse<Void>> enable(@PathVariable Long id) {
        userService.enable(id);
        return ResponseEntity.ok(ApiResponse.ok("User enabled", null));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        userService.softDelete(id);
        return ResponseEntity.ok(ApiResponse.ok("User deleted", null));
    }
}
