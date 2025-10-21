package com.karimhosny.crypto.services.contracts;

import java.io.InputStream;
import java.nio.file.Path;

import com.karimhosny.crypto.dto.EncryptedFileResult;

public interface ICryptoService {

    EncryptedFileResult encryptFile(Path filePath);

    InputStream decryptFile(InputStream encryptedFile, byte[] encryptDEK, byte[] Iv) throws Exception;   
}
