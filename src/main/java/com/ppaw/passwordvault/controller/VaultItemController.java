package com.ppaw.passwordvault.controller;

import com.ppaw.passwordvault.dto.ApiResponse;
import com.ppaw.passwordvault.dto.VaultItemCreateDTO;
import com.ppaw.passwordvault.dto.VaultItemDTO;
import com.ppaw.passwordvault.dto.VaultItemUpdateDTO;
import com.ppaw.passwordvault.service.VaultItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/vault-items")
@RequiredArgsConstructor
public class VaultItemController {

    private static final Logger logger = LoggerFactory.getLogger(VaultItemController.class);

    private final VaultItemService vaultItemService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<VaultItemDTO>>> getAllVaultItems(
            @PathVariable Long userId,
            @RequestParam(required = false) Boolean favorite) {
        logger.info("GET /api/users/{}/vault-items - Request to get vault items (favorite: {})", userId, favorite);
        try {
            List<VaultItemDTO> items = favorite != null && favorite
                    ? vaultItemService.getFavoriteItems(userId)
                    : vaultItemService.getAllVaultItems(userId);
            logger.info("GET /api/users/{}/vault-items - Successfully retrieved {} vault items", userId, items.size());
            return ResponseEntity.ok(ApiResponse.success("Vault items retrieved successfully", items));
        } catch (Exception e) {
            logger.error("GET /api/users/{}/vault-items - Error retrieving vault items", userId, e);
            throw e;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VaultItemDTO>> getVaultItemById(
            @PathVariable Long userId,
            @PathVariable Long id) {
        logger.info("GET /api/users/{}/vault-items/{} - Request to get vault item by id", userId, id);
        try {
            VaultItemDTO item = vaultItemService.getVaultItemById(id, userId);
            logger.info("GET /api/users/{}/vault-items/{} - Successfully retrieved vault item: {}", 
                    userId, id, item.getTitle());
            return ResponseEntity.ok(ApiResponse.success("Vault item retrieved successfully", item));
        } catch (Exception e) {
            logger.error("GET /api/users/{}/vault-items/{} - Error retrieving vault item", userId, id, e);
            throw e;
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<VaultItemDTO>> createVaultItem(
            @PathVariable Long userId,
            @Valid @RequestBody VaultItemCreateDTO createDTO) {
        logger.info("POST /api/users/{}/vault-items - Request to create vault item with title: {}", 
                userId, createDTO.getTitle());
        try {
            VaultItemDTO item = vaultItemService.createVaultItem(userId, createDTO);
            logger.info("POST /api/users/{}/vault-items - Successfully created vault item: {} (id: {})", 
                    userId, item.getTitle(), item.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Vault item created successfully", item));
        } catch (Exception e) {
            logger.error("POST /api/users/{}/vault-items - Error creating vault item with title: {}", 
                    userId, createDTO.getTitle(), e);
            throw e;
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VaultItemDTO>> updateVaultItem(
            @PathVariable Long userId,
            @PathVariable Long id,
            @Valid @RequestBody VaultItemUpdateDTO updateDTO) {
        logger.info("PUT /api/users/{}/vault-items/{} - Request to update vault item", userId, id);
        try {
            VaultItemDTO item = vaultItemService.updateVaultItem(id, userId, updateDTO);
            logger.info("PUT /api/users/{}/vault-items/{} - Successfully updated vault item: {}", 
                    userId, id, item.getTitle());
            return ResponseEntity.ok(ApiResponse.success("Vault item updated successfully", item));
        } catch (Exception e) {
            logger.error("PUT /api/users/{}/vault-items/{} - Error updating vault item", userId, id, e);
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVaultItem(
            @PathVariable Long userId,
            @PathVariable Long id) {
        logger.info("DELETE /api/users/{}/vault-items/{} - Request to delete vault item", userId, id);
        try {
            vaultItemService.deleteVaultItem(id, userId);
            logger.info("DELETE /api/users/{}/vault-items/{} - Successfully deleted vault item", userId, id);
            return ResponseEntity.ok(ApiResponse.success("Vault item deleted successfully", null));
        } catch (Exception e) {
            logger.error("DELETE /api/users/{}/vault-items/{} - Error deleting vault item", userId, id, e);
            throw e;
        }
    }

    @GetMapping("/favorites")
    public ResponseEntity<ApiResponse<List<VaultItemDTO>>> getFavoriteItems(@PathVariable Long userId) {
        logger.info("GET /api/users/{}/vault-items/favorites - Request to get favorite vault items", userId);
        try {
            List<VaultItemDTO> items = vaultItemService.getFavoriteItems(userId);
            logger.info("GET /api/users/{}/vault-items/favorites - Successfully retrieved {} favorite items", 
                    userId, items.size());
            return ResponseEntity.ok(ApiResponse.success("Favorite vault items retrieved successfully", items));
        } catch (Exception e) {
            logger.error("GET /api/users/{}/vault-items/favorites - Error retrieving favorite items", userId, e);
            throw e;
        }
    }
}

