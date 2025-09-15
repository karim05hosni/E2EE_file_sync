package com.kariimhosny.filesyncserver.crypto.services.impl;

import java.util.Optional;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.kariimhosny.filesyncserver.auth.api.AuthUser;
import com.kariimhosny.filesyncserver.crypto.dto.StorePubkRequest;
import com.kariimhosny.filesyncserver.crypto.dto.UserKeysRequest;
import com.kariimhosny.filesyncserver.crypto.entities.UserKey;
import com.kariimhosny.filesyncserver.crypto.repositories.contracts.UserKeysRepository;
import com.kariimhosny.filesyncserver.crypto.services.contracts.IKeysServices;

@Service
public class KeyServicesImpl implements IKeysServices {

    private UserKeysRepository userKeysRepo;
    private AuthUser authUser;

    public KeyServicesImpl(UserKeysRepository userKeysRepo) {
        this.userKeysRepo = userKeysRepo;
    }

    @Override
    public Optional<UserKey> getUserActiveKey(Long userId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean saveUserPubK(StorePubkRequest request) {
        this.authUser = (AuthUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try {
            Long userId = authUser.getId();
            UserKey newKey = new UserKey(userId, request.getPublicKey());
            userKeysRepo.save(newKey);
            return true; // If we reach here, save was successful
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getUserPubk() {
        this.authUser = (AuthUser) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        Optional<UserKey> maybeKey = userKeysRepo.findActiveKeyByUserId(authUser.getId());

        if (maybeKey.isPresent()) {
            UserKey key = maybeKey.get();
            return key.getPublicKey();
        } else {
            throw new RuntimeException("No active public key found");
        }
    }

    @Override
    public boolean saveUserPrivK(UserKeysRequest request) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
