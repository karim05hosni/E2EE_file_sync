package com.karimhosny.storage.services.impl;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;

import com.karimhosny.file.EventsSuppressor;
import com.karimhosny.file.IndexManager;
import com.karimhosny.storage.config.StorageConfig;
import com.karimhosny.storage.services.contracts.IFileStorageService;

public class FileStorage implements IFileStorageService {

    private StorageConfig storageConfig;
    private IndexManager fileIndex;
    private EventsSuppressor eventsSuppressor;

    public FileStorage(EventsSuppressor eventsSuppressor, StorageConfig storageConfig) {
        this.storageConfig = storageConfig;
        this.eventsSuppressor = eventsSuppressor;
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

    public void saveFile(InputStream file, Path outPath) {
        try (OutputStream out = Files.newOutputStream(outPath)) {
            byte[] buffer = new byte[4096]; // 4 KB chunks
            int bytesRead;
            while ((bytesRead = file.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        } catch (IOException ex) {
            System.getLogger(FileStorage.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }

    public void saveDownloadTmpFile(InputStream file, int fileId) throws IOException {
        Path outPath = storageConfig.getDownloadsTmpPath().resolve("file_" + fileId + ".bin");
        System.out.println("downloaded file length BEFORE saving: " + outPath.toFile().length());
        Files.write(outPath, file.readAllBytes());
    }

    public InputStream openDownloadTmpFile(int fileId) throws IOException {
        Path tmpPath = storageConfig.getDownloadsTmpPath().resolve("file_" + fileId + ".bin");
        System.out.println("downloaded file length AFTER saving: " + tmpPath.toFile().length());
        return Files.newInputStream(tmpPath);
    }

    public boolean deleteFile(Path path) {
        eventsSuppressor.suppress(path); // prevent FileWatcher from re-triggering

        // try {
            System.out.println("[DELETE] Trying to delete: " + path);

            // Ensure path exists
            if (!Files.exists(path)) {
                System.out.println("[DELETE] File not found: " + path);
                fileIndex.removeFile(path);
                return false;
            }

            // Retry logic (useful for Windows or if file is temporarily locked)
            boolean deleted = false;
            for (int i = 0; i < 5; i++) {
                if (!Files.exists(path)) {
                    System.out.println("File Deleted Successfuly");
                }
                try {
                    Files.delete(path);
                } catch (IOException ex) {
                    System.getLogger(FileStorage.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }
            }


            // System.out.println("[DELETE] Failed to delete after retries: " + path);
            return false;

        // }
    }

    public boolean deleteTmpFile(int fileId) {
        return false;
    }
;

}
