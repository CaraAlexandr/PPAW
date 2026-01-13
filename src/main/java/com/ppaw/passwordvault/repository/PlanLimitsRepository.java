package com.ppaw.passwordvault.repository;

import com.ppaw.passwordvault.model.PlanLimits;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlanLimitsRepository extends JpaRepository<PlanLimits, Long> {
    
    Optional<PlanLimits> findByServicePlanId(Long servicePlanId);
}

