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
    private final Path filesMetadata;

    public StorageConfig(String baseDir) throws IOException {
        this.basePath = Path.of(baseDir).toAbsolutePath();
        this.workspacePath = basePath.resolve("workspace");
        this.privateKeyPath = basePath.resolve("keys/");
        this.publicKeyPath = basePath.resolve("keys/");
        this.umkMetadataPath = basePath.resolve("keys/");
        this.filesMetadata = basePath.resolve("filesMetadata");
        init();
    }

    private void init() throws IOException {
        // create base
        Files.createDirectories(basePath);
        System.out.println("base folder created");

        // workspace folder
        Files.createDirectories(workspacePath);
        System.out.println("workspace folder created");

        // keys folder
        Files.createDirectories(privateKeyPath);
        System.out.println("keys folder created");

        // filesMetadata folder
        Files.createDirectories(filesMetadata);
        System.out.println("filesMetadata folder created");

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

    public Path getFilesMetadata() {
        return filesMetadata;
    }
}