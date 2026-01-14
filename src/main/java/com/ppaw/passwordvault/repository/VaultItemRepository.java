package com.ppaw.passwordvault.repository;

import com.ppaw.passwordvault.model.VaultItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VaultItemRepository extends JpaRepository<VaultItem, Long> {
    
    // Find vault items by user.id (using Spring Data JPA property path)
    @Query("SELECT v FROM VaultItem v WHERE v.user.id = :userId")
    List<VaultItem> findByUserId(@Param("userId") Long userId);
    
    // LAB 8: Method required for Task A2
    default List<VaultItem> findAllByUserId(Long userId) {
        return findByUserId(userId);
    }
    
    // LAB 8: Method required for Task A2
    @Query("SELECT v FROM VaultItem v WHERE v.id = :id AND v.user.id = :userId")
    Optional<VaultItem> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
    
    // LAB 8: Method required for Task A2
    @Modifying
    @Query("DELETE FROM VaultItem v WHERE v.id = :id AND v.user.id = :userId")
    void deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
    
    @Query("SELECT v FROM VaultItem v WHERE v.user.id = :userId AND v.isFavorite = :isFavorite")
    List<VaultItem> findByUserIdAndIsFavorite(@Param("userId") Long userId, @Param("isFavorite") Boolean isFavorite);
    
    List<VaultItem> findByFolder(String folder);
    
    // Eager loading example - încarcă user și passwordHistory
    @EntityGraph(attributePaths = {"user", "passwordHistory"})
    @Query("SELECT v FROM VaultItem v WHERE v.id = :id")
    Optional<VaultItem> findByIdWithRelations(@Param("id") Long id);
    
    // Eager loading cu JOIN FETCH
    @Query("SELECT DISTINCT v FROM VaultItem v JOIN FETCH v.user WHERE v.user.id = :userId")
    List<VaultItem> findByUserIdWithEagerLoading(@Param("userId") Long userId);
}

