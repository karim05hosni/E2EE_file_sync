package com.karimhosny.file.downloadPipeline.jobs;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import com.karimhosny.crypto.dto.FileMetadata;
import com.karimhosny.file.EventsSuppressor;
import com.karimhosny.storage.config.StorageConfig;
import com.karimhosny.storage.services.contracts.IFileStorageService;

public class InstallJob {

    private FileMetadata metadata;
    private InputStream file;
    private IFileStorageService fileStorageService;
    private StorageConfig storageConfig;
    private EventsSuppressor eventsSuppressor;

    public InstallJob(EventsSuppressor eventsSuppressor, StorageConfig storageConfig, FileMetadata metadata, InputStream file, IFileStorageService fileStorageService) {
        this.metadata = metadata;
        this.file = file;
        this.fileStorageService = fileStorageService;
        this.storageConfig = storageConfig;
        this.eventsSuppressor = eventsSuppressor;
    }

    public InstallJob(EventsSuppressor eventsSuppressor, StorageConfig storageConfig, FileMetadata metadata, IFileStorageService fileStorageService) {
        this.metadata = metadata;
        this.fileStorageService = fileStorageService;
        this.storageConfig = storageConfig;
        this.eventsSuppressor = eventsSuppressor;
    }
    
    public void execute() {
        if (metadata.getAction().equals("DELETE")) {
            try {
                System.out.println("DELETE JOB");
                String fullPath = metadata.getLocalPath();
                int index = fullPath.indexOf("workspace");
                String relativePath = fullPath.substring(index + 10);
                System.out.println("relativePath: " + relativePath);
                Path workspace = storageConfig.getWorkspacePath().resolve(relativePath);
                System.out.println("workspace: " + workspace);
                fileStorageService.deleteFile(workspace);
                System.out.println("file deleted successfuly");
                eventsSuppressor.suppress(workspace);
            } catch (IOException ex) {
                System.getLogger(InstallJob.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }
        } else {
            String fullPath = metadata.getLocalPath();
            int index = fullPath.indexOf("workspace");
            String relativePath = fullPath.substring(index + 10);
            System.out.println("relativePath: " + relativePath);
            Path workspace = storageConfig.getWorkspacePath().resolve(relativePath);
            System.out.println("workspace: " + workspace);
            eventsSuppressor.suppress(workspace);
            System.out.println("Suppressed Events in: " + workspace);
            fileStorageService.saveFile(file, workspace);
        }
    }
}
