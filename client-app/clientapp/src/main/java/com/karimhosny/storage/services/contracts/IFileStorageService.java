package com.karimhosny.storage.services.contracts;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import javax.crypto.Cipher;

public interface IFileStorageService {

    InputStream loadFile(Path filePath) throws IOException;

    void saveCipherFile(InputStream file, Cipher cipher) throws IOException;

    void saveFile(InputStream file, Path outPath);

    void saveDownloadTmpFile(InputStream file, int fileId) throws IOException;
    InputStream openDownloadTmpFile(int fileId) throws IOException;

boolean deleteFile(Path path) throws IOException;
}
