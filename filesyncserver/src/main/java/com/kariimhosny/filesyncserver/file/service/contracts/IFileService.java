package com.kariimhosny.filesyncserver.file.service.contracts;

import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;

import com.kariimhosny.filesyncserver.file.dto.FileMetadataDTO;

public interface IFileService {
    void receiveMetadata(String sessionId, TextMessage metadataMessage);

    FileMetadataDTO receiveFile(String sessionId, BinaryMessage fileMessage);
}
