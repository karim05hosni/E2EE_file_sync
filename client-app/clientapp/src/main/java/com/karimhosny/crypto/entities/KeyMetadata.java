package com.karimhosny.crypto.entities;

import java.util.Map;

/**
 * store this in privk_metadata.json, alongside wrapped_privk.bin.
 * @author Karim
 */
public class KeyMetadata {
    private String algorithm;   // e.g., "AES/GCM/NoPadding"
    private String kdf;         // e.g., "argon2id"
    private String salt;        // base64 encoded
    private Map<String, Object> params; // iteration count, memory, parallelism
    private String nonce;       // base64 for AES-GCM


    public KeyMetadata(String algorithm, String kdf, String salt, Map<String, Object> params, String nonce) {
        this.algorithm = algorithm;
        this.kdf = kdf;
        this.salt = salt;
        this.params = params;
        this.nonce = nonce;
    }

    
    // getters/setters

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public void setKdf(String kdf) {
        this.kdf = kdf;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public String getKdf() {
        return kdf;
    }

    public String getSalt() {
        return salt;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public String getNonce() {
        return nonce;
    }

    
}
