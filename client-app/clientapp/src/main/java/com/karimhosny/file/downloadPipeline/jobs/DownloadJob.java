package com.karimhosny.file.downloadPipeline.jobs;

import java.io.IOException;

import com.karimhosny.auth.api.UserSession;
import com.karimhosny.connection.http.requests.FileRequest;
import com.karimhosny.crypto.dto.FileMetadata;
import com.karimhosny.storage.services.contracts.IFileStorageService;

import okhttp3.Response;

public class DownloadJob {

    private FileMetadata metadata;
    private IFileStorageService fileStorage;

    public DownloadJob(FileMetadata metadata, IFileStorageService fileStorage) {
        this.metadata = metadata;
        this.fileStorage = fileStorage;
    }

    public FileMetadata execute() {
        System.out.println("from download job");
        try {
            FileRequest request = new FileRequest();
            try (Response response = request.download(metadata.getFileId(), UserSession.getInstance().getCurrentUser().getJwtToken())) {
                if (!response.isSuccessful() || response.body() == null) {
                    throw new IOException("Failed: " + response);
                }

                fileStorage.saveDownloadTmpFile(response.body().byteStream(), metadata.getFileId());
            }
            return metadata;
        } catch (IOException ex) {
            System.getLogger(DownloadJob.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
        return null;
    }
}
