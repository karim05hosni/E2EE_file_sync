package com.karimhosny.file.uploadPipeline.uploader;

import java.util.concurrent.BlockingQueue;

import com.karimhosny.connection.websockets.WsClient;
import com.karimhosny.file.uploadPipeline.jobs.UploadJob;

public class FileUploader implements Runnable{
    private BlockingQueue<UploadJob> uploadQueue;
    private WsClient wsClient;
    

    public FileUploader(BlockingQueue<UploadJob> uploadQueue, WsClient wsClient) {
        this.uploadQueue = uploadQueue;
        this.wsClient = wsClient;
    }

    @Override
    public void run() {
        System.out.println("from file uploader");
        System.out.println(uploadQueue.size());
        while (!Thread.currentThread().isInterrupted()) {
            try {
                UploadJob uploadJob = uploadQueue.take();
                uploadJob.execute(wsClient);
            } catch (Exception ex) {
                System.getLogger(FileUploader.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);

            }
            
            // for (UploadJob elem : uploadQueue) {
            //     System.out.println("elem");
            //     elem.execute(wsClient);
            //     uploadQueue.remove();
            // }
        }
    }

}
