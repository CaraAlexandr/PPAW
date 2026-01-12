package com.ppaw.passwordvault.service;

import com.ppaw.passwordvault.dto.VaultItemCreateDTO;
import com.ppaw.passwordvault.dto.VaultItemDTO;
import com.ppaw.passwordvault.dto.VaultItemUpdateDTO;
import com.ppaw.passwordvault.exception.BusinessException;
import com.ppaw.passwordvault.exception.ResourceNotFoundException;
import com.ppaw.passwordvault.exception.ValidationException;
import com.ppaw.passwordvault.model.User;
import com.ppaw.passwordvault.model.VaultItem;
import com.ppaw.passwordvault.repository.UserRepository;
import com.ppaw.passwordvault.repository.VaultItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class VaultItemService {

    private final VaultItemRepository vaultItemRepository;
    private final UserRepository userRepository;
    private final ServicePlanService servicePlanService;
    private final AuditLogService auditLogService;

    public List<VaultItemDTO> getAllVaultItems(Long userId) {
        return vaultItemRepository.findByUserId(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public VaultItemDTO getVaultItemById(Long id, Long userId) {
        VaultItem item = vaultItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VaultItem", id));
        
        if (!item.getUser().getId().equals(userId)) {
            throw new ValidationException("Vault item does not belong to this user");
        }
        
        return toDTO(item);
    }

    public VaultItemDTO createVaultItem(Long userId, VaultItemCreateDTO createDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // Verificare limită plan
        var planDTO = servicePlanService.getServicePlanWithLimits(user.getServicePlan().getId());
        long itemCount = vaultItemRepository.findByUserId(userId).size();
        
        if (planDTO.getLimits() != null && itemCount >= planDTO.getLimits().getMaxVaultItems()) {
            throw new BusinessException("Maximum vault items limit reached for your plan");
        }

        VaultItem item = new VaultItem();
        item.setUser(user);
        item.setTitle(createDTO.getTitle());
        item.setUsername(createDTO.getUsername());
        item.setUrl(createDTO.getUrl());
        item.setNotes(createDTO.getNotes());
        item.setFolder(createDTO.getFolder());
        item.setTags(createDTO.getTags());
        item.setIsFavorite(createDTO.getIsFavorite() != null ? createDTO.getIsFavorite() : false);

        // Simulare criptare (în producție folosești AES sau similar)
        item.setEncryptedPassword("encrypted_" + createDTO.getPassword());
        item.setPasswordIv("iv_" + System.currentTimeMillis());
        item.setPasswordSalt("salt_" + System.currentTimeMillis());

        VaultItem saved = vaultItemRepository.save(item);
        auditLogService.logAction(userId, "CREATE_VAULT_ITEM", 
                "Created vault item: " + saved.getTitle(), null);
        
        return toDTO(saved);
    }

    public VaultItemDTO updateVaultItem(Long id, Long userId, VaultItemUpdateDTO updateDTO) {
        VaultItem item = vaultItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VaultItem", id));
        
        if (!item.getUser().getId().equals(userId)) {
            throw new ValidationException("Vault item does not belong to this user");
        }

        if (updateDTO.getTitle() != null) {
            item.setTitle(updateDTO.getTitle());
        }
        if (updateDTO.getUsername() != null) {
            item.setUsername(updateDTO.getUsername());
        }
        if (updateDTO.getPassword() != null) {
            item.setEncryptedPassword("encrypted_" + updateDTO.getPassword());
            item.setPasswordIv("iv_" + System.currentTimeMillis());
            item.setPasswordSalt("salt_" + System.currentTimeMillis());
        }
        if (updateDTO.getUrl() != null) {
            item.setUrl(updateDTO.getUrl());
        }
        if (updateDTO.getNotes() != null) {
            item.setNotes(updateDTO.getNotes());
        }
        if (updateDTO.getFolder() != null) {
            item.setFolder(updateDTO.getFolder());
        }
        if (updateDTO.getTags() != null) {
            item.setTags(updateDTO.getTags());
        }
        if (updateDTO.getIsFavorite() != null) {
            item.setIsFavorite(updateDTO.getIsFavorite());
        }

        VaultItem updated = vaultItemRepository.save(item);
        auditLogService.logAction(userId, "UPDATE_VAULT_ITEM", 
                "Updated vault item: " + updated.getTitle(), null);
        
        return toDTO(updated);
    }

    public void deleteVaultItem(Long id, Long userId) {
        VaultItem item = vaultItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VaultItem", id));
        
        if (!item.getUser().getId().equals(userId)) {
            throw new ValidationException("Vault item does not belong to this user");
        }

        String title = item.getTitle();
        vaultItemRepository.delete(item);
        auditLogService.logAction(userId, "DELETE_VAULT_ITEM", 
                "Deleted vault item: " + title, null);
    }

    public List<VaultItemDTO> getFavoriteItems(Long userId) {
        return vaultItemRepository.findByUserIdAndIsFavorite(userId, true).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private VaultItemDTO toDTO(VaultItem item) {
        return VaultItemDTO.builder()
                .id(item.getId())
                .userId(item.getUser().getId())
                .title(item.getTitle())
                .username(item.getUsername())
                .url(item.getUrl())
                .notes(item.getNotes())
                .folder(item.getFolder())
                .tags(item.getTags())
                .isFavorite(item.getIsFavorite())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}

