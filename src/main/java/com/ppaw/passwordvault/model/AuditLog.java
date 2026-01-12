package com.ppaw.passwordvault.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * NOU MODEL: AuditLog
 * Tabela pentru înregistrarea acțiunilor utilizatorilor (audit trail)
 * Echivalent adăugării unui model nou în Entity Framework Code First
 */
@Entity
@Table(name = "audit_logs", schema = "vault_schema", indexes = {
    @Index(name = "idx_audit_logs_user_id", columnList = "user_id"),
    @Index(name = "idx_audit_logs_action", columnList = "action"),
    @Index(name = "idx_audit_logs_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String action; // LOGIN, LOGOUT, CREATE_VAULT_ITEM, UPDATE_VAULT_ITEM, DELETE_VAULT_ITEM, etc.

    @Column(columnDefinition = "TEXT")
    private String description; // Descriere detaliată a acțiunii

    @Column(length = 45)
    private String ipAddress; // IP-ul utilizatorului

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

