package com.karimhosny.crypto.dto;

import java.io.InputStream;

public class EncryptedFileResult {
    private InputStream encryptedFileStream;
    private FileMetadata metadata; // includes IV, checksum, DEK, version, etc.

    public EncryptedFileResult(InputStream encryptedFileStream, FileMetadata metadata) {
        this.encryptedFileStream = encryptedFileStream;
        this.metadata = metadata;
    }


    // getters

    public InputStream getEncryptedFileStream() {
        return encryptedFileStream;
    }

    public FileMetadata getMetadata() {
        return metadata;
    }
}