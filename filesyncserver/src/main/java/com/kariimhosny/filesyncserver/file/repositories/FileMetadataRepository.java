package com.kariimhosny.filesyncserver.file.repositories;

import com.kariimhosny.filesyncserver.file.entities.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Integer> {
}
