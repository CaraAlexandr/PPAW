package com.ppaw.passwordvault.service;

import com.ppaw.passwordvault.dto.LoginRequestDTO;
import com.ppaw.passwordvault.dto.LoginResponseDTO;
import com.ppaw.passwordvault.dto.UserCreateDTO;
import com.ppaw.passwordvault.dto.UserDTO;
import com.ppaw.passwordvault.dto.UserUpdateDTO;
import com.ppaw.passwordvault.exception.BusinessException;
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
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final ServicePlanRepository servicePlanRepository;
    private final ServicePlanService servicePlanService;
    private final AuditLogService auditLogService;
    private final TokenService tokenService;

    public List<UserDTO> getAllUsers() {
        logger.info("Getting all users (excluding deleted)");
        try {
            List<UserDTO> users = userRepository.findAllNotDeleted().stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
            logger.info("Successfully retrieved {} users", users.size());
            return users;
        } catch (Exception e) {
            logger.error("Error on getting users from database", e);
            throw e;
        }
    }

    public UserDTO getUserById(Long id) {
        logger.debug("Getting user by id: {}", id);
        try {
            User user = userRepository.findByIdNotDeleted(id)
                    .orElseThrow(() -> {
                        logger.warn("User not found with id: {}", id);
                        return new ResourceNotFoundException("User", id);
                    });
            logger.info("Successfully retrieved user: {} (id: {})", user.getUsername(), id);
            return toDTO(user);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error on getting user by id: {}", id, e);
            throw e;
        }
    }

    public UserDTO createUser(UserCreateDTO createDTO) {
        logger.info("Creating new user with username: {} and email: {}", createDTO.getUsername(), createDTO.getEmail());
        try {
            // Validare - check only non-deleted users
            if (userRepository.findByUsernameNotDeleted(createDTO.getUsername()).isPresent()) {
                logger.warn("Username already exists: {}", createDTO.getUsername());
                throw new ValidationException("Username already exists");
            }
            if (userRepository.findByEmailNotDeleted(createDTO.getEmail()).isPresent()) {
                logger.warn("Email already exists: {}", createDTO.getEmail());
                throw new ValidationException("Email already exists");
            }

            ServicePlan plan = servicePlanRepository.findById(createDTO.getServicePlanId())
                    .orElseThrow(() -> {
                        logger.error("Service plan not found with id: {}", createDTO.getServicePlanId());
                        return new ResourceNotFoundException("ServicePlan", createDTO.getServicePlanId());
                    });

            User user = new User();
            user.setUsername(createDTO.getUsername());
            user.setEmail(createDTO.getEmail());
            user.setPasswordHash(hashPassword(createDTO.getPassword()));
            user.setServicePlan(plan);
            user.setIsActive(true);
            user.setIsDeleted(false); // New users are not deleted
            user.setLoginCount(0);

            User saved = userRepository.save(user);
            logger.info("User created successfully: {} (id: {})", saved.getUsername(), saved.getId());
            auditLogService.logAction(saved.getId(), "USER_CREATED", "User account created", null);
            
            return toDTO(saved);
        } catch (ValidationException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error on creating user with username: {}", createDTO.getUsername(), e);
            throw e;
        }
    }

    public UserDTO updateUser(Long id, UserUpdateDTO updateDTO) {
        logger.info("Updating user with id: {}", id);
        try {
            // Soft delete: only update if user is not deleted
            User user = userRepository.findByIdNotDeleted(id)
                    .orElseThrow(() -> {
                        logger.warn("User not found for update with id: {}", id);
                        return new ResourceNotFoundException("User", id);
                    });

            if (updateDTO.getUsername() != null) {
                logger.debug("Updating username for user id: {} to: {}", id, updateDTO.getUsername());
                userRepository.findByUsernameNotDeleted(updateDTO.getUsername())
                        .ifPresent(existing -> {
                            if (!existing.getId().equals(id)) {
                                logger.warn("Username already exists: {}", updateDTO.getUsername());
                                throw new ValidationException("Username already exists");
                            }
                        });
                user.setUsername(updateDTO.getUsername());
            }

            if (updateDTO.getEmail() != null) {
                logger.debug("Updating email for user id: {} to: {}", id, updateDTO.getEmail());
                userRepository.findByEmailNotDeleted(updateDTO.getEmail())
                        .ifPresent(existing -> {
                            if (!existing.getId().equals(id)) {
                                logger.warn("Email already exists: {}", updateDTO.getEmail());
                                throw new ValidationException("Email already exists");
                            }
                        });
                user.setEmail(updateDTO.getEmail());
            }

            if (updateDTO.getIsActive() != null) {
                logger.debug("Updating isActive for user id: {} to: {}", id, updateDTO.getIsActive());
                user.setIsActive(updateDTO.getIsActive());
            }

            if (updateDTO.getServicePlanId() != null) {
                logger.debug("Updating service plan for user id: {} to: {}", id, updateDTO.getServicePlanId());
                ServicePlan plan = servicePlanRepository.findById(updateDTO.getServicePlanId())
                        .orElseThrow(() -> {
                            logger.error("Service plan not found with id: {}", updateDTO.getServicePlanId());
                            return new ResourceNotFoundException("ServicePlan", updateDTO.getServicePlanId());
                        });
                user.setServicePlan(plan);
            }

            User updated = userRepository.save(user);
            logger.info("User updated successfully: {} (id: {})", updated.getUsername(), id);
            auditLogService.logAction(updated.getId(), "USER_UPDATED", "User account updated", null);
            
            return toDTO(updated);
        } catch (ValidationException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error on updating user with id: {}", id, e);
            throw e;
        }
    }

    public void deleteUser(Long id) {
        logger.info("Soft deleting user with id: {}", id);
        try {
            // Soft delete: mark user as deleted instead of physically removing
            User user = userRepository.findByIdNotDeleted(id)
                    .orElseThrow(() -> {
                        logger.warn("User not found for deletion with id: {}", id);
                        return new ResourceNotFoundException("User", id);
                    });
            user.setIsDeleted(true);
            userRepository.save(user);
            logger.info("User soft deleted successfully: {} (id: {})", user.getUsername(), id);
            auditLogService.logAction(id, "USER_DELETED", "User account soft deleted", null);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error on deleting user with id: {}", id, e);
            throw e;
        }
    }

    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        logger.debug("Login attempt for username/email: {}", loginRequest.getUsername());
        try {
            // Try to find user by username or email (exclude deleted users)
            User user = userRepository.findByUsernameOrEmailNotDeleted(loginRequest.getUsername())
                    .orElseThrow(() -> {
                        logger.warn("Login failed: User not found or deleted - {}", loginRequest.getUsername());
                        return new ValidationException("Invalid username or password");
                    });

            if (!user.getIsActive()) {
                logger.warn("Login failed: User account is inactive - {}", user.getUsername());
                throw new ValidationException("User account is inactive");
            }
            
            // Check if user is deleted (should not happen due to query, but double-check)
            if (user.getIsDeleted()) {
                logger.warn("Login failed: User account has been deleted - {}", user.getUsername());
                throw new ValidationException("User account has been deleted");
            }

            String hashedPassword = hashPassword(loginRequest.getPassword());
            if (!user.getPasswordHash().equals(hashedPassword)) {
                logger.warn("Login failed: Invalid password for user - {}", user.getUsername());
                throw new ValidationException("Invalid username or password");
            }

            // Actualizează login count și last login
            user.setLastLoginAt(java.time.LocalDateTime.now());
            user.setLoginCount((user.getLoginCount() != null ? user.getLoginCount() : 0) + 1);
            userRepository.save(user);

            logger.info("User logged in successfully: {} (id: {})", user.getUsername(), user.getId());
            // Audit log
            auditLogService.logAction(user.getId(), "LOGIN", "User logged in", null);

            // LAB 8: Generate token for authentication
            String token = tokenService.generateToken(user.getId());

            // Get plan limits
            com.ppaw.passwordvault.dto.PlanLimitsDTO planLimits = null;
            if (user.getServicePlan() != null) {
                var planDTO = servicePlanService.getServicePlanWithLimits(user.getServicePlan().getId());
                planLimits = planDTO.getLimits();
            }

            return LoginResponseDTO.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .token(token) // LAB 8: Token for authentication
                    .servicePlanId(user.getServicePlan() != null ? user.getServicePlan().getId() : null)
                    .servicePlanName(user.getServicePlan() != null ? user.getServicePlan().getName() : null)
                    .planLimits(planLimits) // Include plan limits
                    .lastLoginAt(user.getLastLoginAt())
                    .loginCount(user.getLoginCount())
                    .success(true)
                    .build();
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error on login for username: {}", loginRequest.getUsername(), e);
            throw e;
        }
    }

    private UserDTO toDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .servicePlanId(user.getServicePlan() != null ? user.getServicePlan().getId() : null)
                .servicePlanName(user.getServicePlan() != null ? user.getServicePlan().getName() : null)
                .isActive(user.getIsActive())
                .lastLoginAt(user.getLastLoginAt())
                .loginCount(user.getLoginCount())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
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
            throw new BusinessException("Password hashing failed");
        }
    }
}

