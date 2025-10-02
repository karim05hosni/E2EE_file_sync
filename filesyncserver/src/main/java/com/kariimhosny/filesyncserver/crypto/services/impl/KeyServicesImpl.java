package com.kariimhosny.filesyncserver.crypto.services.impl;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.kariimhosny.filesyncserver.auth.api.AuthUser;
import com.kariimhosny.filesyncserver.crypto.dto.StorePubkRequest;
import com.kariimhosny.filesyncserver.crypto.dto.UserKeysView;
import com.kariimhosny.filesyncserver.crypto.entities.UserKey;
import com.kariimhosny.filesyncserver.crypto.repositories.contracts.UserKeysRepository;
import com.kariimhosny.filesyncserver.crypto.services.contracts.IKeysServices;

import io.jsonwebtoken.security.InvalidKeyException;
import jakarta.validation.constraints.Min;

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
            userKeysRepo.deactivateAllKeysForUser(authUser.getId());
            Long userId = authUser.getId();
            UserKey newKey = new UserKey(userId, request.getPublicKey());
            userKeysRepo.save(newKey);
            return true; // If we reach here, save was successful
        } catch (Exception e) {
            System.out.println(e);
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
    public List<UserKeysView> getSpaceUsersPubks() {
        this.authUser = (AuthUser) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        List<UserKeysView> pubks = userKeysRepo.findActiveUserPublicKeysBySpace(authUser.getSpaceId());
        for (UserKeysView elem : pubks) {
            System.out.println(elem.getUserId());
            System.out.println(elem.getPublicKey());
        }
        
        return pubks;

    }

}
