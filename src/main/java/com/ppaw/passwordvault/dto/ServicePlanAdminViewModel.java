package com.ppaw.passwordvault.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * ServicePlanAdminViewModel - DTO pentru admin panel
 * Folosit în form-urile de administrare planuri de servicii
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServicePlanAdminViewModel {

    private Long id;

    @NotBlank(message = "Numele planului este obligatoriu")
    @Size(min = 2, max = 50, message = "Numele planului trebuie să aibă între 2 și 50 de caractere")
    private String name;

    @NotNull(message = "Prețul este obligatoriu")
    @Positive(message = "Prețul trebuie să fie pozitiv")
    private BigDecimal price;

    @NotBlank(message = "Moneda este obligatorie")
    @Size(max = 10, message = "Moneda nu poate depăși 10 caractere")
    private String currency = "USD";

    private Boolean isActive = true;

    // Plan Limits
    private Long limitsId;
    private Integer maxVaultItems;
    private Integer maxPasswordLength;
    private Boolean canExport = false;
    private Boolean canImport = false;
    private Boolean canShare = false;
    private Integer maxHistoryVersions = 0;
    private Boolean canAttachments = false;
    private Integer maxDevices = 1;
    private Boolean excludeAmbiguous = false;
}

