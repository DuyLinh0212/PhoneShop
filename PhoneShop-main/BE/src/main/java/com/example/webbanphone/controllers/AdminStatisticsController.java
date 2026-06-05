package com.example.webbanphone.controllers;

import com.example.webbanphone.dto.admin.AdminStatisticsResponse;
import com.example.webbanphone.services.AdminStatisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/statistics")
public class AdminStatisticsController {

    private final AdminStatisticsService adminStatisticsService;

    public AdminStatisticsController(AdminStatisticsService adminStatisticsService) {
        this.adminStatisticsService = adminStatisticsService;
    }

    @GetMapping
    public ResponseEntity<AdminStatisticsResponse> getStatistics() {
        return ResponseEntity.ok(adminStatisticsService.getStatistics());
    }
}
