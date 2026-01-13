package com.ppaw.passwordvault.service;

import com.ppaw.passwordvault.dto.UserAdminViewModel;
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

    private final UserRepository userRepository;
    private final ServicePlanRepository servicePlanRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public List<UserAdminViewModel> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toAdminViewModel)
                .collect(Collectors.toList());
    }

    public UserAdminViewModel getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        return toAdminViewModel(user);
    }

    @Transactional
    public UserAdminViewModel createUser(UserAdminViewModel viewModel) {
        // Validare
        if (userRepository.findByUsername(viewModel.getUsername()).isPresent()) {
            throw new ValidationException("Username deja există");
        }
        if (userRepository.findByEmail(viewModel.getEmail()).isPresent()) {
            throw new ValidationException("Email deja există");
        }

        ServicePlan plan = servicePlanRepository.findById(viewModel.getServicePlanId())
                .orElseThrow(() -> new ResourceNotFoundException("ServicePlan", viewModel.getServicePlanId()));

        User user = new User();
        user.setUsername(viewModel.getUsername());
        user.setEmail(viewModel.getEmail());
        user.setPasswordHash(hashPassword(viewModel.getPassword()));
        user.setServicePlan(plan);
        user.setIsActive(viewModel.getIsActive() != null ? viewModel.getIsActive() : true);
        user.setLoginCount(0);

        User saved = userRepository.save(user);
        return toAdminViewModel(saved);
    }

    @Transactional
    public UserAdminViewModel updateUser(Long id, UserAdminViewModel viewModel) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        // Validare username
        if (viewModel.getUsername() != null && !viewModel.getUsername().equals(user.getUsername())) {
            userRepository.findByUsername(viewModel.getUsername())
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(id)) {
                            throw new ValidationException("Username deja există");
                        }
                    });
            user.setUsername(viewModel.getUsername());
        }

        // Validare email
        if (viewModel.getEmail() != null && !viewModel.getEmail().equals(user.getEmail())) {
            userRepository.findByEmail(viewModel.getEmail())
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(id)) {
                            throw new ValidationException("Email deja există");
                        }
                    });
            user.setEmail(viewModel.getEmail());
        }

        // Actualizare parolă doar dacă este furnizată
        if (viewModel.getPassword() != null && !viewModel.getPassword().isEmpty()) {
            user.setPasswordHash(hashPassword(viewModel.getPassword()));
        }

        // Actualizare plan
        if (viewModel.getServicePlanId() != null) {
            ServicePlan plan = servicePlanRepository.findById(viewModel.getServicePlanId())
                    .orElseThrow(() -> new ResourceNotFoundException("ServicePlan", viewModel.getServicePlanId()));
            user.setServicePlan(plan);
        }

        // Actualizare status
        if (viewModel.getIsActive() != null) {
            user.setIsActive(viewModel.getIsActive());
        }

        User updated = userRepository.save(user);
        return toAdminViewModel(updated);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        userRepository.delete(user);
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

