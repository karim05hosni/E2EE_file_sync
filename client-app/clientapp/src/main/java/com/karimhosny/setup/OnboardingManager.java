package com.karimhosny.setup;

import com.karimhosny.auth.api.UserSession;
import com.karimhosny.auth.services.contracts.IAuthService;
import com.karimhosny.crypto.services.contracts.ICryptoService;
import com.karimhosny.crypto.services.impl.UserKeysUtils;
import com.karimhosny.storage.config.StorageConfig;

public class OnboardingManager {
    private final StorageConfig storageConfig;
    private final ICryptoService cryptoService;
    private final UserKeysUtils userKeysUtils;
    private final IAuthService authService;

    public OnboardingManager(StorageConfig storageConfig, ICryptoService cryptoService, UserKeysUtils userKeysUtils, IAuthService authService) {
        this.storageConfig = storageConfig;
        this.cryptoService = cryptoService;
        this.userKeysUtils = userKeysUtils;
        this.authService = authService;
    }

    public void run() throws Exception {
        // if first time (no keys), you can trigger onboarding logic here
        // if (Files.exists(storageConfig.getPrivateKeyPath()) || Files.exists(storageConfig.getUmkMetadataPath())) {
        //     return;
        // }
        System.out.println("First time setup detected, onboarding…");
        // 1. Initialize storage folders (already handled inside StorageConfig constructor)

        // register/login
        // authService.register("name", "email", "password");
        // 2. Initialize UMK and user keys
        userKeysUtils.initUMK(UserSession.getInstance().getCurrentUser().getEmail());
        // authService.login("karim@123.com", "1234567");
        userKeysUtils.initUserKeys();

    }
}
