package com.kariimhosny.filesyncserver.file.service.contracts;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.core.io.Resource;

import com.kariimhosny.filesyncserver.file.dto.FileMetadataDTO;
import com.kariimhosny.filesyncserver.file.entities.FileMetadata;

public interface IFileService {
    void receiveMetadata(String sessionId, TextMessage metadataMessage);
    void receiveMetadata(String sessionId, FileMetadataDTO metadataMessage);


    FileMetadataDTO receiveFile(String sessionId, BinaryMessage fileMessage);
    FileMetadataDTO receiveFile(MultipartFile file, FileMetadataDTO metadata );
    Resource sendFile(Integer fileId);
}
