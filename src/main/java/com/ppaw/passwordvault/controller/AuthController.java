package com.ppaw.passwordvault.controller;

import com.ppaw.passwordvault.dto.ApiResponse;
import com.ppaw.passwordvault.dto.LoginRequestDTO;
import com.ppaw.passwordvault.dto.LoginResponseDTO;
import com.ppaw.passwordvault.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(
            @Valid @RequestBody LoginRequestDTO loginRequest) {
        logger.info("POST /api/auth/login - Login attempt for username/email: {}", loginRequest.getUsername());
        try {
            LoginResponseDTO response = userService.login(loginRequest);
            logger.info("POST /api/auth/login - Login successful for user: {} (id: {})", 
                    response.getUsername(), response.getUserId());
            return ResponseEntity.ok(ApiResponse.success("Login successful", response));
        } catch (Exception e) {
            logger.error("POST /api/auth/login - Login failed for username/email: {}", 
                    loginRequest.getUsername(), e);
            throw e;
        }
    }
}

