package com.karimhosny.file.downloadPipeline.jobs;

import java.io.InputStream;

import com.karimhosny.auth.api.UserSession;
import com.karimhosny.auth.entities.User;
import com.karimhosny.crypto.dto.EncryptedFileResult;
import com.karimhosny.crypto.dto.FileMetadata;
import com.karimhosny.crypto.services.contracts.ICryptoService;

public class DecryptJob {

    private FileMetadata metadata;
    private InputStream encryptedfile;
    private ICryptoService cryptoService;

    public DecryptJob(FileMetadata metadata, InputStream encryptedfile, ICryptoService cryptoService) {
        this.metadata = metadata;
        this.encryptedfile = encryptedfile;
        this.cryptoService = cryptoService;
    }

    public EncryptedFileResult execute() {
        try {
            User authUser = UserSession.getInstance().getCurrentUser();
            byte[] encryptedDEK = metadata.getEncryptedDEK().get(authUser.getId());
            InputStream file = cryptoService.decryptFile(encryptedfile, encryptedDEK, metadata.getIv());
            EncryptedFileResult res = new EncryptedFileResult(file, metadata);
            return res;
        } catch (Exception ex) {
            System.getLogger(DecryptJob.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
        return null;
    }
}
