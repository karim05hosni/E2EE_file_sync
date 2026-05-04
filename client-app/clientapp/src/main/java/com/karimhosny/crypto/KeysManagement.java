package com.karimhosny.crypto;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;

import com.karimhosny.crypto.entities.WrappedPrivK;
import com.karimhosny.crypto.entities.kdfMetadata;
import com.karimhosny.crypto.services.impl.UMKutils;
import com.karimhosny.storage.services.contracts.IKeyStorageService;

public class KeysManagement {
    private PrivateKey privateKey;
    private IKeyStorageService keyStorageService;

    public KeysManagement(IKeyStorageService keyStorageService, UMKutils umKUtils){
        this.keyStorageService = keyStorageService;
    }

    public void unlock(String password) throws NoSuchAlgorithmException{
        try {
            // load UMK metadata
            kdfMetadata umkMetadata = keyStorageService.loadUMK();
            // derive UMK
            byte[] UMK =  UMKutils.deriveUmkArgon2(password.toCharArray(), umkMetadata.getSalt());
            // load wrapped privk
            WrappedPrivK wrappedPrivK = keyStorageService.loadWrappedPrivateKey();
            // decrypt privk
            byte[] privateKeyBytes = UMKutils.decryptPrivateKeyWithUmk(wrappedPrivK, UMK);
            // store privk

           // Step 1: Wrap the raw bytes in a PKCS8EncodedKeySpec
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);

            // Step 2: Get the KeyFactory for your algorithm (RSA in this case)
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            // Step 3: Generate the PrivateKey object
            privateKey = keyFactory.generatePrivate(keySpec);
            Arrays.fill(UMK, (byte) 0);
            Arrays.fill(privateKeyBytes, (byte) 0);
        } catch (IOException ex) {
            System.getLogger(KeysManagement.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        } catch (InvalidKeySpecException ex) {
            System.getLogger(KeysManagement.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        } 

    }

    public PrivateKey getPrivateKey() {
        if (privateKey == null) {
            throw new IllegalStateException("Keys not unlocked");
        }
        return privateKey;
    }

    public boolean isUnlocked() {
        return privateKey != null;
    }

    public void lock(){

    }
}
