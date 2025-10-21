package com.karimhosny.file.downloadPipeline.downloader;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;

import com.karimhosny.crypto.dto.FileMetadata;
import com.karimhosny.crypto.services.contracts.ICryptoService;
import com.karimhosny.file.downloadPipeline.jobs.DecryptJob;
import com.karimhosny.file.downloadPipeline.jobs.DownloadJob;
import com.karimhosny.storage.services.contracts.IFileStorageService;

public class Downloader implements Runnable {

    private IFileStorageService fileStorageService;
    private BlockingQueue<DownloadJob> downloadQueue;
    private BlockingQueue<DecryptJob> decryptQueue;
    private ICryptoService cryptoService;

    

    public Downloader(IFileStorageService fileStorageService,  BlockingQueue<DownloadJob> downloadQueue, BlockingQueue<DecryptJob> decryptQueue, ICryptoService cryptoService ) {
        this.fileStorageService = fileStorageService;
        this.downloadQueue = downloadQueue;
        this.decryptQueue = decryptQueue;
        this.cryptoService = cryptoService;
    }

    @Override
    public void run() {
        System.out.println("From FileDownloader");
        while (!Thread.currentThread().isInterrupted()) {
            for (DownloadJob downloadJob : downloadQueue) {
                try {
                    
                    System.out.println("new job in Downloader");
                    // consume from downloadQueue
                    FileMetadata metadata = downloadQueue.take().execute();
                    InputStream encryptedFile = fileStorageService.openDownloadTmpFile(metadata.getFileId());
                    // produce in decryptQueue
                    decryptQueue.add(new DecryptJob(metadata, encryptedFile, cryptoService));
                } catch (InterruptedException | IOException ex) {
                    System.getLogger(Downloader.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }
            }
        }
    }

}
