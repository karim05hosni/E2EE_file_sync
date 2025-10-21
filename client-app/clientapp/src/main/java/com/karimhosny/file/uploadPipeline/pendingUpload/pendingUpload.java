package com.karimhosny.file.uploadPipeline.pendingUpload;

import java.io.InputStream;

import com.karimhosny.crypto.dto.FileMetadata;

public class pendingUpload {
    FileMetadata metadata;
    InputStream cipherFile;

    public pendingUpload(FileMetadata metadata, InputStream cipherFile) {
        this.metadata = metadata;
        this.cipherFile = cipherFile;
    }

    public FileMetadata getmetadata() {
        return metadata;
    }

    public InputStream getCipherFile() {
        return cipherFile;
    }
    
}
