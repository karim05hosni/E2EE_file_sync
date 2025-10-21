package com.karimhosny.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.karimhosny.storage.config.StorageConfig;

public class IndexManager {

    private final StorageConfig storageConfig;
    private final Path indexPath;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, Integer> fileIndex = new HashMap<>();

    public IndexManager(StorageConfig storageConfig) {
        this.storageConfig = storageConfig;
        this.indexPath = storageConfig.getFilesMetadata().resolve("fileIndex.json");

        // Load existing index if present
        if (Files.exists(indexPath)) {
            try {
                Map<String, Integer> loaded = mapper.readValue(indexPath.toFile(),
                        new TypeReference<Map<String, Integer>>() {});
                fileIndex.putAll(loaded);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void addFile(String filePath, Integer fileId) {
        try {
            // if(fileIndex.containsKey(filePath)){
            //     return;
            // }
            fileIndex.put(filePath, fileId);
            mapper.writerWithDefaultPrettyPrinter().writeValue(indexPath.toFile(), fileIndex);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public Integer getFileId(String filePath) {
        return fileIndex.get(filePath);
    }

    public void removeFile(Path filePath) {
        fileIndex.remove(filePath.toString());
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(indexPath.toFile(), fileIndex);
        } catch (IOException ex) {
            System.out.println("from fileIndexManager: "+ex);
        }
    }
}
