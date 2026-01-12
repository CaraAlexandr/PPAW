package com.ppaw.passwordvault.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VaultItemCreateDTO {
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @Size(max = 255, message = "Username must not exceed 255 characters")
    private String username;

    @NotBlank(message = "Password is required")
    private String password; // Va fi criptat în service

    @Size(max = 500, message = "URL must not exceed 500 characters")
    private String url;

    private String notes;

    @Size(max = 100, message = "Folder must not exceed 100 characters")
    private String folder;

    private String tags;

    private Boolean isFavorite = false;
}

