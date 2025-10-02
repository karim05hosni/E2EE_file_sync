package com.kariimhosny.filesyncserver.file.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import com.kariimhosny.filesyncserver.file.entities.Dek;

@Repository
public interface DekRepository extends JpaRepository<Dek, Long> {

    // Find all DEKs for a specific file version
    List<Dek> findByFileVersionId(Integer fileVersionId);

    // Find all DEKs for a specific user
    List<Dek> findByUserId(Long userId);

    // Optional: find DEK by file version + user
    Dek findByFileVersionIdAndUserId(Integer fileVersionId, Long userId);
}