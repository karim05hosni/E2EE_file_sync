package com.karimhosny.crypto.services.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.util.encoders.Hex;

import com.karimhosny.auth.api.UserSession;
import com.karimhosny.crypto.KeysManagement;
import com.karimhosny.crypto.dto.EncryptedFileResult;
import com.karimhosny.crypto.dto.FileMetadata;
import com.karimhosny.crypto.services.contracts.ICryptoService;
import com.karimhosny.storage.services.contracts.IFileStorageService;

public class CrytoService implements ICryptoService {

    private final IFileStorageService fileStorageService;
    private final UserKeysUtils userKeysUtils;
    private KeysManagement keysManagement;

    public CrytoService(KeysManagement keysManagement, IFileStorageService fileStorageService, UserKeysUtils userKeysUtils) {
        this.fileStorageService = fileStorageService;
        this.userKeysUtils = userKeysUtils;
        this.keysManagement = keysManagement;
    }

    private SecretKey generateDEK() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256);
        SecretKey key = keyGenerator.generateKey();
        return key;
    }

    private GCMParameterSpec generateIv() {
        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);
        return new GCMParameterSpec(128, iv);
    }

    private Map<Long, PublicKey> getSpaceUsersPubKeys() {
        return userKeysUtils.fetchSpaceUsersPubKeys();
    }

    private byte[] encryptDEK(byte[] dek, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(dek);
    }

    private byte[] decryptDEK(byte[] encryptedDek, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(encryptedDek);
    }

    @Override
    public EncryptedFileResult encryptFile(Path filePath) {
        try {
            InputStream file = fileStorageService.loadFile(filePath);

            SecretKey DEK = generateDEK();
            GCMParameterSpec ivSpec = generateIv();

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, DEK, ivSpec);

            // get space's users' pubkeys from the server 
            Map<Long, PublicKey> usersPubKs = userKeysUtils.fetchSpaceUsersPubKeys();
            // Encrypt DEK with users' pubkeys "use concurrency "Thread Pool" for efficiency"
            Map<Long, byte[]> encryptedDEKsMap = new HashMap<>();

            for (Long userId : usersPubKs.keySet()) {
                byte[] cipherDEK = encryptDEK(DEK.getEncoded(), usersPubKs.get(userId));
                encryptedDEKsMap.put(userId, cipherDEK);
            }

            // Compute checksum 
            String checksum = computeChecksum(file);
            file.close();

            // reopen it for encryption
            InputStream fileToEncrypt = Files.newInputStream(filePath);
            FileMetadata metadata = new FileMetadata();
            metadata.setChecksum(checksum);
            metadata.setIv(ivSpec.getIV());
            metadata.setEncryptedDEKs(encryptedDEKsMap);
            metadata.setSize(Files.size(filePath));
            metadata.setTimestamp(System.currentTimeMillis());
            metadata.setExt(getFileExtension(filePath.getFileName()));
            metadata.setLocalPath(filePath.toString());
            metadata.setBy(UserSession.getInstance().getCurrentUser().getId());
            metadata.setSpaceId(UserSession.getInstance().getCurrentUser().getSpaceId());

            return new EncryptedFileResult(new CipherInputStream(fileToEncrypt, cipher), metadata);
        } catch (NoSuchAlgorithmException ex) {
            System.getLogger(CrytoService.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        } catch (NoSuchPaddingException ex) {
            System.getLogger(CrytoService.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        } catch (IOException ex) {
            System.getLogger(CrytoService.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        } catch (InvalidKeyException ex) {
            System.getLogger(CrytoService.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        } catch (InvalidAlgorithmParameterException ex) {
            System.getLogger(CrytoService.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        } catch (Exception ex) {
            System.getLogger(CrytoService.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
        return null;
    }


    public String computeChecksum(InputStream file) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] input = file.readAllBytes();
            byte[] digest = messageDigest.digest(input);
            return Hex.toHexString(digest);
        } catch (IOException ex) {
            System.getLogger(CrytoService.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        } catch (NoSuchAlgorithmException ex) {
        }
        return null;
    }

    @Override
    public InputStream decryptFile(InputStream encryptedFile, byte[] encryptDEK, byte[] Iv) throws Exception {
            // fetch user's private key
            PrivateKey privateKey = keysManagement.getPrivateKey();
            // decrypt dek
            byte[] dekBytes = decryptDEK(encryptDEK, privateKey);
            SecretKey dek = new SecretKeySpec(dekBytes, 0, dekBytes.length, "AES");
        try {
            // decrypt file
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec ivSpec = new GCMParameterSpec(128, Iv);
            cipher.init(Cipher.DECRYPT_MODE, dek, ivSpec);
            System.out.println("File Decrypted with params: ");
            System.out.println("encryptedDEK: " + Base64.getEncoder().encodeToString(encryptDEK));
            System.out.println("IV: " + Base64.getEncoder().encodeToString(Iv));
            return new CipherInputStream(encryptedFile, cipher);
        } catch (Exception ex) {
            System.getLogger(CrytoService.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        } finally {
            // Clean sensitive data
            Arrays.fill(dekBytes, (byte) 0);
        }
        return null;
    }

    private String getFileExtension(Path filePath) {
        String filename = filePath.toString();
        if (filename == null) {
            return null;
        }
        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex >= 0) {
            return filename.substring(dotIndex + 1);
        }
        return "";
    }

}
