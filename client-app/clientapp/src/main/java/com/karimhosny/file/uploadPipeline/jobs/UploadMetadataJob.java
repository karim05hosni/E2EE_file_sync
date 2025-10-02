package com.karimhosny.file.uploadPipeline.jobs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.karimhosny.connection.websockets.WsClient;
import com.karimhosny.crypto.dto.FileMetadata;

public class UploadMetadataJob implements UploadJob {
    private FileMetadata fileMetadata;
    // private int version;
    // private byte[] iv;
    // private String cipher_checksum;
    // private long size;
    // private int by;
    // private long timestamp;
    // private String ext;
    // private String path;

    public UploadMetadataJob(FileMetadata fileMetadata) {
        this.fileMetadata = fileMetadata;
    }
    

    
    // setters + getters

    public String toMessage() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(this.fileMetadata);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert metadata to JSON", e);
        }
    }

    @Override
    public void execute(WsClient session) {
        System.out.println(toMessage());
        session.send(toMessage());
    }

}

