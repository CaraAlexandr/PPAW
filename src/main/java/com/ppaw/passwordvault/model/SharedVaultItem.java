package com.ppaw.passwordvault.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "shared_vault_items", schema = "vault_schema",
       uniqueConstraints = @UniqueConstraint(columnNames = {"vault_item_id", "shared_with_user_id"}),
       indexes = {
           @Index(name = "idx_shared_vault_items_shared_with", columnList = "shared_with_user_id"),
           @Index(name = "idx_shared_vault_items_vault_item", columnList = "vault_item_id")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SharedVaultItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vault_item_id", nullable = false)
    private VaultItem vaultItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_by_user_id", nullable = false)
    private User sharedByUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_with_user_id", nullable = false)
    private User sharedWithUser;

    @Column(nullable = false)
    private Boolean canEdit = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

