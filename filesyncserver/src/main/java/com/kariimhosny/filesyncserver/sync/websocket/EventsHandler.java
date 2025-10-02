package com.kariimhosny.filesyncserver.sync.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kariimhosny.filesyncserver.file.dto.FileMetadataDTO;
import com.kariimhosny.filesyncserver.file.service.impl.FileService;

@Component
public class EventsHandler extends TextWebSocketHandler {

    private final FileService fileService;
    private final WebSocketSessionManager wsSessionManager;

    public EventsHandler(WebSocketSessionManager wsSessionManager, FileService fileService) {
        this.wsSessionManager = wsSessionManager;
        this.fileService = fileService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("thread from Events Handler: " + Thread.currentThread().getName());
        System.out.println("New client connected: " + session.getId());
        wsSessionManager.addSession(session);
        session.sendMessage(new TextMessage("Welcome!"));
        System.out.println("message sent");
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        if (message instanceof TextMessage textMsg) {
            // session.getAttributes().get("auth");
            System.out.println("thread from Events Handler.handleMessage(): " + Thread.currentThread().getName());
            fileService.receiveMetadata(session.getId(), textMsg);
            session.getAttributes().put("FileMetadata", "");
            // we need to save fileId, version_no in some context to create the file path
        } else if (message instanceof BinaryMessage binMsg) {
            // handleBinary(session, binMsg);
            FileMetadataDTO confirmedMetadata = fileService.receiveFile(session.getId(), binMsg);
            System.out.println("File saved sucessfuly: "+confirmedMetadata.getFileId());
            // Send Metadata to Client
            // Send metadata back to this client
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(confirmedMetadata);
            session.sendMessage(new TextMessage("META|"+json));
        } else {
            System.out.println("Unknown message type: " + message.getClass());
        }

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) {
        System.out.println("Client disconnected: " + session.getId());
        wsSessionManager.removeSession(session);
    }
}
