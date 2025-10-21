package com.kariimhosny.filesyncserver.file.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kariimhosny.filesyncserver.file.entities.FileVersion;

@Repository
public interface FileVersionRepository extends JpaRepository<FileVersion, Integer> {

    @Query(value="select * from file_versions where file_id= :fileId order by version_no desc limit 1", 
    nativeQuery=true)
    FileVersion findLastVersionByFileId(@Param("fileId") Integer fileId);

}
