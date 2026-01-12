package com.ppaw.passwordvault.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "plan_limits")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanLimits {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false, unique = true)
    private ServicePlan servicePlan;

    @Column(nullable = false)
    private Integer maxVaultItems = 20;

    @Column(nullable = false)
    private Integer maxPasswordLength = 16;

    @Column(nullable = false)
    private Boolean canExport = false;

    @Column(nullable = false)
    private Boolean canImport = false;

    @Column(nullable = false)
    private Boolean canShare = false;

    @Column(nullable = false)
    private Integer maxHistoryVersions = 0;

    @Column(nullable = false)
    private Boolean canAttachments = false;

    @Column(nullable = false)
    private Integer maxDevices = 1;

    @Column(nullable = false)
    private Boolean excludeAmbiguous = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

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

