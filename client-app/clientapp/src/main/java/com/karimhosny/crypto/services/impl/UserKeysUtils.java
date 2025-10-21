package com.karimhosny.crypto.services.impl;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.karimhosny.auth.api.UserSession;
import com.karimhosny.auth.entities.User;
import com.karimhosny.connection.http.requests.CryptoRequests;
import com.karimhosny.crypto.dto.UserKeysView;
import com.karimhosny.crypto.entities.WrappedPrivK;
import com.karimhosny.crypto.entities.kdfMetadata;
import com.karimhosny.crypto.errorsHandling.CryptoOperationException;
import com.karimhosny.storage.services.contracts.IKeyStorageService;

public class UserKeysUtils {

    private IKeyStorageService keyStorageService;
    private CryptoRequests cryptoRequests;
    private static final SecureRandom RNG = new SecureRandom();

    public UserKeysUtils(IKeyStorageService keyStorageService, CryptoRequests cryptoRequests) {
        this.keyStorageService = keyStorageService;
        this.cryptoRequests = cryptoRequests;
    }

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
            // encode public key
            byte[] pubKeyDer = pairs.getPublic().getEncoded();
            // parse to base64, recommended for network transport
            String pubKeyB64 = Base64.getEncoder().encodeToString(pubKeyDer);
            User user = UserSession.getInstance().getCurrentUser();
            cryptoRequests.storePubK(pubKeyB64, user.getJwtToken());
        } catch (IOException | InterruptedException ex) {
            throw new CryptoOperationException("failed to init user keys", ex);
        }
    }

    private byte[] genSalt(int len) {
        byte[] s = new byte[len];
        RNG.nextBytes(s);
        return s;
    }

    public void initUMK(String password) throws IOException {
        try {
            UMKutils.initUmkArgon2(password.toCharArray(), genSalt(10));
        } catch (IOException ex) {
            throw new IOException("Failed to init UMK: ", ex);
        }
    }

    public byte[] deriveUMK(kdfMetadata umkMetadata) {
        // fetch user password
        String password = "karim1234";
        // derive umk
        return UMKutils.deriveUmkArgon2(password.toCharArray(), umkMetadata.getSalt());
    }

    public KeyPair generatePairs() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            return keyPair;
        } catch (NoSuchAlgorithmException ex) {
            throw new CryptoOperationException("failed to generate key pairs: ", ex);
        }
    }

    public PrivateKey loadPrivK() {
        try {
            // fetch privk from storage
            WrappedPrivK privKObj = keyStorageService.loadWrappedPrivateKey();
            // derive umk
            byte[] umk = loadUMK();
            // decrypt privk
            byte[] privateKeyBytes= UMKutils.decryptPrivateKeyWithUmk(privKObj, umk);
            // Step 1: Wrap the raw bytes in a PKCS8EncodedKeySpec
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);

            // Step 2: Get the KeyFactory for your algorithm (RSA in this case)
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            // Step 3: Generate the PrivateKey object
            return keyFactory.generatePrivate(keySpec);
        } catch (IOException ex) {
            throw new CryptoOperationException("Failed to load Private key: ", ex);
        } catch (NoSuchAlgorithmException ex) {
            System.getLogger(UserKeysUtils.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        } catch (InvalidKeySpecException ex) {
            System.getLogger(UserKeysUtils.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
        return null;
    }

    public byte[] loadUMK() {
        try {
            // fetch umk metadata from storage
            kdfMetadata umkMetadata = keyStorageService.loadUMK();
            // derive umk
            return deriveUMK(umkMetadata);
        } catch (IOException ex) {
            throw new CryptoOperationException("Failed to load UMK: ", ex);
        }
    }

    public Map<Long, PublicKey> fetchSpaceUsersPubKeys() {
        try {
            List<PublicKey> res = new LinkedList<>();
            // fetch space users pubkeys from the server
            List<UserKeysView> pubks = cryptoRequests.getSpaceUsersPubKeys(UserSession.getInstance().getCurrentUser().getJwtToken()).getData();
            Map<Long, PublicKey> resMap = new HashMap<>();
            // System.out.println("pubks: "+pubks.get(0).getPublicKey());
            for (UserKeysView obj : pubks) {
                String pubKeyStr = obj.getPublicKey().replaceAll("\\s", "");
                byte[] pubkBytes = Base64.getDecoder().decode(pubKeyStr);
                X509EncodedKeySpec keySpec = new X509EncodedKeySpec(pubkBytes);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                PublicKey publicKey = keyFactory.generatePublic(keySpec);
                resMap.put(obj.getUserId(), publicKey);
                res.add(publicKey);
            }
            // System.out.println("public key: " + res.get(0).toString());
            return resMap;
        } catch (IOException | InterruptedException ex) {
            throw new CryptoOperationException("failed to fetch space users pubkeys: ", ex);
        } catch (InvalidKeySpecException ex) {
            System.getLogger(UserKeysUtils.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        } catch (NoSuchAlgorithmException ex) {
            System.getLogger(UserKeysUtils.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
        return null;
    }

    public PublicKey fetchUserPublicKey() {
        try {
            // get pubk from the server
            String pubk = (String) cryptoRequests.getPubk(UserSession.getInstance().getCurrentUser().getJwtToken()).getData().get(0);
            pubk = pubk.replaceAll("\\s", "");
            byte[] pubkBytes = Base64.getDecoder().decode(pubk);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(pubkBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA"); // or "EC", depends on your key type
            PublicKey publicKey = keyFactory.generatePublic(keySpec);
            return publicKey;
        } catch (NoSuchAlgorithmException ex) {
            System.getLogger(CrytoService.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        } catch (InvalidKeySpecException ex) {
            System.getLogger(CrytoService.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        } catch (IOException ex) {

        } catch (InterruptedException ex) {
            System.getLogger(CrytoService.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
        return null;
    }

}
