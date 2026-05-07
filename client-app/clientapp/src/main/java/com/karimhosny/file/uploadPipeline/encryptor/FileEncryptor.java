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
    private final Map<Integer, pendingUpload> pendingUploadsMap;

    public FileEncryptor(Map<Integer, pendingUpload> pendingUploadsMap, ScheduledExecutorService scheduler,StorageConfig storageConfig, WsClient wsClient, IndexManager fileIndexManager, FileMetadataService fileMetadataService, ICryptoService cryptoService, BlockingQueue<pendingUpload> pendingUploadsQueue, BlockingQueue<EncryptJob> encryptQueue, BlockingQueue<UploadJob> uploadQueue) {
        this.cryptoService = cryptoService;
        this.encryptQueue = encryptQueue;
        this.pendingUploadsQueue = pendingUploadsQueue;
        this.uploadQueue = uploadQueue;
        this.fileMetadataService = fileMetadataService;
        this.wsClient = wsClient;
        this.storageConfig = storageConfig;
        this.fileIndexManager = fileIndexManager;
        this.scheduler = scheduler;
        this.pendingUploadsMap = pendingUploadsMap;
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
                /*if (job.getType() == EncryptJob.Type.MODIFY) {
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

                */
                
                if (job.getType() == EncryptJob.Type.MODIFY) {
                    debounce(job.getPath().toAbsolutePath().normalize());
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
            
            }

    }

    private void processModify(Path path) {
    try {
        // 1- encrypt file
        EncryptedFileResult res = cryptoService.encryptFile(path);

        // 2- get fileId from indexManager
        Integer fileId = fileIndexManager.getFileId(path.toString());
        Path metadataPath = storageConfig.getFilesMetadata()
                .resolve("file_" + fileId + ".json");

        // 3- load metadata
        FileMetadata metadata = fileMetadataService.load(metadataPath);

        metadata.setVersion(metadata.getVersion() + 1);
        metadata.setChecksum(res.getMetadata().getChecksum());
        metadata.setIv(res.getMetadata().getIv());
        metadata.setEncryptedDEKs(res.getMetadata().getEncryptedDEK());

        // 4- save updated metadata locally
        fileMetadataService.save(metadata);

        ObjectMapper mapper = new ObjectMapper();

        // 5- notify server about metadata change
        wsClient.send("MODIFY|" + mapper.writeValueAsString(metadata));

        // 6- put in pending upload queue
        // pendingUploadsQueue.add(new pendingUpload(
        //         metadata,
        //         res.getEncryptedFileStream()
        // ));
        pendingUploadsMap.put(metadata.getFileId(), new pendingUpload(metadata, res.getEncryptedFileStream()));
        System.out.println("File modified, added to pending uploads: " + path.toAbsolutePath().normalize());
    } catch (Exception e) {
        // replace with proper logger later
        e.printStackTrace();
    } finally {
        debounceMap.remove(path);
    }

    
}
    private void debounce(Path path) {
        ScheduledFuture<?> old = debounceMap.get(path);

        if (old != null && !old.isDone()) {
            old.cancel(false);
        }

        ScheduledFuture<?> future =
                scheduler.schedule(() -> processModify(path),
                        700,
                        TimeUnit.MILLISECONDS);

        debounceMap.put(path, future);
    }
}
