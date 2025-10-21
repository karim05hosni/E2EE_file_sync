package com.kariimhosny.filesyncserver.sync.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kariimhosny.filesyncserver.file.dto.FileMetadataDTO;
import com.kariimhosny.filesyncserver.file.entities.FileVersion;
import com.kariimhosny.filesyncserver.file.repositories.FileMetadataRepository;
import com.kariimhosny.filesyncserver.file.repositories.FileVersionRepository;
import com.kariimhosny.filesyncserver.file.service.impl.FileService;

@Component
public class EventsHandler extends TextWebSocketHandler {

    private final FileService fileService;
    private final WebSocketSessionManager wsSessionManager;
    private final FileMetadataRepository fileMetadataRepository;
    private final FileVersionRepository fileVersionRepository;
    private final SpaceSessionManager spaceSessionManager;

    public EventsHandler(SpaceSessionManager spaceSessionManager, FileVersionRepository fileVersionRepository, FileMetadataRepository fileMetadataRepository, WebSocketSessionManager wsSessionManager, FileService fileService) {
        this.wsSessionManager = wsSessionManager;
        this.fileService = fileService;
        this.fileMetadataRepository = fileMetadataRepository;
        this.fileVersionRepository = fileVersionRepository;
        this.spaceSessionManager = spaceSessionManager;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("thread from Events Handler: " + Thread.currentThread().getName());
        String clientId = session.getAttributes().get("id").toString();
        String spaceId = session.getAttributes().get("spaceId").toString();
        System.out.println("New client connected: " + clientId);
        wsSessionManager.addSession(clientId, session);
        System.out.println("spaceId in session attr. : " + spaceId);
        spaceSessionManager.addClient(spaceId, new ClientSession(clientId, session));
        wsSessionManager.getSession(clientId).sendMessage(new TextMessage("Welcome"));;

        System.out.println("message sent");
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        if (message instanceof TextMessage textMsg) {
            String spaceId = session.getAttributes().get("spaceId").toString();
            String clientId = session.getAttributes().get("id").toString();
            String stringMsg = message.getPayload().toString();
            System.out.println(stringMsg);
            if (stringMsg.startsWith("MODIFY")) {
                System.out.println("Received Modify Event");
                String jsonMessage = stringMsg.replaceFirst("^MODIFY\\|", "");
                // parse the message to file metadata dto
                ObjectMapper objectMapper = new ObjectMapper();
                FileMetadataDTO dto = objectMapper.readValue(jsonMessage, FileMetadataDTO.class);

                // get file metadata (version) from DB
                FileVersion serverFileVersion = fileVersionRepository.findLastVersionByFileId(dto.getFileId());

                // check the checksum
                if (!dto.getChecksum().equals(serverFileVersion.getChecksum())) {
                    if (dto.getVersion() > serverFileVersion.getVersionNo()) {
                        // Upload
                        session.sendMessage(new TextMessage("UPLOAD_REQUIRED"));
                        fileService.receiveMetadata(session.getId(), dto);
                    }
                } else {
                    String json = objectMapper.writeValueAsString(serverFileVersion);
                    // send saved metadata
                    session.sendMessage(new TextMessage("NOTHING_REQUIRED|" + json));
                }
            } else if (stringMsg.startsWith("DELETE")) {
                spaceSessionManager.broadcastExcept(spaceId, clientId, stringMsg);
            }
        } else if (message instanceof BinaryMessage binMsg) {
            FileMetadataDTO confirmedMetadata = fileService.receiveFile(session.getId(), binMsg);
            System.out.println("File saved sucessfuly: " + confirmedMetadata.getFileId());
            // Send metadata back to this client
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(confirmedMetadata);
            session.sendMessage(new TextMessage("META|" + json));
        } else {
            System.out.println("Unknown message type: " + message.getClass());
        }

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) {
        System.out.println("Client disconnected: " + session.getId());
        String clientId = session.getAttributes().get("id").toString();
        wsSessionManager.removeSession(session);
        spaceSessionManager.removeClient(session.getAttributes().get("spaceId").toString(), clientId);
    }
}
