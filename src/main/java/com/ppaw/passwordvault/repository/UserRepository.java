package com.ppaw.passwordvault.repository;

import com.ppaw.passwordvault.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    List<User> findByIsActive(Boolean isActive);
    
    // Eager loading example - încarcă servicePlan și vaultItems
    @EntityGraph(attributePaths = {"servicePlan", "vaultItems"})
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdWithRelations(@Param("id") Long id);
    
    // Eager loading cu JOIN FETCH
    @Query("SELECT DISTINCT u FROM User u JOIN FETCH u.servicePlan LEFT JOIN FETCH u.vaultItems WHERE u.id = :id")
    Optional<User> findByIdWithEagerLoading(@Param("id") Long id);
    
    // Lazy loading - implicit (servicePlan se încarcă doar când e accesat)
    // Doar citește user-ul fără să încarce relațiile
    Optional<User> findById(Long id);
}

