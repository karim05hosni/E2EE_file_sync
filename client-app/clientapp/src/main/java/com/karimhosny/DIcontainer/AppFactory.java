package com.karimhosny.DIcontainer;

import com.karimhosny.crypto.services.contracts.ICryptoService;
import com.karimhosny.crypto.services.impl.CrytoService;
import com.karimhosny.crypto.services.impl.UMKutils;
import com.karimhosny.setup.OnboardingManager;
import com.karimhosny.storage.config.StorageConfig;
import com.karimhosny.storage.services.contracts.IKeyStorageService;
import com.karimhosny.storage.services.impl.FileKeyStorage;

public class AppFactory {
    private final StorageConfig storageConfig;
    private final ICryptoService cryptoService;
    private final OnboardingManager onboardingManager;
    private final IKeyStorageService keyStorageService;
    private final UMKutils UMKutils;

    public AppFactory(String rootDir) throws Exception {
        this.storageConfig = new StorageConfig(rootDir);
        this.keyStorageService = new FileKeyStorage(storageConfig);
        this.UMKutils = new UMKutils(keyStorageService);
        this.cryptoService = new CrytoService(keyStorageService);
        this.onboardingManager = new OnboardingManager(storageConfig, cryptoService);
    }

    public StorageConfig getStorageConfig() {
        return storageConfig;
    }

    public ICryptoService getCryptoService() {
        return cryptoService;
    }

    public OnboardingManager getOnboardingManager() {
        return onboardingManager;
    }
}
