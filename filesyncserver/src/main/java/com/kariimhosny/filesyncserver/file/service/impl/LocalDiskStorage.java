package com.kariimhosny.filesyncserver.file.service.impl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kariimhosny.filesyncserver.auth.api.AuthUser;
import com.kariimhosny.filesyncserver.auth.repositories.contracts.IUserRepository;
import com.kariimhosny.filesyncserver.file.repositories.FileMetadataRepository;
import com.kariimhosny.filesyncserver.file.repositories.FileVersionRepository;
import com.kariimhosny.filesyncserver.file.service.contracts.ServerStorage;
import com.kariimhosny.filesyncserver.sync.websocket.WebSocketSessionManager;

import io.jsonwebtoken.io.IOException;
// import jakarta.persistence.criteria.Path;

@Service
public class LocalDiskStorage implements ServerStorage {

    private final FileMetadataRepository fileMetadataRepository;
    private final FileVersionRepository fileVersionRepository;
    private final ObjectMapper mapper;
    private final Path root;
    private final Path spacesPath;
    private AuthUser authUser;
    private IUserRepository userRepository;
    private final WebSocketSessionManager wsSession;
    public LocalDiskStorage(WebSocketSessionManager wsSession, IUserRepository userRepository, FileMetadataRepository fileMetadataRepository, FileVersionRepository fileVersionRepository) {
        this.fileMetadataRepository = fileMetadataRepository;
        this.fileVersionRepository = fileVersionRepository;
        this.userRepository = userRepository;
        this.wsSession = wsSession;
        this.mapper = new ObjectMapper();
        this.root = Paths.get("/server_storage");
        this.spacesPath = root.resolve("spaces");
    }

    @Override
    public void saveFile(byte[] data, Integer fileId, int version_no, Long spaceId) {
        System.out.println("Thread name from storage service: " + Thread.currentThread().getName());
        try {
            // space_id/files/fileId/v[i].bin
            String file = "spaces/" + spaceId + "/files" + "/" + fileId + "/v[" + version_no + "].bin";
            Path filePath = root.resolve(file);
            Files.createDirectories(filePath.getParent()); // ensure folders exist
            Files.write(filePath, data);
        } catch (IOException ex) {
            System.getLogger(LocalDiskStorage.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        } catch (java.io.IOException ex) {
            System.getLogger(LocalDiskStorage.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }
    public Path getFilePath(Integer fileId, Long spaceId, int version_no){
        String file = "spaces/" + spaceId + "/files" + "/" + fileId + "/v[" + version_no + "].bin";
        return root.resolve(file);
    }
    @Override
    public byte[] readFile(String path) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean exists(String path) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void saveMetadata(byte[] data) {

        // fileMetadataRepository.save(fileMetadata);
        // fileVersionRepository.save(fileVersion);
        // try {
        //     Path root = Paths.get("/server_storage");
        //     Path filePath = root.resolve("metadata");
        //     Files.createDirectories(filePath.getParent()); // ensure folders exist
        //     Files.write(filePath, data);
        // } catch (IOException ex) {
        //     System.getLogger(LocalDiskStorage.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        // }
    }

    @Override
    public void saveFileVersionDEKs(Long fileId, Long versionId, byte[] dek) {
        try {
            Path root = Paths.get("/server_storage/DEKs")
                    .resolve(fileId.toString());
            Files.createDirectories(root);

            Path filePath = root.resolve(versionId + ".dek");
            Files.write(filePath, dek);  // stores DEK bytes
        } catch (java.io.IOException ex) {

        }
    }

}
