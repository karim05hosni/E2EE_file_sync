package com.karimhosny.crypto.services.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;

import com.karimhosny.crypto.entities.WrappedPrivK;
import com.karimhosny.crypto.entities.kdfMetadata;
import com.karimhosny.storage.services.contracts.IKeyStorageService;

public class UMKutils {

    private static final SecureRandom RNG = new SecureRandom();
    private static IKeyStorageService keystorage;
    // Argon2 params (tune if you need stronger or faster)
    private static final int ARGON2_ITERATIONS = 3;       // time cost
    private static final int ARGON2_MEMORY_KB = 65536;   // 64 MB
    private static final int ARGON2_PARALLELISM = 1;
    private static final int UMK_LEN_BYTES = 32;         // 256-bit AES key

    // AES-GCM params
    private static final int GCM_TAG_BITS = 128;
    private static final int GCM_IV_LEN = 12; // 96-bit nonce

    public UMKutils(IKeyStorageService keystorage) {
        this.keystorage = keystorage;
    }

    public static void initUmkArgon2(char[] password, byte[] salt) throws IOException {
        kdfMetadata kdf = new kdfMetadata("argon2id", salt, ARGON2_MEMORY_KB, ARGON2_ITERATIONS, ARGON2_PARALLELISM);
        keystorage.saveUMK(kdf);
    }

    public static byte[] deriveUmkArgon2(char[] password, byte[] salt) {
        Argon2Parameters.Builder builder = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withSalt(salt)
                .withParallelism(ARGON2_PARALLELISM)
                .withIterations(ARGON2_ITERATIONS)
                .withMemoryAsKB(ARGON2_MEMORY_KB);

        Argon2BytesGenerator generator = new Argon2BytesGenerator();
        generator.init(builder.build());
        byte[] out = new byte[UMK_LEN_BYTES];
        // convert char[] -> bytes (UTF-8). Minimize lifetime.
        byte[] pwdBytes = toUtf8Bytes(password);
        // byte[] UMKpem;
        try {
            generator.generateBytes(pwdBytes, out);
            return out;
        } finally {
            // wipe temporary password bytes
            Arrays.fill(pwdBytes, (byte) 0);
        }
    }

    // --- AES-GCM wrap private key ---
    public static WrappedPrivK encryptPrivateKeyWithUmk(
            byte[] umk, byte[] privateKeyBytes, byte[] salt) throws Exception {

        byte[] iv = new byte[GCM_IV_LEN]; // e.g. 12 bytes
        RNG.nextBytes(iv);

        SecretKeySpec keySpec = new SecretKeySpec(umk, "AES");
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_BITS, iv);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, spec);

        byte[] cipherBytes = cipher.doFinal(privateKeyBytes);

        WrappedPrivK w = new WrappedPrivK();
        w.kdfAlgorithm = "argon2id";
        w.kdf_params = Map.of(
                "memory_kb", ARGON2_MEMORY_KB,
                "iterations", ARGON2_ITERATIONS,
                "parallelism", ARGON2_PARALLELISM
        );
        w.salt = Base64.getEncoder().encodeToString(salt);
        w.aes_gcm_iv = Base64.getEncoder().encodeToString(iv);
        w.ciphertext = Base64.getEncoder().encodeToString(cipherBytes);
        w.created_at = Instant.now().toString();

        // Cleanup sensitive material
        Arrays.fill(umk, (byte) 0);
        Arrays.fill(privateKeyBytes, (byte) 0);

        return w;
    }

    public static byte[] decryptPrivateKeyWithUmk(WrappedPrivK wrapped, byte[] umk) throws Exception {
        // Decode base64 fields
        byte[] iv = Base64.getDecoder().decode(wrapped.aes_gcm_iv);
        byte[] cipherBytes = Base64.getDecoder().decode(wrapped.ciphertext);

        // Reconstruct AES key from UMK
        SecretKeySpec keySpec = new SecretKeySpec(umk, "AES");

        // Setup cipher in decrypt mode
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_BITS, iv);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, spec);

        try {
            // This will both decrypt and verify GCM tag
            return cipher.doFinal(cipherBytes);
        } catch (AEADBadTagException e) {
            // Wrong UMK, corrupted ciphertext, or tampered data
            throw new SecurityException("Failed to authenticate data (bad key or modified ciphertext)", e);
        } finally {
            // Optional: wipe UMK from memory
            Arrays.fill(umk, (byte) 0);
        }
    }

    private static byte[] toUtf8Bytes(char[] chars) {
        String s = new String(chars);
        byte[] b = s.getBytes(StandardCharsets.UTF_8);
        // try to clear String? impossible reliably; but clear char[] by caller if needed
        return b;
    }

}
