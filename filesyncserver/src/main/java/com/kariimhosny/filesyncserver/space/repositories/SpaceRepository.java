package com.kariimhosny.filesyncserver.space.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kariimhosny.filesyncserver.space.entities.Space;

@Repository
public interface SpaceRepository extends JpaRepository<Space, Long> {
    
    Optional<Space> findByOwner(Long ownerId);
    
    boolean existsByName(String name);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.spaceId = :spaceId")
    Integer countMembersBySpaceId(@Param("spaceId") Long spaceId);
}
