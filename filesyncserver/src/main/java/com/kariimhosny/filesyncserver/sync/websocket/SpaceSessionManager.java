package com.kariimhosny.filesyncserver.sync.websocket;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class SpaceSessionManager {

    // workspaceId -> (clientId -> ClientSession)
    private final Map<String, Map<String, ClientSession>> workspaceSessions = new ConcurrentHashMap<>();

    // Add client to a workspace
    public void addClient(String workspaceId, ClientSession session) {
        workspaceSessions
            .computeIfAbsent(workspaceId, k -> new ConcurrentHashMap<>())
            .put(session.getClientId(), session);
    }

    // Remove client from a workspace
    public void removeClient(String workspaceId, String clientId) {
        Map<String, ClientSession> clients = workspaceSessions.get(workspaceId);
        if (clients != null) {
            clients.remove(clientId);
            if (clients.isEmpty()) {
                workspaceSessions.remove(workspaceId);
            }
        }
    }

    // Broadcast to all except one client
    public void broadcastExcept(String workspaceId, String excludeClientId, String message) {
        Map<String, ClientSession> clients = workspaceSessions.get(workspaceId);
        if (clients != null) {
            for (ClientSession session : clients.values()) {
                if (!session.getClientId().equals(excludeClientId) && session.isOpen()) {
                    try {
                        session.sendMessage(message);
                    } catch (IOException e) {
                        e.printStackTrace(); // You can log instead
                    }
                } else {
                    System.out.println("Excluded client: "+ session.getClientId());
                }
            }
        }
    }

    // Get specific session
    public ClientSession getClient(String workspaceId, String clientId) {
        Map<String, ClientSession> clients = workspaceSessions.get(workspaceId);
        return (clients != null) ? clients.get(clientId) : null;
    }

    // Optional: broadcast to all clients in workspace
    public void broadcastToAll(String workspaceId, String message) {
        broadcastExcept(workspaceId, null, message);
    }
}
