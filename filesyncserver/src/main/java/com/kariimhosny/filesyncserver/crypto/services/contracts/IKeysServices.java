package com.kariimhosny.filesyncserver.crypto.services.contracts;

import java.util.Optional;

import com.kariimhosny.filesyncserver.crypto.dto.StorePubkRequest;
import com.kariimhosny.filesyncserver.crypto.dto.UserKeysRequest;
import com.kariimhosny.filesyncserver.crypto.entities.UserKey;

public interface IKeysServices {
    /**
     * Save user's public key
     * @param pubK The public key string
     * @return true if successful
     */
    boolean saveUserPubK(StorePubkRequest request);

    String getUserPubk();
    
    /**
     * Save user's wrapped private key
     * @return true if successful
     */
    boolean saveUserPrivK(UserKeysRequest request);

    
    /**
     * Get user's active public key
     * @param userId The user ID
     * @return Optional containing the user key if found
     */
    Optional<UserKey> getUserActiveKey(Long userId);
}
