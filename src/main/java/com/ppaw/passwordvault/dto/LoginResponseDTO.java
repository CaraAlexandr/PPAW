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
public class LoginResponseDTO {
    private Long userId;
    private String username;
    private String email;
    private Long servicePlanId;
    private String servicePlanName;
    private LocalDateTime lastLoginAt;
    private Integer loginCount;
    private Boolean success;
}

