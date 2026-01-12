package com.ppaw.passwordvault.controller;

import com.ppaw.passwordvault.dto.ApiResponse;
import com.ppaw.passwordvault.dto.VaultItemCreateDTO;
import com.ppaw.passwordvault.dto.VaultItemDTO;
import com.ppaw.passwordvault.dto.VaultItemUpdateDTO;
import com.ppaw.passwordvault.service.VaultItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/vault-items")
@RequiredArgsConstructor
public class VaultItemController {

    private final VaultItemService vaultItemService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<VaultItemDTO>>> getAllVaultItems(
            @PathVariable Long userId,
            @RequestParam(required = false) Boolean favorite) {
        List<VaultItemDTO> items = favorite != null && favorite
                ? vaultItemService.getFavoriteItems(userId)
                : vaultItemService.getAllVaultItems(userId);
        
        return ResponseEntity.ok(ApiResponse.success("Vault items retrieved successfully", items));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VaultItemDTO>> getVaultItemById(
            @PathVariable Long userId,
            @PathVariable Long id) {
        VaultItemDTO item = vaultItemService.getVaultItemById(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Vault item retrieved successfully", item));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<VaultItemDTO>> createVaultItem(
            @PathVariable Long userId,
            @Valid @RequestBody VaultItemCreateDTO createDTO) {
        VaultItemDTO item = vaultItemService.createVaultItem(userId, createDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Vault item created successfully", item));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VaultItemDTO>> updateVaultItem(
            @PathVariable Long userId,
            @PathVariable Long id,
            @Valid @RequestBody VaultItemUpdateDTO updateDTO) {
        VaultItemDTO item = vaultItemService.updateVaultItem(id, userId, updateDTO);
        return ResponseEntity.ok(ApiResponse.success("Vault item updated successfully", item));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVaultItem(
            @PathVariable Long userId,
            @PathVariable Long id) {
        vaultItemService.deleteVaultItem(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Vault item deleted successfully", null));
    }

    @GetMapping("/favorites")
    public ResponseEntity<ApiResponse<List<VaultItemDTO>>> getFavoriteItems(@PathVariable Long userId) {
        List<VaultItemDTO> items = vaultItemService.getFavoriteItems(userId);
        return ResponseEntity.ok(ApiResponse.success("Favorite vault items retrieved successfully", items));
    }
}

