package com.ppaw.passwordvault.dto;

import com.ppaw.passwordvault.validation.OptionalSize;
import com.ppaw.passwordvault.validation.ValidationGroups;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UserAdminViewModel - DTO pentru admin panel
 * Folosit în form-urile de administrare utilizatori
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAdminViewModel {

    private Long id;

    @NotBlank(message = "Username este obligatoriu")
    @Size(min = 3, max = 100, message = "Username trebuie să aibă între 3 și 100 de caractere")
    private String username;

    @NotBlank(message = "Email este obligatoriu")
    @Email(message = "Email trebuie să fie valid")
    @Size(max = 255, message = "Email nu poate depăși 255 de caractere")
    private String email;

    @NotBlank(message = "Parola este obligatorie", groups = ValidationGroups.Create.class)
    @Size(min = 8, message = "Parola trebuie să aibă minimum 8 caractere", groups = ValidationGroups.Create.class)
    @OptionalSize(min = 8, message = "Parola trebuie să aibă minimum 8 caractere (lăsați gol pentru a păstra parola actuală)", 
                  groups = ValidationGroups.Update.class)
    private String password; // Opțional pentru edit (doar dacă se schimbă)

    @NotNull(message = "Planul de servicii este obligatoriu")
    private Long servicePlanId;

    private Boolean isActive = true; // Checkbox

    // Informații pentru afișare
    private String servicePlanName;
    private Integer loginCount;
    private String lastLoginAt;
}

