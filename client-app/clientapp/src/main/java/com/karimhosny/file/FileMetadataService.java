package com.karimhosny.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.karimhosny.crypto.dto.FileMetadata;
import com.karimhosny.storage.config.StorageConfig;

public class FileMetadataService {
    private final StorageConfig storageConfig;
    private final ObjectMapper mapper;
    private final Path fileMetadataPath;

    public FileMetadataService(StorageConfig storageConfig) {
        this.storageConfig = storageConfig;
        this.mapper = new ObjectMapper();
        this.fileMetadataPath = storageConfig.getFilesMetadata();
    }

    
    public void save(FileMetadata fileMetadata){
        try {
            File metadataFile = fileMetadataPath.resolve("file_"+fileMetadata.getFileId()+".json").toFile();
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(metadataFile, fileMetadata);
        } catch (IOException ex) {
            System.getLogger(FileMetadataService.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }
}
