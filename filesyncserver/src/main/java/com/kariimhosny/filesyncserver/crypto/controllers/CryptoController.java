package com.kariimhosny.filesyncserver.crypto.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kariimhosny.filesyncserver.common.dto.ApiResponse;
import com.kariimhosny.filesyncserver.crypto.dto.StorePubkRequest;
import com.kariimhosny.filesyncserver.crypto.services.contracts.IKeysServices;

import jakarta.validation.Valid;

@RestController
@RequestMapping("api/crypto")
public class CryptoController {

    private IKeysServices keysServices;

    public CryptoController(IKeysServices keysServices){
        this.keysServices = keysServices;
    }

    @PostMapping("/store-pubK")
    public ResponseEntity<ApiResponse<Boolean>> storeUserPubK(@Valid @RequestBody StorePubkRequest request){
        boolean result = keysServices.saveUserPubK(request);
        if (result) {
            return ResponseEntity.ok(ApiResponse.success(true, "Public key saved successfully"));
        } else {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to save public key"));
        }
    }

    @GetMapping("/get-pubK")
    public ResponseEntity<ApiResponse<String>> getUserPubK(){
        String publicKey = keysServices.getUserPubk();
        if (publicKey != null && !publicKey.isEmpty()) {
            System.out.println("User's pubk is retrieved successfully");
            return ResponseEntity.ok(ApiResponse.success(publicKey, "Public key retrieved successfully"));
        } else {
            return ResponseEntity.badRequest()
            .body(ApiResponse.error("Failed to get the public key"));
        }
    }
}
