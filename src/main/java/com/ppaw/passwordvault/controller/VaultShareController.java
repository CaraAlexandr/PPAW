package com.ppaw.passwordvault.controller;

import com.ppaw.passwordvault.dto.ApiResponse;
import com.ppaw.passwordvault.dto.ServicePlanDTO;
import com.ppaw.passwordvault.dto.UserDTO;
import com.ppaw.passwordvault.exception.ResourceNotFoundException;
import com.ppaw.passwordvault.exception.ValidationException;
import com.ppaw.passwordvault.model.SharedVaultItem;
import com.ppaw.passwordvault.model.User;
import com.ppaw.passwordvault.model.VaultItem;
import com.ppaw.passwordvault.repository.SharedVaultItemRepository;
import com.ppaw.passwordvault.repository.UserRepository;
import com.ppaw.passwordvault.repository.VaultItemRepository;
import com.ppaw.passwordvault.service.ServicePlanService;
import com.ppaw.passwordvault.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for vault sharing functionality
 */
@RestController
@RequestMapping("/api/vault/share")
@RequiredArgsConstructor
public class VaultShareController {

    private final VaultItemRepository vaultItemRepository;
    private final UserRepository userRepository;
    private final SharedVaultItemRepository sharedVaultItemRepository;
    private final UserService userService;
    private final ServicePlanService servicePlanService;

    @Data
    public static class ShareRequest {
        private Long vaultItemId;
        private String sharedWithUsernameOrEmail;
        private Boolean canEdit = false;
    }

    /**
     * Share a vault item with another user (only if plan allows sharing)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<String>> shareVaultItem(
            @RequestBody ShareRequest shareRequest,
            HttpServletRequest request) {
        Long userId = getUserId(request);
        
        // Check if user's plan allows sharing
        var user = userService.getUserById(userId);
        if (user.getServicePlanId() == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User has no service plan assigned", null));
        }
        
        ServicePlanDTO plan = servicePlanService.getServicePlanWithLimits(user.getServicePlanId());
        if (plan.getLimits() == null || !plan.getLimits().getCanShare()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Sharing is not available for your plan. Please upgrade to Premium.", null));
        }
        
        // Get vault item
        VaultItem vaultItem = vaultItemRepository.findById(shareRequest.getVaultItemId())
                .orElseThrow(() -> new ResourceNotFoundException("VaultItem", shareRequest.getVaultItemId()));
        
        if (!vaultItem.getUser().getId().equals(userId)) {
            throw new ValidationException("Vault item does not belong to you");
        }
        
        // Find user to share with
        User sharedWithUser = userRepository.findByUsernameOrEmail(shareRequest.getSharedWithUsernameOrEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User with username/email " + shareRequest.getSharedWithUsernameOrEmail() + " not found"));
        
        if (sharedWithUser.getId().equals(userId)) {
            throw new ValidationException("Cannot share with yourself");
        }
        
        // Check if already shared
        if (sharedVaultItemRepository.findByVaultItemIdAndSharedWithUserId(
                shareRequest.getVaultItemId(), sharedWithUser.getId()).isPresent()) {
            throw new ValidationException("Vault item is already shared with this user");
        }
        
        // Create share
        SharedVaultItem share = new SharedVaultItem();
        share.setVaultItem(vaultItem);
        share.setSharedByUser(userRepository.findById(userId).orElseThrow());
        share.setSharedWithUser(sharedWithUser);
        share.setCanEdit(shareRequest.getCanEdit() != null ? shareRequest.getCanEdit() : false);
        
        sharedVaultItemRepository.save(share);
        
        return ResponseEntity.ok(ApiResponse.success(
                "Vault item shared successfully with " + sharedWithUser.getUsername(), null));
    }

    /**
     * Get items shared with current user
     */
    @GetMapping("/received")
    public ResponseEntity<ApiResponse<List<SharedVaultItemDTO>>> getSharedWithMe(HttpServletRequest request) {
        Long userId = getUserId(request);
        
        List<SharedVaultItem> shared = sharedVaultItemRepository.findBySharedWithUserId(userId);
        
        List<SharedVaultItemDTO> dtos = shared.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success("Shared items retrieved successfully", dtos));
    }

    @Data
    public static class SharedVaultItemDTO {
        private Long id;
        private Long vaultItemId;
        private String vaultItemTitle;
        private String sharedByUsername;
        private Boolean canEdit;
        private String createdAt;
    }

    private SharedVaultItemDTO toDTO(SharedVaultItem share) {
        SharedVaultItemDTO dto = new SharedVaultItemDTO();
        dto.setId(share.getId());
        dto.setVaultItemId(share.getVaultItem().getId());
        dto.setVaultItemTitle(share.getVaultItem().getTitle());
        dto.setSharedByUsername(share.getSharedByUser().getUsername());
        dto.setCanEdit(share.getCanEdit());
        dto.setCreatedAt(share.getCreatedAt().toString());
        return dto;
    }

    private Long getUserId(HttpServletRequest request) {
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj == null) {
            throw new RuntimeException("User ID not found in request");
        }
        return (Long) userIdObj;
    }
}

