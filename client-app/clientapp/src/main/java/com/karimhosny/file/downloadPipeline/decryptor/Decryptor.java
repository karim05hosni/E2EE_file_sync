package com.karimhosny.file.downloadPipeline.decryptor;

import java.util.concurrent.BlockingQueue;

import com.karimhosny.crypto.dto.EncryptedFileResult;
import com.karimhosny.file.EventsSuppressor;
import com.karimhosny.file.downloadPipeline.jobs.DecryptJob;
import com.karimhosny.file.downloadPipeline.jobs.InstallJob;
import com.karimhosny.storage.config.StorageConfig;
import com.karimhosny.storage.services.contracts.IFileStorageService;

public class Decryptor implements Runnable {
    private BlockingQueue<DecryptJob> decryptQueue;
    private BlockingQueue<InstallJob> InstallQueue;
    private StorageConfig storageConfig;
    private IFileStorageService fileStorageService;
    private EventsSuppressor eventsSuppressor;
    

    public Decryptor(EventsSuppressor eventsSuppressor, BlockingQueue<DecryptJob> decryptQueue, BlockingQueue<InstallJob> InstallQueue, StorageConfig storageConfig, IFileStorageService fileStorageService ) {
        this.decryptQueue = decryptQueue;
        this.InstallQueue = InstallQueue;
        this.storageConfig = storageConfig;
        this.fileStorageService = fileStorageService;
        this.eventsSuppressor = eventsSuppressor;
    }
    @Override
    public void run(){
        while (!Thread.currentThread().isInterrupted()) {
            // for (DecryptJob decryptJob : decryptQueue) {
                try {
                    DecryptJob decryptJob = decryptQueue.take();
                    EncryptedFileResult file = decryptJob.execute();
                    InstallJob installJob = new InstallJob(eventsSuppressor, storageConfig, file.getMetadata(), file.getEncryptedFileStream(), fileStorageService);
                    InstallQueue.put(installJob);
                } catch (InterruptedException ex) {
                    System.getLogger(Decryptor.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }

            // }
        }
    }
}
