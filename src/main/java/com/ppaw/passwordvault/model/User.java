package com.ppaw.passwordvault.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users", schema = "vault_schema", indexes = {
    @Index(name = "idx_users_plan_id", columnList = "service_plan_id"),
    @Index(name = "idx_users_email", columnList = "email")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, name = "password_hash")
    private String passwordHash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_plan_id", nullable = false)
    private ServicePlan servicePlan;

    @Column(nullable = false)
    private Boolean isActive = true;

    // Soft delete - marchează utilizatorul ca șters logic
    @Column(nullable = false, name = "is_deleted")
    private Boolean isDeleted = false;

    // NOUĂ PROPRIETATE 1: Data ultimei autentificări
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    // NOUĂ PROPRIETATE 2: Numărul de autentificări
    @Column(nullable = false, name = "login_count")
    private Integer loginCount = 0;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VaultItem> vaultItems;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AuditLog> auditLogs;

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

