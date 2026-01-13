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
public class PlanLimitsDTO {
    private Long id;
    private Long planId;
    private Integer maxVaultItems;
    private Integer maxPasswordLength;
    private Boolean canExport;
    private Boolean canImport;
    private Boolean canShare;
    private Integer maxHistoryVersions;
    private Boolean canAttachments;
    private Integer maxDevices;
    private Boolean excludeAmbiguous;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

