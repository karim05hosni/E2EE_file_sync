package com.karimhosny.setup;

import java.nio.file.Files;

import com.karimhosny.crypto.services.contracts.ICryptoService;
import com.karimhosny.storage.config.StorageConfig;

public class OnboardingManager {
    private final StorageConfig storageConfig;
    private final ICryptoService cryptoService;

    public OnboardingManager(StorageConfig storageConfig, ICryptoService cryptoService) {
        this.storageConfig = storageConfig;
        this.cryptoService = cryptoService;
    }

    public void run() throws Exception {
        // if first time (no keys), you can trigger onboarding logic here
        if (!Files.exists(storageConfig.getPrivateKeyPath()) || !Files.exists(storageConfig.getUmkMetadataPath())) {
            System.out.println("First time setup detected, onboarding…");
        }
        // 1. Initialize storage folders (already handled inside StorageConfig constructor)

        // 2. Initialize UMK and user keys
        cryptoService.initUMK();
        cryptoService.initUserKeys();
        
        // Later you could add:
        // - connect to server
        // - register/login
        // - sync initial metadata
    }
}
