package com.ppaw.passwordvault.repository;

import com.ppaw.passwordvault.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    List<AuditLog> findByUserId(Long userId);
    
    List<AuditLog> findByAction(String action);
    
    List<AuditLog> findByUserIdAndAction(Long userId, String action);
    
    List<AuditLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    // Eager loading example
    @Query("SELECT a FROM AuditLog a JOIN FETCH a.user WHERE a.id = :id")
    AuditLog findByIdWithUser(@Param("id") Long id);
}

