package com.karimhosny.crypto.services.impl;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import com.karimhosny.crypto.entities.WrappedPrivK;
import com.karimhosny.crypto.entities.kdfMetadata;
import com.karimhosny.crypto.services.contracts.ICryptoService;
import com.karimhosny.storage.services.contracts.IKeyStorageService;

public class CrytoService implements ICryptoService {

    private IKeyStorageService keyStorageService;

    private static final SecureRandom RNG = new SecureRandom();

    public CrytoService(IKeyStorageService keyStorageService) {
        this.keyStorageService = keyStorageService;
    }

    @Override
    public void initUMK() {
        String password = "karim1234";
        try {
            UMKutils.initUmkArgon2(password.toCharArray(), genSalt(10));
        } catch (IOException ex) {
            System.getLogger(CrytoService.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }

    @Override
    public void initUserKeys() {
        try {
            // generate pairs
            KeyPair pairs = generatePairs();
            // load umk
            kdfMetadata umkMetadata = keyStorageService.loadUMK();
            byte[] umk = deriveUMK(umkMetadata);
            // encrypt privk
            WrappedPrivK privk = UMKutils.encryptPrivateKeyWithUmk(umk, pairs.getPrivate().getEncoded(), umkMetadata.getSalt());
            // persist privk
            keyStorageService.saveWrappedPrivateKey(privk);
            // send pubk to server

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private byte[] deriveUMK(kdfMetadata umkMetadata) {
        // fetch user password
        String password = "karim1234";
        // derive umk
        return UMKutils.deriveUmkArgon2(password.toCharArray(), umkMetadata.getSalt());
    }

    private KeyPair generatePairs() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            return keyPair;
        } catch (NoSuchAlgorithmException ex) {
            return null;
        }
    }


    private static byte[] genSalt(int len) {
        byte[] s = new byte[len];
        RNG.nextBytes(s);
        return s;
    }

    @Override
    public byte[] loadPrivK() {
        try {
            // fetch privk from storage
            WrappedPrivK privKObj = keyStorageService.loadWrappedPrivateKey();
            // derive umk
            byte[] umk = loadUMK();
            // decrypt privk
            return UMKutils.decryptPrivateKeyWithUmk(privKObj, umk);
        } catch (Exception e) {
            System.out.println("from CryptoService.loadPrivK()" + e);
            return null;
        }
    }

    @Override
    public byte[] loadUMK() {
        try {
            // fetch umk metadata from storage
            kdfMetadata umkMetadata = keyStorageService.loadUMK();
            // derive umk
            return deriveUMK(umkMetadata);
        } catch (IOException e) {
            System.out.println("from CryptoService.loadUMK()" + e);
            return null;
        }
    }

}
