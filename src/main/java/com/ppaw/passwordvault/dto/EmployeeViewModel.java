package com.ppaw.passwordvault.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * EmployeeViewModel - DTO complex pentru admin panel
 * Conține câmpuri din Employee + câmpuri din Company (pentru afișare)
 * Folosit în form-urile de administrare angajați
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeViewModel {

    private Long id;

    @NotBlank(message = "Prenumele este obligatoriu")
    @Size(min = 2, max = 100, message = "Prenumele trebuie să aibă între 2 și 100 de caractere")
    private String firstName;

    @NotBlank(message = "Numele este obligatoriu")
    @Size(min = 2, max = 100, message = "Numele trebuie să aibă între 2 și 100 de caractere")
    private String lastName;

    @NotBlank(message = "Email-ul este obligatoriu")
    @Email(message = "Email-ul trebuie să fie valid")
    @Size(max = 255, message = "Email-ul nu poate depăși 255 de caractere")
    private String email;

    @Size(max = 20, message = "Telefonul nu poate depăși 20 de caractere")
    private String phone;

    @Size(max = 100, message = "Funcția nu poate depăși 100 de caractere")
    private String position;

    @NotNull(message = "Compania este obligatorie")
    private Long companyId; // Pentru selectare în form

    private Boolean isActive = true; // Checkbox

    // Câmpuri din Company pentru afișare în tabel
    private String companyName;
    private String companyCountry;
}

