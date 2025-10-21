package com.kariimhosny.filesyncserver.sync.websocket;

import java.io.IOException;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

public class ClientSession {
    private final String clientId;
    private final WebSocketSession socket;

    public ClientSession(String clientId, WebSocketSession socket) {
        this.clientId = clientId;
        this.socket = socket;
    }

    public String getClientId() {
        return clientId;
    }

    public WebSocketSession getSocket() {
        return socket;
    }

    public boolean isOpen() {
        return socket.isOpen();
    }

    public void sendMessage(String message) throws IOException {
        if (isOpen()) {
            socket.sendMessage(new TextMessage(message));
        }
    }
}
