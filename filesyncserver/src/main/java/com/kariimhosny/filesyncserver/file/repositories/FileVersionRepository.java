package com.kariimhosny.filesyncserver.file.repositories;

import com.kariimhosny.filesyncserver.file.entities.FileVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileVersionRepository extends JpaRepository<FileVersion, Integer> {
}
