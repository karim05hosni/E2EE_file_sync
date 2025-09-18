package com.karimhosny.storage.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class StorageConfig {


    private final Path basePath;
    private final Path workspacePath;
    private final Path privateKeyPath;
    private final Path publicKeyPath;
    private final Path umkMetadataPath;

    public StorageConfig(String baseDir) throws IOException {
        this.basePath = Path.of(baseDir).toAbsolutePath();
        this.workspacePath = basePath.resolve("workspace");
        this.privateKeyPath = basePath.resolve("keys/");
        this.publicKeyPath = basePath.resolve("keys/");
        this.umkMetadataPath = basePath.resolve("keys/");
        init();
    }

    private void init() throws IOException {
        // create base
        Files.createDirectories(basePath);

        // workspace folder
        Files.createDirectories(workspacePath);

        // keys folder
        Files.createDirectories(privateKeyPath.getParent());


    }

    // getters for other services
    public Path getWorkspacePath() {
        return workspacePath;
    }

    public Path getPrivateKeyPath() {
        return privateKeyPath;
    }

    public Path getPublicKeyPath() {
        return publicKeyPath;
    }

    public Path getBasePath() {
        return basePath;
    }

    public Path getUmkMetadataPath(){
        return umkMetadataPath;
    }
}