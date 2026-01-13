package com.ppaw.passwordvault.controller;

import com.ppaw.passwordvault.dto.ApiResponse;
import com.ppaw.passwordvault.dto.LoginRequestDTO;
import com.ppaw.passwordvault.dto.LoginResponseDTO;
import com.ppaw.passwordvault.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(
            @Valid @RequestBody LoginRequestDTO loginRequest) {
        LoginResponseDTO response = userService.login(loginRequest);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }
}

