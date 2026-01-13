package com.ppaw.passwordvault.repository;

import com.ppaw.passwordvault.model.VaultItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VaultItemRepository extends JpaRepository<VaultItem, Long> {
    
    List<VaultItem> findByUserId(Long userId);
    
    List<VaultItem> findByUserIdAndIsFavorite(Long userId, Boolean isFavorite);
    
    List<VaultItem> findByFolder(String folder);
    
    // Eager loading example - încarcă user și passwordHistory
    @EntityGraph(attributePaths = {"user", "passwordHistory"})
    @Query("SELECT v FROM VaultItem v WHERE v.id = :id")
    Optional<VaultItem> findByIdWithRelations(@Param("id") Long id);
    
    // Eager loading cu JOIN FETCH
    @Query("SELECT DISTINCT v FROM VaultItem v JOIN FETCH v.user WHERE v.user.id = :userId")
    List<VaultItem> findByUserIdWithEagerLoading(@Param("userId") Long userId);
}

