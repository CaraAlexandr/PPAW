package com.ppaw.passwordvault.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VaultItemDTO {
    private Long id;
    private Long userId;
    private String title;
    private String username;
    private String url;
    private String notes;
    private String folder;
    private String tags;
    private Boolean isFavorite;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // Nu expunem parolele încărcate în DTO pentru securitate
}

