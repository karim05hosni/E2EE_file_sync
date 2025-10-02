package com.karimhosny.DIcontainer;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.karimhosny.auth.services.contracts.IAuthService;
import com.karimhosny.auth.services.impl.AuthService;
import com.karimhosny.connection.http.config.Client;
import com.karimhosny.connection.http.requests.AuthRequests;
import com.karimhosny.connection.http.requests.CryptoRequests;
import com.karimhosny.connection.websockets.WsClient;
import com.karimhosny.crypto.services.contracts.ICryptoService;
import com.karimhosny.crypto.services.impl.CrytoService;
import com.karimhosny.crypto.services.impl.UMKutils;
import com.karimhosny.crypto.services.impl.UserKeysUtils;
import com.karimhosny.file.FileMetadataService;
import com.karimhosny.file.uploadPipeline.UploadPipelineManager;
import com.karimhosny.file.uploadPipeline.encryptor.FileEncryptor;
import com.karimhosny.file.uploadPipeline.jobs.EncryptJob;
import com.karimhosny.file.uploadPipeline.jobs.UploadJob;
import com.karimhosny.file.uploadPipeline.uploader.FileUploader;
import com.karimhosny.file.uploadPipeline.watcher.FileWatcher;
import com.karimhosny.setup.OnboardingManager;
import com.karimhosny.space.services.contracts.ISpaceService;
import com.karimhosny.space.services.impl.SpaceService;
import com.karimhosny.storage.config.StorageConfig;
import com.karimhosny.storage.services.contracts.IFileStorageService;
import com.karimhosny.storage.services.contracts.IKeyStorageService;
import com.karimhosny.storage.services.impl.FileKeyStorage;
import com.karimhosny.storage.services.impl.FileStorage;

public class AppFactory {
    private final StorageConfig storageConfig;
    private final ICryptoService cryptoService;
    private final OnboardingManager onboardingManager;
    private final IKeyStorageService keyStorageService;
    private final UMKutils UMKutils;
    private final UserKeysUtils userKeysUtils;
    private final Client client;
    private final IAuthService authService;
    private final AuthRequests authRequests;
    private final CryptoRequests cryptoRequests;
    private final FileWatcher fileWatcher;
    private final FileEncryptor fileEncryptor;
    private final FileUploader fileUploader;
    private final UploadPipelineManager uploadPipelineManager;
    private final BlockingQueue<EncryptJob> encryptQueue;
    private final BlockingQueue<UploadJob> uploadQueue;
    private final IFileStorageService fileStorageService;
    private final WsClient wsClient;
    private final ISpaceService spaceService;

    public AppFactory(String rootDir) throws Exception {
        this.storageConfig = new StorageConfig(rootDir); 
        this.keyStorageService = new FileKeyStorage(storageConfig);
        this.fileStorageService = new FileStorage(storageConfig);
        this.UMKutils = new UMKutils(keyStorageService);
        this.client = new Client("http://localhost:8080/");
        this.wsClient = new WsClient(new FileMetadataService(storageConfig));
        this.authRequests = new AuthRequests(client);
        this.cryptoRequests = new CryptoRequests(client);
        this.userKeysUtils = new UserKeysUtils(keyStorageService, cryptoRequests);
        this.cryptoService = new CrytoService(fileStorageService, userKeysUtils);
        this.authService = new AuthService(authRequests);
        this.spaceService = new SpaceService(client);
        this.onboardingManager = new OnboardingManager(storageConfig, cryptoService, userKeysUtils, authService);
        this.encryptQueue = new LinkedBlockingQueue<>();
        this.uploadQueue = new LinkedBlockingQueue<>();
        this.fileWatcher = new FileWatcher(storageConfig, encryptQueue);
        this.fileEncryptor = new FileEncryptor(cryptoService, encryptQueue, uploadQueue);
        this.fileUploader = new FileUploader(uploadQueue, wsClient);
        this.uploadPipelineManager = new UploadPipelineManager(fileWatcher, fileEncryptor, fileUploader);
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

    public IKeyStorageService getKeyStorageService() {
        return keyStorageService;
    }

    public UMKutils getUMKutils() {
        return UMKutils;
    }

    public Client getClient() {
        return client;
    }

    public IAuthService getAuthService() {
        return authService;
    }

    public AuthRequests getAuthRequests() {
        return authRequests;
    }

    public CryptoRequests getCryptoRequests() {
        return cryptoRequests;
    }

    public FileWatcher getFileWatcher() {
        return fileWatcher;
    }

    public FileEncryptor getFileEncryptor() {
        return fileEncryptor;
    }

    public UploadPipelineManager getUploadPipelineManager() {
        return uploadPipelineManager;
    }

    public BlockingQueue<EncryptJob> getEncryptQueue() {
        return encryptQueue;
    }

    public BlockingQueue<UploadJob> getUploadQueue() {
        return uploadQueue;
    }

    public IFileStorageService getFileStorageService() {
        return fileStorageService;
    }

    public WsClient getWsClient() {
        return wsClient;
    }

    public FileUploader getFileUploader() {
        return fileUploader;
    }

    public ISpaceService getSpaceService() {
        return spaceService;
    }

    public UserKeysUtils getUserKeysUtils() {
        return userKeysUtils;
    }
}
