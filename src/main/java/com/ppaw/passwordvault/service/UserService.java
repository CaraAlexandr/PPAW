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

    private final UserRepository userRepository;
    private final ServicePlanRepository servicePlanRepository;
    private final AuditLogService auditLogService;

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        return toDTO(user);
    }

    public UserDTO createUser(UserCreateDTO createDTO) {
        // Validare
        if (userRepository.findByUsername(createDTO.getUsername()).isPresent()) {
            throw new ValidationException("Username already exists");
        }
        if (userRepository.findByEmail(createDTO.getEmail()).isPresent()) {
            throw new ValidationException("Email already exists");
        }

        ServicePlan plan = servicePlanRepository.findById(createDTO.getServicePlanId())
                .orElseThrow(() -> new ResourceNotFoundException("ServicePlan", createDTO.getServicePlanId()));

        User user = new User();
        user.setUsername(createDTO.getUsername());
        user.setEmail(createDTO.getEmail());
        user.setPasswordHash(hashPassword(createDTO.getPassword()));
        user.setServicePlan(plan);
        user.setIsActive(true);
        user.setLoginCount(0);

        User saved = userRepository.save(user);
        auditLogService.logAction(saved.getId(), "USER_CREATED", "User account created", null);
        
        return toDTO(saved);
    }

    public UserDTO updateUser(Long id, UserUpdateDTO updateDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        if (updateDTO.getUsername() != null) {
            userRepository.findByUsername(updateDTO.getUsername())
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(id)) {
                            throw new ValidationException("Username already exists");
                        }
                    });
            user.setUsername(updateDTO.getUsername());
        }

        if (updateDTO.getEmail() != null) {
            userRepository.findByEmail(updateDTO.getEmail())
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(id)) {
                            throw new ValidationException("Email already exists");
                        }
                    });
            user.setEmail(updateDTO.getEmail());
        }

        if (updateDTO.getIsActive() != null) {
            user.setIsActive(updateDTO.getIsActive());
        }

        if (updateDTO.getServicePlanId() != null) {
            ServicePlan plan = servicePlanRepository.findById(updateDTO.getServicePlanId())
                    .orElseThrow(() -> new ResourceNotFoundException("ServicePlan", updateDTO.getServicePlanId()));
            user.setServicePlan(plan);
        }

        User updated = userRepository.save(user);
        auditLogService.logAction(updated.getId(), "USER_UPDATED", "User account updated", null);
        
        return toDTO(updated);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        userRepository.delete(user);
        auditLogService.logAction(id, "USER_DELETED", "User account deleted", null);
    }

    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new ValidationException("Invalid username or password"));

        if (!user.getIsActive()) {
            throw new ValidationException("User account is inactive");
        }

        String hashedPassword = hashPassword(loginRequest.getPassword());
        if (!user.getPasswordHash().equals(hashedPassword)) {
            throw new ValidationException("Invalid username or password");
        }

        // Actualizează login count și last login
        user.setLastLoginAt(java.time.LocalDateTime.now());
        user.setLoginCount((user.getLoginCount() != null ? user.getLoginCount() : 0) + 1);
        userRepository.save(user);

        // Audit log
        auditLogService.logAction(user.getId(), "LOGIN", "User logged in", null);

        return LoginResponseDTO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .servicePlanId(user.getServicePlan() != null ? user.getServicePlan().getId() : null)
                .servicePlanName(user.getServicePlan() != null ? user.getServicePlan().getName() : null)
                .lastLoginAt(user.getLastLoginAt())
                .loginCount(user.getLoginCount())
                .success(true)
                .build();
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

