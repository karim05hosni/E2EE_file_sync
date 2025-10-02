package com.karimhosny.storage.services.impl;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;

import com.karimhosny.storage.config.StorageConfig;
import com.karimhosny.storage.services.contracts.IFileStorageService;

public class FileStorage implements IFileStorageService {
    private StorageConfig storageConfig;

    

    public FileStorage(StorageConfig storageConfig) {
        this.storageConfig = storageConfig;
    }

    /**
     * return input stream of a file NOTE: DON'T FORGET TO COSE THE FILE !!!
     *
     * @param filePath
     * @return
     */
    @Override
    public InputStream loadFile(Path filePath) throws IOException {
        return new FileInputStream(filePath.toFile());
    }

    @Override
    public void saveCipherFile(InputStream file, Cipher cipher) throws IOException {
        Path outPath = storageConfig.getFilesMetadata().resolve("file.bin");
        CipherInputStream cis = new CipherInputStream(file, cipher);

        try (OutputStream out = Files.newOutputStream(outPath)) {
            out.write(cipher.getIV());
            byte[] buffer = new byte[4096]; // 4 KB chunks
            int bytesRead;
            while ((bytesRead = cis.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }

}
