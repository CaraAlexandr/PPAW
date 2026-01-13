package com.ppaw.passwordvault.controller;

import com.ppaw.passwordvault.dto.ApiResponse;
import com.ppaw.passwordvault.dto.ServicePlanDTO;
import com.ppaw.passwordvault.service.ServicePlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/service-plans")
@RequiredArgsConstructor
public class ServicePlanController {

    private final ServicePlanService servicePlanService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ServicePlanDTO>>> getAllServicePlans(
            @RequestParam(required = false) Boolean active) {
        List<ServicePlanDTO> plans = active != null && active
                ? servicePlanService.getActiveServicePlans()
                : servicePlanService.getAllServicePlans();
        
        return ResponseEntity.ok(ApiResponse.success("Service plans retrieved successfully", plans));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ServicePlanDTO>> getServicePlanById(@PathVariable Long id) {
        ServicePlanDTO plan = servicePlanService.getServicePlanById(id);
        return ResponseEntity.ok(ApiResponse.success("Service plan retrieved successfully", plan));
    }

    @GetMapping("/{id}/with-limits")
    public ResponseEntity<ApiResponse<ServicePlanDTO>> getServicePlanWithLimits(@PathVariable Long id) {
        ServicePlanDTO plan = servicePlanService.getServicePlanWithLimits(id);
        return ResponseEntity.ok(ApiResponse.success("Service plan with limits retrieved successfully", plan));
    }
}

