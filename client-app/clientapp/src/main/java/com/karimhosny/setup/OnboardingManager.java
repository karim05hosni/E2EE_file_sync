package com.karimhosny.setup;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

import com.karimhosny.auth.api.UserSession;
import com.karimhosny.auth.services.contracts.IAuthService;
import com.karimhosny.crypto.KeysManagement;
import com.karimhosny.crypto.services.contracts.ICryptoService;
import com.karimhosny.crypto.services.impl.UserKeysUtils;
import com.karimhosny.storage.config.StorageConfig;

public class OnboardingManager {

    private final StorageConfig storageConfig;
    private final ICryptoService cryptoService;
    private final UserKeysUtils userKeysUtils;
    private final IAuthService authService;
    Scanner scanner = new Scanner(System.in);
    private final KeysManagement keysManagement;

    public OnboardingManager(KeysManagement keysManagement, StorageConfig storageConfig, ICryptoService cryptoService, UserKeysUtils userKeysUtils, IAuthService authService) {
        this.storageConfig = storageConfig;
        this.cryptoService = cryptoService;
        this.userKeysUtils = userKeysUtils;
        this.authService = authService;
        this.keysManagement = keysManagement;
    }

    public void run() throws Exception {
        // if first time (no keys), you can trigger onboarding logic here
        Path privKey = storageConfig.getPrivateKeyPath().resolve("wrapped_privk.json");
        Path metadataDir = storageConfig.getFilesMetadata();

        boolean hasKeys = Files.exists(privKey);
        boolean hasMetadata = Files.exists(metadataDir);

        if (hasKeys && hasMetadata) {
            System.out.println("Setup already complete — skipping onboarding.");
            System.out.print("Login: Email");
            String email = scanner.nextLine();
            System.out.print("Login: Password");
            String password = scanner.nextLine();
            authService.login(email, password);
            // init private key
            keysManagement.unlock(password);
            return;
        }

        System.out.println("First time setup detected, onboarding…");
        // 1. Initialize storage folders (already handled inside StorageConfig constructor)
        // register/login
        // authService.register("name", "email", "password");
        // 2. Initialize UMK and user keys
        System.out.print("Login: Email");
        String email = scanner.nextLine();
        System.out.print("Login: Password");
        String password = scanner.nextLine();
        authService.login(email, password);
        userKeysUtils.initUMK(UserSession.getInstance().getCurrentUser().getEmail());

        userKeysUtils.initUserKeys(password);
        // base folder creation
        return;
    }
}
