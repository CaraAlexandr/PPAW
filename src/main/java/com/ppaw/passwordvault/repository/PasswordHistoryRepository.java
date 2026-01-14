package com.ppaw.passwordvault.repository;

import com.ppaw.passwordvault.model.PasswordHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, Long> {
    
    List<PasswordHistory> findByVaultItemIdOrderByCreatedAtDesc(Long vaultItemId);
    
    @Query("SELECT COUNT(h) FROM PasswordHistory h WHERE h.vaultItem.id = :vaultItemId")
    Long countByVaultItemId(@Param("vaultItemId") Long vaultItemId);
    
    // Delete old password history versions, keeping only the most recent ones
    @Modifying
    @Query(value = "DELETE FROM vault_schema.password_history WHERE vault_item_id = :vaultItemId AND id NOT IN " +
           "(SELECT id FROM (SELECT id FROM vault_schema.password_history WHERE vault_item_id = :vaultItemId " +
           "ORDER BY created_at DESC LIMIT :keepCount) AS recent)", nativeQuery = true)
    void deleteOldVersions(@Param("vaultItemId") Long vaultItemId, @Param("keepCount") Integer keepCount);
}

