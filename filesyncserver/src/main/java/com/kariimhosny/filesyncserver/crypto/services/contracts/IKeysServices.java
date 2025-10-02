package com.kariimhosny.filesyncserver.crypto.services.contracts;

import java.security.PublicKey;
import java.util.List;
import java.util.Optional;

import com.kariimhosny.filesyncserver.crypto.dto.StorePubkRequest;
import com.kariimhosny.filesyncserver.crypto.dto.UserKeysView;
import com.kariimhosny.filesyncserver.crypto.entities.UserKey;

public interface IKeysServices {
    /**
     * Save user's public key
     * @param pubK The public key string
     * @return true if successful
     */
    boolean saveUserPubK(StorePubkRequest request);

    String getUserPubk();

    List<UserKeysView> getSpaceUsersPubks();
    
    /**
     * Save user's wrapped private key
     * @return true if successful
     */

    
    /**
     * Get user's active public key
     * @param userId The user ID
     * @return Optional containing the user key if found
     */
    Optional<UserKey> getUserActiveKey(Long userId);
}
