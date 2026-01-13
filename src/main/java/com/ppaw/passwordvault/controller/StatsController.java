package com.ppaw.passwordvault.controller;

import com.ppaw.passwordvault.dto.ApiResponse;
import com.ppaw.passwordvault.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final UserRepository userRepository;
    private final ServicePlanRepository servicePlanRepository;
    private final VaultItemRepository vaultItemRepository;
    private final AuditLogRepository auditLogRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Long>>> getStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalServicePlans", servicePlanRepository.count());
        stats.put("totalVaultItems", vaultItemRepository.count());
        stats.put("totalAuditLogs", auditLogRepository.count());
        stats.put("activeUsers", userRepository.count());
        
        return ResponseEntity.ok(ApiResponse.success("Statistics retrieved successfully", stats));
    }
}

