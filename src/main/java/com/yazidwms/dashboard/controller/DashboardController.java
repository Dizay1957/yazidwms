package com.yazidwms.dashboard.controller;

import com.yazidwms.common.api.ApiResponse;
import com.yazidwms.dashboard.dto.DashboardDtos;
import com.yazidwms.dashboard.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    ResponseEntity<ApiResponse<DashboardDtos.DashboardResponse>> overview() {
        return ResponseEntity.ok(ApiResponse.ok("Dashboard fetched", dashboardService.overview()));
    }
}
