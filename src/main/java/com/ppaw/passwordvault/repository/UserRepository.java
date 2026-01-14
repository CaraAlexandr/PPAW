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
    
    // Find all users that are not deleted (soft delete filter)
    @Query("SELECT u FROM User u WHERE u.isDeleted = false")
    List<User> findAllNotDeleted();
    
    // Find user by ID that is not deleted
    @Query("SELECT u FROM User u WHERE u.id = :id AND u.isDeleted = false")
    Optional<User> findByIdNotDeleted(@Param("id") Long id);
    
    // Find user by username that is not deleted
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.isDeleted = false")
    Optional<User> findByUsernameNotDeleted(@Param("username") String username);
    
    // Find user by email that is not deleted
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isDeleted = false")
    Optional<User> findByEmailNotDeleted(@Param("email") String email);
    
    // Find user by username OR email that is not deleted (for login)
    @Query("SELECT u FROM User u WHERE (u.username = :identifier OR u.email = :identifier) AND u.isDeleted = false")
    Optional<User> findByUsernameOrEmailNotDeleted(@Param("identifier") String identifier);
    
    // Legacy methods - kept for backward compatibility but should be updated to use NotDeleted versions
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    // Find user by username OR email
    @Query("SELECT u FROM User u WHERE u.username = :identifier OR u.email = :identifier")
    Optional<User> findByUsernameOrEmail(@Param("identifier") String identifier);
    
    List<User> findByIsActive(Boolean isActive);
    
    // Eager loading example - încarcă servicePlan și vaultItems (exclude deleted)
    @EntityGraph(attributePaths = {"servicePlan", "vaultItems"})
    @Query("SELECT u FROM User u WHERE u.id = :id AND u.isDeleted = false")
    Optional<User> findByIdWithRelations(@Param("id") Long id);
    
    // Eager loading cu JOIN FETCH (exclude deleted)
    @Query("SELECT DISTINCT u FROM User u JOIN FETCH u.servicePlan LEFT JOIN FETCH u.vaultItems WHERE u.id = :id AND u.isDeleted = false")
    Optional<User> findByIdWithEagerLoading(@Param("id") Long id);
    
    // Lazy loading - implicit (servicePlan se încarcă doar când e accesat)
    // Doar citește user-ul fără să încarce relațiile
    Optional<User> findById(Long id);
}

