package com.ppaw.passwordvault.controller;

import com.ppaw.passwordvault.dto.ApiResponse;
import com.ppaw.passwordvault.dto.ServicePlanDTO;
import com.ppaw.passwordvault.dto.VaultItemCreateDTO;
import com.ppaw.passwordvault.dto.VaultItemDTO;
import com.ppaw.passwordvault.service.ServicePlanService;
import com.ppaw.passwordvault.service.UserService;
import com.ppaw.passwordvault.service.VaultItemService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Controller for vault import functionality
 */
@RestController
@RequestMapping("/api/vault/import")
@RequiredArgsConstructor
public class VaultImportController {

    private final VaultItemService vaultItemService;
    private final UserService userService;
    private final ServicePlanService servicePlanService;

    @Data
    public static class ImportRequest {
        private List<Map<String, Object>> items;
    }

    /**
     * Import vault items from JSON (only if plan allows import)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ImportResult>> importVault(
            @RequestBody ImportRequest importRequest,
            HttpServletRequest request) {
        Long userId = getUserId(request);
        
        // Check if user's plan allows import
        var user = userService.getUserById(userId);
        if (user.getServicePlanId() == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User has no service plan assigned", null));
        }
        
        ServicePlanDTO plan = servicePlanService.getServicePlanWithLimits(user.getServicePlanId());
        if (plan.getLimits() == null || !plan.getLimits().getCanImport()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Import is not available for your plan. Please upgrade to Premium.", null));
        }
        
        List<VaultItemDTO> imported = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        for (Map<String, Object> itemData : importRequest.getItems()) {
            try {
                VaultItemCreateDTO createDTO = new VaultItemCreateDTO();
                createDTO.setTitle((String) itemData.get("title"));
                createDTO.setUsername((String) itemData.get("username"));
                createDTO.setPassword((String) itemData.get("password"));
                createDTO.setUrl((String) itemData.get("url"));
                createDTO.setNotes((String) itemData.get("notes"));
                createDTO.setFolder((String) itemData.get("folder"));
                createDTO.setTags((String) itemData.get("tags"));
                
                VaultItemDTO created = vaultItemService.createVaultItem(userId, createDTO);
                imported.add(created);
            } catch (Exception e) {
                errors.add("Failed to import item: " + itemData.get("title") + " - " + e.getMessage());
            }
        }
        
        ImportResult result = new ImportResult();
        result.setImportedCount(imported.size());
        result.setErrorCount(errors.size());
        result.setErrors(errors);
        
        return ResponseEntity.ok(ApiResponse.success("Import completed", result));
    }

    @Data
    public static class ImportResult {
        private int importedCount;
        private int errorCount;
        private List<String> errors;
    }

    private Long getUserId(HttpServletRequest request) {
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj == null) {
            throw new RuntimeException("User ID not found in request");
        }
        return (Long) userIdObj;
    }
}


