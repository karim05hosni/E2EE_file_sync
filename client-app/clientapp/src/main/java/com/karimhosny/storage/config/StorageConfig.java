package com.karimhosny.storage.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Scanner;

import com.fasterxml.jackson.databind.ObjectMapper;

public class StorageConfig {

    private final Path basePath;
    private final Path workspacePath;
    private final Path privateKeyPath;
    private final Path publicKeyPath;
    private final Path umkMetadataPath;
    private final Path filesMetadata;
    private final Path configPath;
    private final Path downloadsTmpPath;
    private final Path decryptedTmpPath;


    public StorageConfig() throws IOException {
        Scanner scanner = new Scanner(System.in);
        // create config file path
        Path appDir = Paths.get("").toAbsolutePath();  // current working directory
        System.out.println("App Path: " + appDir);
        
        System.out.println("Enter your temporary config id");
        this.configPath = appDir.resolve("config_"+scanner.nextLine()+".json");
        System.out.println("resolved config path: " + this.configPath);
        // search for config file
        // create if not existed, take input baseDir, save it in json
        
        String baseDir;
        if (!Files.exists(this.configPath)) {
            // System.out.println("config file has not been found, creating new one...");
            // Files.createFile(this.configPath);
            System.out.println("new config file created successfuly");

            System.out.println("baseDirectory key has not been found, creating new one...");
            System.out.print("Enter base directory path: ");
            baseDir = scanner.nextLine();
            String json = "{ \"baseDirectory\": \"" + baseDir + "\"}";
            Files.writeString(this.configPath, json, StandardCharsets.UTF_8);
        }
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> config = mapper.readValue(this.configPath.toFile(), Map.class);
        baseDir = config.get("baseDirectory");

        if (baseDir == null) {
            
        }

        // read baseDir
        this.basePath = Path.of(baseDir).toAbsolutePath();
        this.workspacePath = basePath.resolve("workspace");
        this.privateKeyPath = basePath.resolve("keys/");
        this.publicKeyPath = basePath.resolve("keys/");
        this.umkMetadataPath = basePath.resolve("keys/");
        this.filesMetadata = basePath.resolve("filesMetadata");
        this.downloadsTmpPath = basePath.resolve("tmp/downloads");
        this.decryptedTmpPath = basePath.resolve("tmp/decrypted");
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

        Files.createDirectories(downloadsTmpPath);
        System.out.println("downloadsTmpPath folder created");

        Files.createDirectories(decryptedTmpPath);
        System.out.println("decryptedTmpPath folder created");
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

    public Path getUmkMetadataPath() {
        return umkMetadataPath;
    }

    public Path getFilesMetadata() {
        return filesMetadata;
    }

    // public Path getConfigPath() {
    //     return configPath;
    // }

    public Path getDownloadsTmpPath() {
        return downloadsTmpPath;
    }

    public Path getDecryptedTmpPath() {
        return decryptedTmpPath;
    }


}
