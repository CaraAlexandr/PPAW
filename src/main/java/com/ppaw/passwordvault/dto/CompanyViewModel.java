package com.ppaw.passwordvault.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CompanyViewModel - DTO pentru form-uri
 * Echivalent CompanyViewModel din ASP.NET MVC
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyViewModel {

    private Long id;

    @NotBlank(message = "Numele este obligatoriu")
    @Size(max = 100, message = "Numele nu poate depăși 100 de caractere")
    private String name;

    @Size(max = 255, message = "Descrierea nu poate depăși 255 de caractere")
    private String description;

    @NotBlank(message = "Țara este obligatorie")
    @Size(max = 50, message = "Țara nu poate depăși 50 de caractere")
    private String country; // Câmp select

    private Boolean isActive = true; // Checkbox

    @Email(message = "Email-ul trebuie să fie valid")
    @Size(max = 100, message = "Email-ul nu poate depăși 100 de caractere")
    private String email;

    @Size(max = 20, message = "Telefonul nu poate depăși 20 de caractere")
    private String phone;
}


