package com.ppaw.passwordvault.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vault_item_id", nullable = false)
    private VaultItem vaultItem;

    @Column(nullable = false, name = "encrypted_password", columnDefinition = "TEXT")
    private String encryptedPassword;

    @Column(nullable = false, name = "password_iv")
    private String passwordIv;

    @Column(nullable = false, name = "password_salt")
    private String passwordSalt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

