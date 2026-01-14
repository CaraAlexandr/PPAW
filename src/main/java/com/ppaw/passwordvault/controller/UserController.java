package com.ppaw.passwordvault.controller;

import com.ppaw.passwordvault.dto.ApiResponse;
import com.ppaw.passwordvault.dto.UserCreateDTO;
import com.ppaw.passwordvault.dto.UserDTO;
import com.ppaw.passwordvault.dto.UserUpdateDTO;
import com.ppaw.passwordvault.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers() {
        logger.info("GET /api/users - Request to get all users");
        try {
            List<UserDTO> users = userService.getAllUsers();
            logger.info("GET /api/users - Successfully retrieved {} users", users.size());
            return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
        } catch (Exception e) {
            logger.error("GET /api/users - Error retrieving users", e);
            throw e;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
        logger.info("GET /api/users/{} - Request to get user by id", id);
        try {
            UserDTO user = userService.getUserById(id);
            logger.info("GET /api/users/{} - Successfully retrieved user: {}", id, user.getUsername());
            return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", user));
        } catch (Exception e) {
            logger.error("GET /api/users/{} - Error retrieving user", id, e);
            throw e;
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserDTO>> createUser(@Valid @RequestBody UserCreateDTO createDTO) {
        logger.info("POST /api/users - Request to create user with username: {}", createDTO.getUsername());
        try {
            UserDTO user = userService.createUser(createDTO);
            logger.info("POST /api/users - Successfully created user: {} (id: {})", user.getUsername(), user.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("User created successfully", user));
        } catch (Exception e) {
            logger.error("POST /api/users - Error creating user with username: {}", createDTO.getUsername(), e);
            throw e;
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateDTO updateDTO) {
        logger.info("PUT /api/users/{} - Request to update user", id);
        try {
            UserDTO user = userService.updateUser(id, updateDTO);
            logger.info("PUT /api/users/{} - Successfully updated user: {}", id, user.getUsername());
            return ResponseEntity.ok(ApiResponse.success("User updated successfully", user));
        } catch (Exception e) {
            logger.error("PUT /api/users/{} - Error updating user", id, e);
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        logger.info("DELETE /api/users/{} - Request to delete user", id);
        try {
            userService.deleteUser(id);
            logger.info("DELETE /api/users/{} - Successfully deleted user", id);
            return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
        } catch (Exception e) {
            logger.error("DELETE /api/users/{} - Error deleting user", id, e);
            throw e;
        }
    }
}

