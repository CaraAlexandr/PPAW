package com.ppaw.passwordvault.service;

import com.ppaw.passwordvault.dto.UserAdminViewModel;
import com.ppaw.passwordvault.exception.ResourceNotFoundException;
import com.ppaw.passwordvault.exception.ValidationException;
import com.ppaw.passwordvault.model.ServicePlan;
import com.ppaw.passwordvault.model.User;
import com.ppaw.passwordvault.repository.ServicePlanRepository;
import com.ppaw.passwordvault.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * UserAdminService - Service pentru administrarea utilizatorilor
 * Folosit în admin panel MVC
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserAdminService {

    private static final Logger logger = LoggerFactory.getLogger(UserAdminService.class);

    private final UserRepository userRepository;
    private final ServicePlanRepository servicePlanRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public List<UserAdminViewModel> getAllUsers() {
        logger.info("Getting all users for admin panel (excluding deleted)");
        try {
            List<UserAdminViewModel> users = userRepository.findAllNotDeleted().stream()
                    .map(this::toAdminViewModel)
                    .collect(Collectors.toList());
            logger.info("Successfully retrieved {} users for admin panel", users.size());
            return users;
        } catch (Exception e) {
            logger.error("Error on getting users from database for admin panel", e);
            throw e;
        }
    }

    public UserAdminViewModel getUserById(Long id) {
        logger.debug("Getting user by id for admin panel: {}", id);
        try {
            User user = userRepository.findByIdNotDeleted(id)
                    .orElseThrow(() -> {
                        logger.warn("User not found for admin panel with id: {}", id);
                        return new ResourceNotFoundException("User", id);
                    });
            logger.info("Successfully retrieved user for admin panel: {} (id: {})", user.getUsername(), id);
            return toAdminViewModel(user);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error on getting user by id for admin panel: {}", id, e);
            throw e;
        }
    }

    @Transactional
    public UserAdminViewModel createUser(UserAdminViewModel viewModel) {
        logger.info("Creating new user via admin panel with username: {} and email: {}", 
                viewModel.getUsername(), viewModel.getEmail());
        try {
            // Validare - check only non-deleted users
            if (userRepository.findByUsernameNotDeleted(viewModel.getUsername()).isPresent()) {
                logger.warn("Username already exists in admin panel: {}", viewModel.getUsername());
                throw new ValidationException("Username deja există");
            }
            if (userRepository.findByEmailNotDeleted(viewModel.getEmail()).isPresent()) {
                logger.warn("Email already exists in admin panel: {}", viewModel.getEmail());
                throw new ValidationException("Email deja există");
            }

            ServicePlan plan = servicePlanRepository.findById(viewModel.getServicePlanId())
                    .orElseThrow(() -> {
                        logger.error("Service plan not found in admin panel with id: {}", viewModel.getServicePlanId());
                        return new ResourceNotFoundException("ServicePlan", viewModel.getServicePlanId());
                    });

            User user = new User();
            user.setUsername(viewModel.getUsername());
            user.setEmail(viewModel.getEmail());
            user.setPasswordHash(hashPassword(viewModel.getPassword()));
            user.setServicePlan(plan);
            user.setIsActive(viewModel.getIsActive() != null ? viewModel.getIsActive() : true);
            user.setIsDeleted(false); // New users are not deleted
            user.setLoginCount(0);

            User saved = userRepository.save(user);
            logger.info("User created successfully via admin panel: {} (id: {})", saved.getUsername(), saved.getId());
            return toAdminViewModel(saved);
        } catch (ValidationException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error on creating user via admin panel with username: {}", viewModel.getUsername(), e);
            throw e;
        }
    }

    @Transactional
    public UserAdminViewModel updateUser(Long id, UserAdminViewModel viewModel) {
        logger.info("Updating user via admin panel with id: {}", id);
        try {
            // Soft delete: only update if user is not deleted
            User user = userRepository.findByIdNotDeleted(id)
                    .orElseThrow(() -> {
                        logger.warn("User not found for update in admin panel with id: {}", id);
                        return new ResourceNotFoundException("User", id);
                    });

            // Validare username
            if (viewModel.getUsername() != null && !viewModel.getUsername().equals(user.getUsername())) {
                logger.debug("Updating username for user id: {} to: {}", id, viewModel.getUsername());
                userRepository.findByUsernameNotDeleted(viewModel.getUsername())
                        .ifPresent(existing -> {
                            if (!existing.getId().equals(id)) {
                                logger.warn("Username already exists in admin panel: {}", viewModel.getUsername());
                                throw new ValidationException("Username deja există");
                            }
                        });
                user.setUsername(viewModel.getUsername());
            }

            // Validare email
            if (viewModel.getEmail() != null && !viewModel.getEmail().equals(user.getEmail())) {
                logger.debug("Updating email for user id: {} to: {}", id, viewModel.getEmail());
                userRepository.findByEmailNotDeleted(viewModel.getEmail())
                        .ifPresent(existing -> {
                            if (!existing.getId().equals(id)) {
                                logger.warn("Email already exists in admin panel: {}", viewModel.getEmail());
                                throw new ValidationException("Email deja există");
                            }
                        });
                user.setEmail(viewModel.getEmail());
            }

            // Actualizare parolă doar dacă este furnizată
            if (viewModel.getPassword() != null && !viewModel.getPassword().isEmpty()) {
                logger.debug("Updating password for user id: {}", id);
                user.setPasswordHash(hashPassword(viewModel.getPassword()));
            }

            // Actualizare plan
            if (viewModel.getServicePlanId() != null) {
                logger.debug("Updating service plan for user id: {} to: {}", id, viewModel.getServicePlanId());
                ServicePlan plan = servicePlanRepository.findById(viewModel.getServicePlanId())
                        .orElseThrow(() -> {
                            logger.error("Service plan not found in admin panel with id: {}", viewModel.getServicePlanId());
                            return new ResourceNotFoundException("ServicePlan", viewModel.getServicePlanId());
                        });
                user.setServicePlan(plan);
            }

            // Actualizare status
            if (viewModel.getIsActive() != null) {
                logger.debug("Updating isActive for user id: {} to: {}", id, viewModel.getIsActive());
                user.setIsActive(viewModel.getIsActive());
            }

            User updated = userRepository.save(user);
            logger.info("User updated successfully via admin panel: {} (id: {})", updated.getUsername(), id);
            return toAdminViewModel(updated);
        } catch (ValidationException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error on updating user via admin panel with id: {}", id, e);
            throw e;
        }
    }

    @Transactional
    public void deleteUser(Long id) {
        logger.info("Soft deleting user via admin panel with id: {}", id);
        try {
            // Soft delete: mark user as deleted instead of physically removing
            User user = userRepository.findByIdNotDeleted(id)
                    .orElseThrow(() -> {
                        logger.warn("User not found for deletion in admin panel with id: {}", id);
                        return new ResourceNotFoundException("User", id);
                    });
            user.setIsDeleted(true);
            userRepository.save(user);
            logger.info("User soft deleted successfully via admin panel: {} (id: {})", user.getUsername(), id);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error on deleting user via admin panel with id: {}", id, e);
            throw e;
        }
    }

    private UserAdminViewModel toAdminViewModel(User user) {
        UserAdminViewModel viewModel = new UserAdminViewModel();
        viewModel.setId(user.getId());
        viewModel.setUsername(user.getUsername());
        viewModel.setEmail(user.getEmail());
        viewModel.setServicePlanId(user.getServicePlan() != null ? user.getServicePlan().getId() : null);
        viewModel.setServicePlanName(user.getServicePlan() != null ? user.getServicePlan().getName() : null);
        viewModel.setIsActive(user.getIsActive());
        viewModel.setLoginCount(user.getLoginCount());
        viewModel.setLastLoginAt(user.getLastLoginAt() != null 
                ? user.getLastLoginAt().format(DATE_FORMATTER) 
                : "N/A");
        return viewModel;
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Password hashing failed", e);
        }
    }
}


