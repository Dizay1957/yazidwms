package com.yazidwms.role.controller;

import com.yazidwms.common.api.ApiResponse;
import com.yazidwms.role.entity.Role;
import com.yazidwms.role.repository.RoleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {
    private final RoleRepository roleRepository;

    public RoleController(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<ApiResponse<List<Role>>> list() {
        return ResponseEntity.ok(ApiResponse.ok("Roles fetched", roleRepository.findAll()));
    }
}
