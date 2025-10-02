package com.karimhosny.file.uploadPipeline;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.karimhosny.file.uploadPipeline.encryptor.FileEncryptor;
import com.karimhosny.file.uploadPipeline.uploader.FileUploader;
import com.karimhosny.file.uploadPipeline.watcher.FileWatcher;

public class UploadPipelineManager {
    private FileWatcher fileWatcher;
    private FileEncryptor fileEncryptor;
    private FileUploader fileUploader;

    public UploadPipelineManager(FileWatcher fileWatcher, FileEncryptor fileEncryptor, FileUploader fileUploader) {
        this.fileWatcher = fileWatcher;
        this.fileEncryptor = fileEncryptor;
        this.fileUploader = fileUploader;
    }

    
    public void start(){
        System.out.println("Hello from uploadPipeline manager");
        ExecutorService executor = Executors.newFixedThreadPool(4);
        executor.submit(fileWatcher);
        executor.submit(fileEncryptor);
        executor.submit(fileUploader);
    }

}
