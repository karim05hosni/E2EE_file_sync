package com.kariimhosny.filesyncserver.file.service.impl;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kariimhosny.filesyncserver.file.dto.FileMetadataDTO;
import com.kariimhosny.filesyncserver.file.entities.FileMetadata;
import com.kariimhosny.filesyncserver.file.entities.FileVersion;
import com.kariimhosny.filesyncserver.file.repositories.DekJdbcRepository;
import com.kariimhosny.filesyncserver.file.repositories.FileMetadataRepository;
import com.kariimhosny.filesyncserver.file.repositories.FileVersionRepository;
import com.kariimhosny.filesyncserver.file.service.contracts.IFileService;
import com.kariimhosny.filesyncserver.file.service.contracts.ServerStorage;


@Service
public class FileService implements IFileService {

    
    private final ServerStorage serverStorage;
    private final FileMetadataRepository fileMetadataRepository;
    private final FileVersionRepository fileVersionRepository;
    private final DekJdbcRepository dekRepository;
    private final Map<String, FileMetadataDTO> pendingUploads = new ConcurrentHashMap<>();

    public FileService(ServerStorage serverStorage, FileMetadataRepository fileMetadataRepository, DekJdbcRepository dekRepository, FileVersionRepository fileVersionRepository) {
        this.serverStorage = serverStorage;
        this.fileMetadataRepository = fileMetadataRepository;
        this.fileVersionRepository = fileVersionRepository;
        this.dekRepository = dekRepository;
    }

    public void receiveMetadata(String sessionId, TextMessage metadataMessage) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            FileMetadataDTO dto = objectMapper.readValue(((TextMessage) metadataMessage).asBytes(), FileMetadataDTO.class);
            System.out.println("thread from FileService: " + Thread.currentThread().getName());

            if ("UPLOAD".equals(dto.getAction())) {
                pendingUploads.put(sessionId, dto);
                // make & save filesMetadata entity
                FileMetadata fileMetadata = FileMetadata.builder()
                        .extension(dto.getExt())
                        .spaceId(dto.getSpaceId())
                        .owner(dto.getOwner())
                        .path(dto.getLocalPath())
                        
                        .build();

                FileVersion fileVersion = FileVersion.builder()
                        .versionNo(dto.getVersion())
                        .checksum(dto.getChecksum())
                        .byUserId(dto.getBy())
                        .iv(dto.getIv())
                        .build();

                FileMetadata savedMetadata = fileMetadataRepository.save(fileMetadata);
                fileVersion.setFileId(savedMetadata.getId());
                FileVersion savedVersion = fileVersionRepository.save(fileVersion);
                for (Long userId : dto.getEncryptedDEK().keySet()) {
                    dekRepository.saveDek(userId, savedVersion.getId(), dto.getEncryptedDEK().get(userId));
                    System.out.println("DEK is saved");
                }
                dto.setFileId(savedMetadata.getId());
            }
        } catch (IOException ex) {
            System.getLogger(FileService.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }

    @Override
    public FileMetadataDTO receiveFile(String sessionId, BinaryMessage fileMessage) {
        FileMetadataDTO dto = pendingUploads.get(sessionId);
        if (dto == null) {
            throw new IllegalStateException("No metadata for incoming binary message (sessionId=" + sessionId + ")");
        }
        serverStorage.saveFile(fileMessage.getPayload().array(), dto.getFileId(),  dto.getVersion(), dto.getSpaceId());
        // cleanup
        pendingUploads.remove(sessionId);
        
        System.out.println("File saved for fileId=" + dto.getFileId() + " v" + dto.getVersion());
        return dto;
    }



}
