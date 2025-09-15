package com.kariimhosny.filesyncserver.crypto.repositories.contracts;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.kariimhosny.filesyncserver.crypto.entities.UserKey;

/**
 * Repository interface for UserKey entity operations.
 * Handles public key management for users including key rotation scenarios.
 */
@Repository
public interface UserKeysRepository extends JpaRepository<UserKey, Long> {
    
    // ===== Basic Finders =====
    
    /**
     * Find all keys for a specific user
     */
    List<UserKey> findByUserId(Long userId);
    
    /**
     * Find keys by user ID and active status
     */
    List<UserKey> findByUserIdAndIsActive(Long userId, Boolean isActive);
    
    /**
     * Find the active public key for a user
     * @param userId the user ID
     * @return the active UserKey if exists
     */
    Optional<UserKey> findByUserIdAndIsActiveTrue(Long userId);
    
    /**
     * Find all inactive keys for a user
     */
    List<UserKey> findByUserIdAndIsActiveFalse(Long userId);
    
    // ===== Custom Queries =====
    
    /**
     * Get the currently active key for a user (more explicit)
     * @return 
     */
    @Query("SELECT uk FROM UserKey uk WHERE uk.userId = :userId AND uk.isActive = true")
    Optional<UserKey> findActiveKeyByUserId(@Param("userId") Long userId);

    
    /**
     * Get all users who have at least one active key
     */
    @Query("SELECT DISTINCT uk.userId FROM UserKey uk WHERE uk.isActive = true")
    List<Long> findUsersWithActiveKeys();
    
    /**
     * Check if user has any active keys
     * @param userId
     */
    @Query("SELECT COUNT(uk) > 0 FROM UserKey uk WHERE uk.userId = :userId AND uk.isActive = true")
    boolean hasActiveKey(@Param("userId") Long userId);
    
    /**
     * Get count of active keys for a user (should typically be 1)
     */
    @Query("SELECT COUNT(uk) FROM UserKey uk WHERE uk.userId = :userId AND uk.isActive = true")
    long countActiveKeysByUserId(@Param("userId") Long userId);
    
    /**
     * Find keys by partial public key match (for debugging/admin purposes)
     */
    @Query("SELECT uk FROM UserKey uk WHERE uk.publicKey LIKE %:keyFragment%")
    List<UserKey> findByPublicKeyContaining(@Param("keyFragment") String keyFragment);
    
    // ===== Bulk Operations =====
    
    /**
     * Deactivate all keys for a specific user
     * Useful before adding a new active key during key rotation
     */
    @Modifying
    @Transactional
    @Query("UPDATE UserKey uk SET uk.isActive = false WHERE uk.userId = :userId")
    int deactivateAllKeysForUser(@Param("userId") Long userId);
    
    /**
     * Deactivate all keys except the specified one
     * Useful when promoting a key to active status
     */
    @Modifying
    @Transactional
    @Query("UPDATE UserKey uk SET uk.isActive = false WHERE uk.userId = :userId AND uk.id != :keepActiveId")
    int deactivateAllExceptOne(@Param("userId") Long userId, @Param("keepActiveId") Long keepActiveId);
    
    /**
     * Delete all inactive keys for a user (cleanup operation)
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM UserKey uk WHERE uk.userId = :userId AND uk.isActive = false")
    int deleteInactiveKeysForUser(@Param("userId") Long userId);
    
    /**
     * Bulk deactivate keys by IDs
     */
    @Modifying
    @Transactional
    @Query("UPDATE UserKey uk SET uk.isActive = false WHERE uk.id IN :keyIds")
    int deactivateKeysByIds(@Param("keyIds") List<Long> keyIds);
    
    // ===== Admin/Monitoring Queries =====
    
    /**
     * Find users with multiple active keys (potential issue)
     */
    @Query("SELECT uk.userId FROM UserKey uk WHERE uk.isActive = true GROUP BY uk.userId HAVING COUNT(uk) > 1")
    List<Long> findUsersWithMultipleActiveKeys();
    
    /**
     * Find users without any active keys
     */
    @Query("SELECT u.id FROM User u WHERE u.id NOT IN (SELECT DISTINCT uk.userId FROM UserKey uk WHERE uk.isActive = true)")
    List<Long> findUsersWithoutActiveKeys();
}
