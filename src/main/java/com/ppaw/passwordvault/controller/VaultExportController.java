package com.ppaw.passwordvault.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ppaw.passwordvault.dto.ApiResponse;
import com.ppaw.passwordvault.dto.ServicePlanDTO;
import com.ppaw.passwordvault.dto.VaultItemDTO;
import com.ppaw.passwordvault.service.ServicePlanService;
import com.ppaw.passwordvault.service.UserService;
import com.ppaw.passwordvault.service.VaultItemService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for vault export functionality
 */
@RestController
@RequestMapping("/api/vault/export")
@RequiredArgsConstructor
public class VaultExportController {

    private static final Logger logger = LoggerFactory.getLogger(VaultExportController.class);

    private final VaultItemService vaultItemService;
    private final UserService userService;
    private final ServicePlanService servicePlanService;
    private final ObjectMapper objectMapper;

    /**
     * Export all vault items as JSON (only if plan allows export)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> exportVault(HttpServletRequest request) {
        Long userId = getUserId(request);
        
        // Check if user's plan allows export
        var user = userService.getUserById(userId);
        if (user.getServicePlanId() == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User has no service plan assigned", null));
        }
        
        ServicePlanDTO plan = servicePlanService.getServicePlanWithLimits(user.getServicePlanId());
        if (plan.getLimits() == null || !plan.getLimits().getCanExport()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Export is not available for your plan. Please upgrade to Usual or Premium.", null));
        }
        
        List<VaultItemDTO> items = vaultItemService.getAllVaultItems(userId);
        
        Map<String, Object> exportData = new HashMap<>();
        exportData.put("userId", userId);
        exportData.put("username", user.getUsername());
        exportData.put("exportDate", java.time.LocalDateTime.now().toString());
        exportData.put("itemCount", items.size());
        exportData.put("items", items);
        
        return ResponseEntity.ok(ApiResponse.success("Vault exported successfully", exportData));
    }

    /**
     * Export vault as downloadable JSON file
     */
    @GetMapping("/download")
    public ResponseEntity<String> downloadVault(HttpServletRequest request) {
        logger.info("GET /api/vault/export/download - Request to download vault export");
        try {
            Long userId = getUserId(request);
            
            // Check if user's plan allows export
            var user = userService.getUserById(userId);
            if (user.getServicePlanId() == null) {
                logger.warn("Export failed: User {} has no service plan assigned", userId);
                String errorJson = objectMapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(ApiResponse.error("User has no service plan assigned", null));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(errorJson);
            }
            
            ServicePlanDTO plan = servicePlanService.getServicePlanWithLimits(user.getServicePlanId());
            if (plan.getLimits() == null || !plan.getLimits().getCanExport()) {
                logger.warn("Export failed: Export not available for user {} plan", userId);
                String errorJson = objectMapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(ApiResponse.error("Export is not available for your plan. Please upgrade to Usual or Premium.", null));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(errorJson);
            }
            
            List<VaultItemDTO> items = vaultItemService.getAllVaultItems(userId);
            
            Map<String, Object> exportData = new HashMap<>();
            exportData.put("userId", userId);
            exportData.put("username", user.getUsername());
            exportData.put("exportDate", java.time.LocalDateTime.now().toString());
            exportData.put("itemCount", items.size());
            exportData.put("items", items);
            
            // Convert to JSON using ObjectMapper with pretty printing
            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(exportData);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setContentDispositionFormData("attachment", "vault-export.json");
            
            logger.info("GET /api/vault/export/download - Successfully exported {} items for user {}", items.size(), userId);
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(json);
        } catch (Exception e) {
            logger.error("GET /api/vault/export/download - Error exporting vault", e);
            try {
                // Serialize error response to JSON string
                String errorJson = objectMapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(ApiResponse.error("Failed to export vault: " + e.getMessage(), null));
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(errorJson);
            } catch (Exception jsonException) {
                logger.error("Failed to serialize error response to JSON", jsonException);
                // Fallback to plain text if JSON serialization fails
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.TEXT_PLAIN)
                        .body("Failed to export vault: " + e.getMessage());
            }
        }
    }

    private Long getUserId(HttpServletRequest request) {
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj == null) {
            throw new RuntimeException("User ID not found in request");
        }
        return (Long) userIdObj;
    }

}


