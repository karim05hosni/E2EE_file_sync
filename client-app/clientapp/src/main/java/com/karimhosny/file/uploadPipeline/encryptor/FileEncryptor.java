package com.karimhosny.file.uploadPipeline.encryptor;

import java.util.concurrent.BlockingQueue;

import com.karimhosny.auth.api.UserSession;
import com.karimhosny.auth.entities.User;
import com.karimhosny.crypto.dto.EncryptedFileResult;
import com.karimhosny.crypto.services.contracts.ICryptoService;
import com.karimhosny.file.uploadPipeline.jobs.EncryptJob;
import com.karimhosny.file.uploadPipeline.jobs.UploadFileJob;
import com.karimhosny.file.uploadPipeline.jobs.UploadJob;
import com.karimhosny.file.uploadPipeline.jobs.UploadMetadataJob;

public class FileEncryptor implements Runnable
{
    private ICryptoService cryptoService;
    private BlockingQueue<EncryptJob> encryptQueue;
    private BlockingQueue<UploadJob> uploadQueue;

    public FileEncryptor(ICryptoService cryptoService, BlockingQueue<EncryptJob> encryptQueue, BlockingQueue<UploadJob> uploadQueue) {
        this.cryptoService = cryptoService;
        this.encryptQueue = encryptQueue;
        this.uploadQueue = uploadQueue;
    }

    public void run(){
        System.out.println("From FileEncryptor");
        while (!Thread.currentThread().isInterrupted()) { 
            for (EncryptJob elem : encryptQueue) {
                System.out.println("Path: " + elem.getPath() + " event: "+ elem.getType());
                // encrypt file
                EncryptedFileResult res= cryptoService.encryptFile(elem.getPath());
                res.getMetadata().setVersion(0);
                User user = UserSession.getInstance().getCurrentUser();

                res.getMetadata().setOwner(user.getId());
                res.getMetadata().setSpaceId(user.getSpaceId());
                res.getMetadata().setAction("UPLOAD");
                // load UploadMetadataJob
                UploadMetadataJob uploadMetadataJob = new UploadMetadataJob(res.getMetadata());
                UploadFileJob uploadFileJob = new UploadFileJob(res.getEncryptedFileStream());
                // put it in upload queue
                uploadQueue.add(uploadMetadataJob);
                uploadQueue.add(uploadFileJob);
                
                encryptQueue.remove();
            }
        }
    }
}
