package com.ppaw.passwordvault.service;

import com.ppaw.passwordvault.dto.VaultItemCreateDTO;
import com.ppaw.passwordvault.dto.VaultItemDTO;
import com.ppaw.passwordvault.dto.VaultItemUpdateDTO;
import com.ppaw.passwordvault.exception.BusinessException;
import com.ppaw.passwordvault.exception.ResourceNotFoundException;
import com.ppaw.passwordvault.exception.ValidationException;
import com.ppaw.passwordvault.model.PasswordHistory;
import com.ppaw.passwordvault.model.User;
import com.ppaw.passwordvault.model.VaultItem;
import com.ppaw.passwordvault.repository.PasswordHistoryRepository;
import com.ppaw.passwordvault.repository.UserRepository;
import com.ppaw.passwordvault.repository.VaultItemRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class VaultItemService {

    private static final Logger logger = LoggerFactory.getLogger(VaultItemService.class);

    private final VaultItemRepository vaultItemRepository;
    private final UserRepository userRepository;
    private final ServicePlanService servicePlanService;
    private final AuditLogService auditLogService;
    private final PasswordHistoryRepository passwordHistoryRepository;

    public List<VaultItemDTO> getAllVaultItems(Long userId) {
        logger.info("Getting all vault items for user id: {}", userId);
        try {
            List<VaultItemDTO> items = vaultItemRepository.findByUserId(userId).stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
            logger.info("Successfully retrieved {} vault items for user id: {}", items.size(), userId);
            return items;
        } catch (Exception e) {
            logger.error("Error on getting vault items from database for user id: {}", userId, e);
            throw e;
        }
    }

    public VaultItemDTO getVaultItemById(Long id, Long userId) {
        logger.debug("Getting vault item by id: {} for user id: {}", id, userId);
        try {
            VaultItem item = vaultItemRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.warn("Vault item not found with id: {}", id);
                        return new ResourceNotFoundException("VaultItem", id);
                    });
            
            if (!item.getUser().getId().equals(userId)) {
                logger.warn("Vault item {} does not belong to user {}", id, userId);
                throw new ValidationException("Vault item does not belong to this user");
            }
            
            logger.info("Successfully retrieved vault item: {} (id: {}) for user id: {}", item.getTitle(), id, userId);
            return toDTO(item);
        } catch (ValidationException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error on getting vault item by id: {} for user id: {}", id, userId, e);
            throw e;
        }
    }

    public VaultItemDTO createVaultItem(Long userId, VaultItemCreateDTO createDTO) {
        logger.info("Creating new vault item for user id: {} with title: {}", userId, createDTO.getTitle());
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        logger.error("User not found with id: {}", userId);
                        return new ResourceNotFoundException("User", userId);
                    });

            // Get plan limits
            var planDTO = servicePlanService.getServicePlanWithLimits(user.getServicePlan().getId());
            var limits = planDTO.getLimits();
            
            if (limits == null) {
                logger.error("Service plan limits not found for user id: {}", userId);
                throw new BusinessException("Service plan limits not found");
            }
            
            // Validate max vault items limit
            long itemCount = vaultItemRepository.findByUserId(userId).size();
            if (itemCount >= limits.getMaxVaultItems()) {
                logger.warn("Maximum vault items limit ({}) reached for user id: {}", limits.getMaxVaultItems(), userId);
                throw new BusinessException(String.format("Maximum vault items limit (%d) reached for your plan. Please upgrade to add more items.", 
                        limits.getMaxVaultItems()));
            }

            // Validate password length limit
            if (createDTO.getPassword() != null && createDTO.getPassword().length() > limits.getMaxPasswordLength()) {
                logger.warn("Password length exceeds maximum allowed ({}) for user id: {}", limits.getMaxPasswordLength(), userId);
                throw new ValidationException(String.format("Password length exceeds maximum allowed (%d characters) for your plan", 
                        limits.getMaxPasswordLength()));
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
            logger.info("Vault item created successfully: {} (id: {}) for user id: {}", saved.getTitle(), saved.getId(), userId);
            auditLogService.logAction(userId, "CREATE_VAULT_ITEM", 
                    "Created vault item: " + saved.getTitle(), null);
            
            return toDTO(saved);
        } catch (BusinessException | ValidationException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error on creating vault item for user id: {} with title: {}", userId, createDTO.getTitle(), e);
            throw e;
        }
    }

    public VaultItemDTO updateVaultItem(Long id, Long userId, VaultItemUpdateDTO updateDTO) {
        logger.info("Updating vault item with id: {} for user id: {}", id, userId);
        try {
            VaultItem item = vaultItemRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.warn("Vault item not found for update with id: {}", id);
                        return new ResourceNotFoundException("VaultItem", id);
                    });
            
            if (!item.getUser().getId().equals(userId)) {
                logger.warn("Vault item {} does not belong to user {} for update", id, userId);
                throw new ValidationException("Vault item does not belong to this user");
            }

        // Get plan limits for password validation
        var planDTO = servicePlanService.getServicePlanWithLimits(item.getUser().getServicePlan().getId());
        var limits = planDTO.getLimits();
        
        boolean passwordChanged = false;
        String oldEncryptedPassword = item.getEncryptedPassword();
        String oldPasswordIv = item.getPasswordIv();
        String oldPasswordSalt = item.getPasswordSalt();

        if (updateDTO.getTitle() != null) {
            item.setTitle(updateDTO.getTitle());
        }
        if (updateDTO.getUsername() != null) {
            item.setUsername(updateDTO.getUsername());
        }
        if (updateDTO.getPassword() != null && !updateDTO.getPassword().isEmpty()) {
            // Validate password length limit
            if (limits != null && updateDTO.getPassword().length() > limits.getMaxPasswordLength()) {
                throw new ValidationException(String.format("Password length exceeds maximum allowed (%d characters) for your plan", 
                        limits.getMaxPasswordLength()));
            }
            
            // Save old password to history if plan allows history
            if (limits != null && limits.getMaxHistoryVersions() > 0) {
                PasswordHistory history = new PasswordHistory();
                history.setVaultItem(item);
                history.setEncryptedPassword(oldEncryptedPassword);
                history.setPasswordIv(oldPasswordIv);
                history.setPasswordSalt(oldPasswordSalt);
                passwordHistoryRepository.save(history);
                
                // Limit history versions
                Long historyCount = passwordHistoryRepository.countByVaultItemId(item.getId());
                if (historyCount > limits.getMaxHistoryVersions()) {
                    passwordHistoryRepository.deleteOldVersions(item.getId(), limits.getMaxHistoryVersions());
                }
            }
            
            passwordChanged = true;
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
            logger.info("Vault item updated successfully: {} (id: {}) for user id: {} {}", 
                    updated.getTitle(), id, userId, passwordChanged ? "(password changed)" : "");
            auditLogService.logAction(userId, "UPDATE_VAULT_ITEM", 
                    "Updated vault item: " + updated.getTitle() + (passwordChanged ? " (password changed)" : ""), null);
            
            return toDTO(updated);
        } catch (ValidationException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error on updating vault item with id: {} for user id: {}", id, userId, e);
            throw e;
        }
    }

    public void deleteVaultItem(Long id, Long userId) {
        logger.info("Deleting vault item with id: {} for user id: {}", id, userId);
        try {
            VaultItem item = vaultItemRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.warn("Vault item not found for deletion with id: {}", id);
                        return new ResourceNotFoundException("VaultItem", id);
                    });
            
            if (!item.getUser().getId().equals(userId)) {
                logger.warn("Vault item {} does not belong to user {} for deletion", id, userId);
                throw new ValidationException("Vault item does not belong to this user");
            }

            String title = item.getTitle();
            vaultItemRepository.delete(item);
            logger.info("Vault item deleted successfully: {} (id: {}) for user id: {}", title, id, userId);
            auditLogService.logAction(userId, "DELETE_VAULT_ITEM", 
                    "Deleted vault item: " + title, null);
        } catch (ValidationException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error on deleting vault item with id: {} for user id: {}", id, userId, e);
            throw e;
        }
    }

    public List<VaultItemDTO> getFavoriteItems(Long userId) {
        logger.debug("Getting favorite vault items for user id: {}", userId);
        try {
            List<VaultItemDTO> items = vaultItemRepository.findByUserIdAndIsFavorite(userId, true).stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
            logger.info("Successfully retrieved {} favorite vault items for user id: {}", items.size(), userId);
            return items;
        } catch (Exception e) {
            logger.error("Error on getting favorite vault items for user id: {}", userId, e);
            throw e;
        }
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

