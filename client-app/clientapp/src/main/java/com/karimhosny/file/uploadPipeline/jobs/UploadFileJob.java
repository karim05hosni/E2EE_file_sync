package com.karimhosny.file.uploadPipeline.jobs;

import java.io.IOException;
import java.io.InputStream;

import com.karimhosny.auth.api.UserSession;
import com.karimhosny.connection.http.requests.FileRequest;
import com.karimhosny.connection.websockets.WsClient;
import com.karimhosny.crypto.dto.FileMetadata;

public class UploadFileJob implements UploadJob {

    private InputStream cipherFile;
    private FileRequest fileRequests;
    private FileMetadata metadata;
    public UploadFileJob(InputStream cipherFile, FileMetadata metadata) {
        this.cipherFile = cipherFile;
        this.metadata = metadata;
    }

    @Override
    public void execute(WsClient session) {
        try {
            fileRequests = new FileRequest();
            String token = UserSession.getInstance().getCurrentUser().getJwtToken();
            fileRequests.upload(cipherFile, metadata, token);
            cipherFile.close();
        } catch (IOException ex) {
            System.getLogger(UploadFileJob.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }

}
