package com.karimhosny.storage.services.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.karimhosny.crypto.entities.WrappedPrivK;
import com.karimhosny.crypto.entities.kdfMetadata;
import com.karimhosny.storage.config.StorageConfig;
import com.karimhosny.storage.services.contracts.IKeyStorageService;

public class FileKeyStorage implements IKeyStorageService {

    private final Path privateKeyPath;
    private final Path umkPath;

    public FileKeyStorage(StorageConfig config) {
        this.privateKeyPath = config.getPrivateKeyPath();
        this.umkPath = config.getUmkMetadataPath();
    }

    @Override
    public void saveWrappedPrivateKey(WrappedPrivK wrappedPrivK) throws IOException {
        Path metaFile = privateKeyPath.resolve("wrapped_privk.json");

        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(metaFile.toFile(), wrappedPrivK);

        System.out.println("private key + metadata saved successfully");
    }

    @Override
    public WrappedPrivK loadWrappedPrivateKey() throws IOException {
        Path metaFile = privateKeyPath.resolve("wrapped_privk.json");

        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(metaFile.toFile(), WrappedPrivK.class);
    }

    @Override
    public void saveUMK(kdfMetadata umkMetadata) throws IOException {

        Path metaFile = umkPath.resolve("umk_metadata.json");
        String json = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(umkMetadata);
        Files.writeString(metaFile, json);
        System.out.println("umk metadata saved successfully");
    }

    @Override
    public kdfMetadata loadUMK() throws IOException {
        Path metaFile = umkPath.resolve("umk_metadata.json");

        if (!Files.exists(metaFile)) {
            throw new FileNotFoundException("UMK metadata file not found: " + metaFile);
        }

        String json = Files.readString(metaFile);
        ObjectMapper mapper = new ObjectMapper();

        // Deserialize JSON into kdfMetadata object
        kdfMetadata umkMetadata = mapper.readValue(json, kdfMetadata.class);

        return umkMetadata;
    }

}
