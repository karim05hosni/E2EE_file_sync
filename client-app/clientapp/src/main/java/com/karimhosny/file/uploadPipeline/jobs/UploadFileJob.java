package com.karimhosny.file.uploadPipeline.jobs;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.karimhosny.connection.websockets.WsClient;

public class UploadFileJob implements UploadJob {

    private InputStream cipherFile;

    public UploadFileJob(InputStream cipherFile) {
        this.cipherFile = cipherFile;
    }

    @Override
    public void execute(WsClient session) {
        try {
            byte[] buffer = new byte[64 *1024]; // 64kb chunk
            
            int bytesRead;
            
            boolean first = true;
            
            while ((bytesRead = cipherFile.read(buffer)) != -1) {
                // Wrap only the valid portion of the buffer
                ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, 0, bytesRead);
                
                // For the last chunk, "last" flag must be true
                boolean last = (bytesRead < 64 *1024);
                
                session.send(byteBuffer, last);
                
                first = false;
            }
        } catch (IOException ex) {
            System.getLogger(UploadFileJob.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }

}
