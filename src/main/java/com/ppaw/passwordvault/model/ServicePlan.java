package com.ppaw.passwordvault.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Index;

@Entity
@Table(name = "service_plans", schema = "vault_schema", indexes = {
    @Index(name = "idx_service_plans_active", columnList = "is_active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServicePlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    // MODIFICARE TIP DATE: de la VARCHAR(3) la VARCHAR(10) pentru a suporta coduri monede mai lungi
    @Column(nullable = false, length = 10)
    private String currency = "USD";

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "servicePlan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private PlanLimits planLimits;

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

