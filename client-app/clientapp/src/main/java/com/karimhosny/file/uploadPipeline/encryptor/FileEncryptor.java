package com.karimhosny.file.uploadPipeline.encryptor;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.karimhosny.auth.api.UserSession;
import com.karimhosny.auth.entities.User;
import com.karimhosny.connection.websockets.WsClient;
import com.karimhosny.crypto.dto.EncryptedFileResult;
import com.karimhosny.crypto.dto.FileMetadata;
import com.karimhosny.crypto.services.contracts.ICryptoService;
import com.karimhosny.file.FileMetadataService;
import com.karimhosny.file.IndexManager;
import com.karimhosny.file.uploadPipeline.jobs.EncryptJob;
import com.karimhosny.file.uploadPipeline.jobs.UploadFileJob;
import com.karimhosny.file.uploadPipeline.jobs.UploadJob;
import com.karimhosny.file.uploadPipeline.jobs.UploadMetadataJob;
import com.karimhosny.file.uploadPipeline.pendingUpload.pendingUpload;
import com.karimhosny.storage.config.StorageConfig;

public class FileEncryptor implements Runnable {

    private ICryptoService cryptoService;
    private FileMetadataService fileMetadataService;
    private BlockingQueue<pendingUpload> pendingUploadsQueue;
    private BlockingQueue<EncryptJob> encryptQueue;
    private BlockingQueue<UploadJob> uploadQueue;
    private WsClient wsClient;
    private IndexManager fileIndexManager;
    private StorageConfig storageConfig;
    // Global scheduler (created once)
    private ScheduledExecutorService scheduler;

    // Track scheduled tasks per file path
    private final Map<Path, ScheduledFuture<?>> debounceMap = new ConcurrentHashMap<>();

    public FileEncryptor(ScheduledExecutorService scheduler,StorageConfig storageConfig, WsClient wsClient, IndexManager fileIndexManager, FileMetadataService fileMetadataService, ICryptoService cryptoService, BlockingQueue<pendingUpload> pendingUploadsQueue, BlockingQueue<EncryptJob> encryptQueue, BlockingQueue<UploadJob> uploadQueue) {
        this.cryptoService = cryptoService;
        this.encryptQueue = encryptQueue;
        this.pendingUploadsQueue = pendingUploadsQueue;
        this.uploadQueue = uploadQueue;
        this.fileMetadataService = fileMetadataService;
        this.wsClient = wsClient;
        this.storageConfig = storageConfig;
        this.fileIndexManager = fileIndexManager;
        this.scheduler = scheduler;
    }

    public void run() {
        System.out.println("From FileEncryptor");
        while (!Thread.currentThread().isInterrupted()) {
            try {
                EncryptJob job = encryptQueue.take();
                 System.out.println("Path: " + job.getPath() + " event: " + job.getType());
                User user = UserSession.getInstance().getCurrentUser();
                // encrypt file
                if (job.getType() == EncryptJob.Type.CREATE) {
                    EncryptedFileResult res = cryptoService.encryptFile(job.getPath());
                    res.getMetadata().setVersion(0);
                    res.getMetadata().setOwner(user.getId());
                    res.getMetadata().setSpaceId(user.getSpaceId());
                    res.getMetadata().setAction("UPLOAD");
                    // load UploadMetadataJob
                    UploadMetadataJob uploadMetadataJob = new UploadMetadataJob(res.getMetadata());
                    UploadFileJob uploadFileJob = new UploadFileJob(res.getEncryptedFileStream(), res.getMetadata());
                    // put it in upload queue
                    uploadQueue.add(uploadMetadataJob);
                    uploadQueue.add(uploadFileJob);
                }
                if (job.getType() == EncryptJob.Type.MODIFY) {
                    System.out.println("Modify Event");
                    // cancel old debounce task if exists
                    ScheduledFuture<?> oldFuture = debounceMap.get(job.getPath());
                    if (oldFuture != null && !oldFuture.isDone()) {
                        oldFuture.cancel(false);
                    }

                    // schedule new debounce task
                    Runnable task = () -> {
                        try {
                            EncryptedFileResult res = cryptoService.encryptFile(job.getPath());

                            // get fileId from indexManager
                            Integer fileId = fileIndexManager.getFileId(job.getPath().toString());
                            // lookup file metadata by fileId
                            Path metadataPath = storageConfig.getFilesMetadata().resolve("file_" + fileId + ".json");
                            // load metadata
                            FileMetadata metadata = fileMetadataService.load(metadataPath);

                            metadata.setVersion(metadata.getVersion() + 1);
                            metadata.setChecksum(res.getMetadata().getChecksum());
                            metadata.setIv(res.getMetadata().getIv());
                            // res.getMetadata().setVersion(metadata.getVersion() + 1);
                            metadata.setEncryptedDEKs(res.getMetadata().getEncryptedDEK());

                            fileMetadataService.save(metadata);
                            ObjectMapper mapper = new ObjectMapper();
                            wsClient.send("MODIFY|" + mapper.writeValueAsString(metadata));
                            // put to pendingUploadQueue
                            pendingUploadsQueue.add(new pendingUpload(metadata, res.getEncryptedFileStream()));
                            debounceMap.remove(job.getPath()); // cleanup
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    };
                    ScheduledFuture<?> newFuture = scheduler.schedule(task, 700, TimeUnit.MILLISECONDS);
                    debounceMap.put(job.getPath(), newFuture);
                }
                if (job.getType() == EncryptJob.Type.DELETE) {
                    try {
                        Integer fileId = fileIndexManager.getFileId(job.getPath().toString());
                        // lookup file metadata by fileId
                        Path metadataPath = storageConfig.getFilesMetadata().resolve("file_" + fileId + ".json");
                        // load metadata
                        FileMetadata metadata = fileMetadataService.load(metadataPath);
                        
                        metadata.setAction("DELETE");
                        ObjectMapper mapper = new ObjectMapper();
                        wsClient.send("DELETE|"+mapper.writeValueAsString(metadata));
                    } catch (JsonProcessingException ex) {
                        System.getLogger(FileEncryptor.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                    }
                }
            } catch (InterruptedException ex) {
                System.getLogger(FileEncryptor.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }
            
            
            /*
            for (EncryptJob elem : encryptQueue) {
                System.out.println("Path: " + elem.getPath() + " event: " + elem.getType());
                // encrypt file
                User user = UserSession.getInstance().getCurrentUser();
                if (elem.getType() == EncryptJob.Type.CREATE) {
                    EncryptedFileResult res = cryptoService.encryptFile(elem.getPath());
                    res.getMetadata().setVersion(0);
                    res.getMetadata().setOwner(user.getId());
                    res.getMetadata().setSpaceId(user.getSpaceId());
                    res.getMetadata().setAction("UPLOAD");
                    // load UploadMetadataJob
                    UploadMetadataJob uploadMetadataJob = new UploadMetadataJob(res.getMetadata());
                    UploadFileJob uploadFileJob = new UploadFileJob(res.getEncryptedFileStream(), res.getMetadata());
                    // put it in upload queue
                    uploadQueue.add(uploadMetadataJob);
                    uploadQueue.add(uploadFileJob);
                }
                if (elem.getType() == EncryptJob.Type.MODIFY) {
                    System.out.println("Modify Event");
                    // cancel old debounce task if exists
                    ScheduledFuture<?> oldFuture = debounceMap.get(elem.getPath());
                    if (oldFuture != null && !oldFuture.isDone()) {
                        oldFuture.cancel(false);
                    }

                    // schedule new debounce task
                    Runnable task = () -> {
                        try {
                            EncryptedFileResult res = cryptoService.encryptFile(elem.getPath());

                            // get fileId from indexManager
                            Integer fileId = fileIndexManager.getFileId(elem.getPath().toString());
                            // lookup file metadata by fileId
                            Path metadataPath = storageConfig.getFilesMetadata().resolve("file_" + fileId + ".json");
                            // load metadata
                            FileMetadata metadata = fileMetadataService.load(metadataPath);

                            metadata.setVersion(metadata.getVersion() + 1);
                            metadata.setChecksum(res.getMetadata().getChecksum());
                            metadata.setIv(res.getMetadata().getIv());
                            // res.getMetadata().setVersion(metadata.getVersion() + 1);
                            metadata.setEncryptedDEKs(res.getMetadata().getEncryptedDEK());

                            fileMetadataService.save(metadata);
                            ObjectMapper mapper = new ObjectMapper();
                            wsClient.send("MODIFY|" + mapper.writeValueAsString(metadata));
                            // put to pendingUploadQueue
                            pendingUploadsQueue.add(new pendingUpload(metadata, res.getEncryptedFileStream()));
                            debounceMap.remove(elem.getPath()); // cleanup
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    };
                    ScheduledFuture<?> newFuture = scheduler.schedule(task, 500, TimeUnit.MILLISECONDS);
                    debounceMap.put(elem.getPath(), newFuture);
                }
                
                if (elem.getType() == EncryptJob.Type.DELETE) {
                    try {
                        Integer fileId = fileIndexManager.getFileId(elem.getPath().toString());
                        // lookup file metadata by fileId
                        Path metadataPath = storageConfig.getFilesMetadata().resolve("file_" + fileId + ".json");
                        // load metadata
                        FileMetadata metadata = fileMetadataService.load(metadataPath);
                        
                        metadata.setAction("DELETE");
                        ObjectMapper mapper = new ObjectMapper();
                        wsClient.send("DELETE|"+mapper.writeValueAsString(metadata));
                    } catch (JsonProcessingException ex) {
                        System.getLogger(FileEncryptor.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                    }
                }
                encryptQueue.remove();
            }*/
        }

    }

}
