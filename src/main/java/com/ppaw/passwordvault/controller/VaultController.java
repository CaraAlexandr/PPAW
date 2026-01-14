package com.ppaw.passwordvault.controller;

import com.ppaw.passwordvault.dto.ApiResponse;
import com.ppaw.passwordvault.dto.VaultItemCreateDTO;
import com.ppaw.passwordvault.dto.VaultItemDTO;
import com.ppaw.passwordvault.dto.VaultItemUpdateDTO;
import com.ppaw.passwordvault.service.VaultItemService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * LAB 8: Vault API endpoints secured by user authentication
 * All endpoints filter items by the authenticated user's ID
 */
@RestController
@RequestMapping("/api/vault")
@RequiredArgsConstructor
public class VaultController {

    private final VaultItemService vaultItemService;

    /**
     * Get all vault items for the authenticated user
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<VaultItemDTO>>> getAllVaultItems(HttpServletRequest request) {
        Long userId = getUserId(request);
        List<VaultItemDTO> items = vaultItemService.getAllVaultItems(userId);
        return ResponseEntity.ok(ApiResponse.success("Vault items retrieved successfully", items));
    }

    /**
     * Get a specific vault item by ID (only if it belongs to the authenticated user)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VaultItemDTO>> getVaultItemById(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = getUserId(request);
        VaultItemDTO item = vaultItemService.getVaultItemById(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Vault item retrieved successfully", item));
    }

    /**
     * Create a new vault item for the authenticated user
     */
    @PostMapping
    public ResponseEntity<ApiResponse<VaultItemDTO>> createVaultItem(
            @Valid @RequestBody VaultItemCreateDTO createDTO,
            HttpServletRequest request) {
        Long userId = getUserId(request);
        VaultItemDTO item = vaultItemService.createVaultItem(userId, createDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Vault item created successfully", item));
    }

    /**
     * Update a vault item (only if it belongs to the authenticated user)
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VaultItemDTO>> updateVaultItem(
            @PathVariable Long id,
            @Valid @RequestBody VaultItemUpdateDTO updateDTO,
            HttpServletRequest request) {
        Long userId = getUserId(request);
        VaultItemDTO item = vaultItemService.updateVaultItem(id, userId, updateDTO);
        return ResponseEntity.ok(ApiResponse.success("Vault item updated successfully", item));
    }

    /**
     * Delete a vault item (only if it belongs to the authenticated user)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVaultItem(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = getUserId(request);
        vaultItemService.deleteVaultItem(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Vault item deleted successfully", null));
    }

    /**
     * Extract userId from request attribute (set by AuthInterceptor)
     */
    private Long getUserId(HttpServletRequest request) {
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj == null) {
            throw new RuntimeException("User ID not found in request");
        }
        return (Long) userIdObj;
    }
}

