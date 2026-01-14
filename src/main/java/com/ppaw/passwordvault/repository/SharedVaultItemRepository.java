package com.ppaw.passwordvault.repository;

import com.ppaw.passwordvault.model.SharedVaultItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SharedVaultItemRepository extends JpaRepository<SharedVaultItem, Long> {
    
    List<SharedVaultItem> findBySharedWithUserId(Long userId);
    
    List<SharedVaultItem> findBySharedByUserId(Long userId);
    
    List<SharedVaultItem> findByVaultItemId(Long vaultItemId);
    
    @Query("SELECT s FROM SharedVaultItem s WHERE s.vaultItem.id = :vaultItemId AND s.sharedWithUser.id = :userId")
    Optional<SharedVaultItem> findByVaultItemIdAndSharedWithUserId(@Param("vaultItemId") Long vaultItemId, @Param("userId") Long userId);
}


