package com.karimhosny.crypto.entities;

import java.util.Map;

/**
 * makes your code cleaner when you load the private key back from disk: 
 * instead of returning two separate objects, you just return one.
 * @author Karim
 */
public class WrappedPrivK {

    private byte[] wrappedKey;
    private KeyMetadata metadata;
    public String kdfAlgorithm;
    public Map<String, Object> kdf_params;
    public String salt;
    public String aes_gcm_iv;
    public String ciphertext;
    public String created_at;

}
