package com.kariimhosny.filesyncserver.auth.repositories.contracts;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kariimhosny.filesyncserver.auth.enrtities.User;

@Repository
public interface IUserRepository extends JpaRepository<User, Long>{
    
    // Find user by email (for login)
    Optional<User> findByEmail(String email);
    
    // Check if email already exists (for registration)
    boolean existsByEmail(String email);
    
    // Find all users in a specific space
    List<User> findBySpaceId(Long spaceId);
    
    // Find users without a space (not assigned to any space yet)
    List<User> findBySpaceIdIsNull();
    
    // Update the spaceId for a user
    @Modifying
    @Query("UPDATE User u SET u.spaceId = :spaceId WHERE u.id = :userId")
    int updateUserSpaceIdById(@Param("userId") Long userId, @Param("spaceId") Long spaceId);
}