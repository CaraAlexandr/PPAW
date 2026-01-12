package com.ppaw.passwordvault.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "vault_items", schema = "vault_schema", indexes = {
    @Index(name = "idx_vault_items_user_id", columnList = "user_id"),
    @Index(name = "idx_vault_items_folder", columnList = "folder"),
    @Index(name = "idx_vault_items_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VaultItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(name = "username")
    private String username;

    @Column(nullable = false, name = "encrypted_password", columnDefinition = "TEXT")
    private String encryptedPassword;

    @Column(nullable = false, name = "password_iv")
    private String passwordIv;

    @Column(nullable = false, name = "password_salt")
    private String passwordSalt;

    @Column(length = 500)
    private String url;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private String folder;

    private String tags;

    @Column(nullable = false)
    private Boolean isFavorite = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "vaultItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PasswordHistory> passwordHistory;

    @OneToMany(mappedBy = "vaultItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SharedVaultItem> sharedVaultItems;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

