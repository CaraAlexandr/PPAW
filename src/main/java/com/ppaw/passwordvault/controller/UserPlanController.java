package com.ppaw.passwordvault.controller;

import com.ppaw.passwordvault.dto.ApiResponse;
import com.ppaw.passwordvault.dto.ServicePlanDTO;
import com.ppaw.passwordvault.service.ServicePlanService;
import com.ppaw.passwordvault.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for user plan information
 */
@RestController
@RequestMapping("/api/user/plan")
@RequiredArgsConstructor
public class UserPlanController {

    private final UserService userService;
    private final ServicePlanService servicePlanService;

    /**
     * Get current user's plan information including limits
     */
    @GetMapping
    public ResponseEntity<ApiResponse<ServicePlanDTO>> getUserPlan(HttpServletRequest request) {
        Long userId = getUserId(request);
        var user = userService.getUserById(userId);
        
        if (user.getServicePlanId() == null) {
            return ResponseEntity.ok(ApiResponse.error("User has no service plan assigned", null));
        }
        
        ServicePlanDTO plan = servicePlanService.getServicePlanWithLimits(user.getServicePlanId());
        return ResponseEntity.ok(ApiResponse.success("Plan information retrieved successfully", plan));
    }

    private Long getUserId(HttpServletRequest request) {
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj == null) {
            throw new RuntimeException("User ID not found in request");
        }
        return (Long) userIdObj;
    }
}


