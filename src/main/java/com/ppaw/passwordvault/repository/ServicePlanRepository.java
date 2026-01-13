package com.ppaw.passwordvault.repository;

import com.ppaw.passwordvault.model.ServicePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServicePlanRepository extends JpaRepository<ServicePlan, Long> {
    
    Optional<ServicePlan> findByName(String name);
    
    List<ServicePlan> findByIsActive(Boolean isActive);
    
    // Eager loading example - va încărca planLimits în același query
    @Query("SELECT sp FROM ServicePlan sp JOIN FETCH sp.planLimits WHERE sp.id = :id")
    Optional<ServicePlan> findByIdWithLimits(@Param("id") Long id);
    
    // Eager loading pentru toate planurile active cu limitările lor
    @Query("SELECT sp FROM ServicePlan sp JOIN FETCH sp.planLimits WHERE sp.isActive = true")
    List<ServicePlan> findAllActiveWithLimits();
}

