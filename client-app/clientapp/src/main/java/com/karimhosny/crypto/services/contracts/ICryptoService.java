package com.karimhosny.crypto.services.contracts;

import java.nio.file.Path;

import com.karimhosny.crypto.dto.EncryptedFileResult;

public interface ICryptoService {



    EncryptedFileResult encryptFile(Path filePath);

    void decryptFile();
}
