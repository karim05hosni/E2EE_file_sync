package com.kariimhosny.filesyncserver.file.service.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kariimhosny.filesyncserver.auth.api.AuthUser;
import com.kariimhosny.filesyncserver.file.dto.FileMetadataDTO;
import com.kariimhosny.filesyncserver.file.entities.FileMetadata;
import com.kariimhosny.filesyncserver.file.entities.FileVersion;
import com.kariimhosny.filesyncserver.file.repositories.DekJdbcRepository;
import com.kariimhosny.filesyncserver.file.repositories.FileMetadataRepository;
import com.kariimhosny.filesyncserver.file.repositories.FileVersionRepository;
import com.kariimhosny.filesyncserver.file.service.contracts.IFileService;
import com.kariimhosny.filesyncserver.file.service.contracts.ServerStorage;
import com.kariimhosny.filesyncserver.sync.websocket.SpaceSessionManager;
import com.kariimhosny.filesyncserver.sync.websocket.WebSocketSessionManager;


@Service
public class FileService implements IFileService {

    // Action constants
    private static final String ACTION_UPLOAD = "UPLOAD";
    private static final String ACTION_DOWNLOAD = "DOWNLOAD";
    private static final String MESSAGE_PREFIX_META = "META|";
    private static final String MESSAGE_PREFIX_DOWNLOAD_REQUIRED = "DOWNLOAD_REQUIRED|";
    
    private final ServerStorage serverStorage;
    private final FileMetadataRepository fileMetadataRepository;
    private final FileVersionRepository fileVersionRepository;
    private final DekJdbcRepository dekRepository;
    private final Map<String, FileMetadataDTO> pendingUploads = new ConcurrentHashMap<>();
    private final WebSocketSessionManager session;
    private final SpaceSessionManager spaceSessions;

    public FileService(SpaceSessionManager spaceSessionManager, WebSocketSessionManager session, ServerStorage serverStorage, FileMetadataRepository fileMetadataRepository, DekJdbcRepository dekRepository, FileVersionRepository fileVersionRepository) {
        this.serverStorage = serverStorage;
        this.fileMetadataRepository = fileMetadataRepository;
        this.fileVersionRepository = fileVersionRepository;
        this.dekRepository = dekRepository;
        this.session = session;
        this.spaceSessions = spaceSessionManager;
    }

    /**
     * Receives file metadata from a DTO object and prepares for file upload.
     * 
     * @param sessionId The WebSocket session ID
     * @param dto The file metadata DTO
     */
    @Override
    public void receiveMetadata(String sessionId, FileMetadataDTO dto) {
        System.out.println("receiveMetadata: Processing metadata DTO for session " + sessionId);
        
        if (ACTION_UPLOAD.equals(dto.getAction())) {
            pendingUploads.put(sessionId, dto);
            saveFileMetadataAndVersion(dto);
        }
    }

    /**
     * Receives file metadata from a WebSocket text message and prepares for file upload.
     * 
     * @param sessionId The WebSocket session ID
     * @param metadataMessage The text message containing metadata
     */
    @Override
    public void receiveMetadata(String sessionId, TextMessage metadataMessage) {
        System.out.println("receiveMetadata: Processing text message for session " + sessionId);
        
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            FileMetadataDTO dto = objectMapper.readValue(metadataMessage.asBytes(), FileMetadataDTO.class);
            
            System.out.println("receiveMetadata: Thread " + Thread.currentThread().getName());

            if (ACTION_UPLOAD.equals(dto.getAction())) {
                pendingUploads.put(sessionId, dto);
                saveFileMetadataAndVersion(dto);
            }
        } catch (IOException ex) {
            System.getLogger(FileService.class.getName()).log(System.Logger.Level.ERROR, "receiveMetadata: Error parsing metadata", ex);
        }
    }

    /**
     * Receives file binary data via WebSocket and saves it to storage.
     * 
     * @param sessionId The WebSocket session ID
     * @param fileMessage The binary message containing file data
     * @return The file metadata DTO
     */
    @Override
    public FileMetadataDTO receiveFile(String sessionId, BinaryMessage fileMessage) {
        System.out.println("receiveFile: Processing binary message for session " + sessionId);
        
        FileMetadataDTO dto = pendingUploads.get(sessionId);
        if (dto == null) {
            throw new IllegalStateException("receiveFile: No metadata for incoming binary message (sessionId=" + sessionId + ")");
        }
        
        serverStorage.saveFile(fileMessage.getPayload().array(), dto.getFileId(), dto.getVersion(), dto.getSpaceId());
        pendingUploads.remove(sessionId);
        
        System.out.println("receiveFile: File saved for fileId=" + dto.getFileId() + " v" + dto.getVersion());
        
        return dto;
    }

    /**
     * Receives file via HTTP multipart upload and broadcasts to other clients.
     * 
     * @param file The multipart file
     * @param metadata The file metadata
     * @return The updated file metadata DTO
     */
    @Override
    public FileMetadataDTO receiveFile(MultipartFile file, FileMetadataDTO metadata) {
        System.out.println("receiveFile: Received multipart file upload");
        
        try {
            saveFileMetadataAndVersion(metadata);
            metadata.setAction(ACTION_DOWNLOAD);
            
            serverStorage.saveFile(file.getBytes(), metadata.getFileId(), metadata.getVersion(), metadata.getSpaceId());
            
            AuthUser authUser = (AuthUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(metadata);
            
            session.getSession(authUser.getId().toString()).sendMessage(new TextMessage(MESSAGE_PREFIX_META + json));
            System.out.println("receiveFile: Broadcasting to space members...");
            
            spaceSessions.broadcastExcept(authUser.getSpaceId().toString(), authUser.getId().toString(), MESSAGE_PREFIX_DOWNLOAD_REQUIRED + json);
            System.out.println("receiveFile: Broadcast completed successfully");
            
            return metadata;
        } catch (IOException ex) {
            System.getLogger(FileService.class.getName()).log(System.Logger.Level.ERROR, "receiveFile: Error processing multipart file", ex);
            return null;
        }
    }


    /**
     * Retrieves a file resource for download.
     * 
     * @param fileId The file ID
     * @return The file resource
     */
    @Override
    public Resource sendFile(Integer fileId) {
        System.out.println("sendFile: Retrieving file with ID " + fileId);
        
        try {
            FileVersion latestVersion = fileVersionRepository.findLastVersionByFileId(fileId);
            System.out.println("sendFile: Latest version found for fileId " + latestVersion.getFileId());
            
            AuthUser authUser = (AuthUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Path filePath = serverStorage.getFilePath(fileId, authUser.getSpaceId(), latestVersion.getVersionNo());
            
            System.out.println("sendFile: Cipher file found at " + filePath.toString());
            System.out.println("sendFile: Cipher file length " + filePath.toFile().length());
            
            return new UrlResource(filePath.toUri());
        } catch (MalformedURLException ex) {
            System.getLogger(FileService.class.getName()).log(System.Logger.Level.ERROR, "sendFile: Error creating file resource", ex);
        }
        return null;
    }
    
    /**
     * Private helper method to save file metadata and version entities.
     * Consolidates duplicate code from multiple methods.
     * 
     * @param dto The file metadata DTO
     */
    private void saveFileMetadataAndVersion(FileMetadataDTO dto) {
        System.out.println("saveFileMetadataAndVersion: Saving metadata for file");
        
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

        FileMetadata savedMetadata = fileMetadataRepository.findById(dto.getFileId())
                .orElseGet(() -> fileMetadataRepository.save(fileMetadata));
        
        fileVersion.setFileId(savedMetadata.getId());
        FileVersion savedVersion = fileVersionRepository.save(fileVersion);
        
        for (Long userId : dto.getEncryptedDEK().keySet()) {
            dekRepository.saveDek(userId, savedVersion.getId(), dto.getEncryptedDEK().get(userId));
            System.out.println("saveFileMetadataAndVersion: DEK saved for userId " + userId);
        }
        
        dto.setFileId(savedMetadata.getId());
    }
}
