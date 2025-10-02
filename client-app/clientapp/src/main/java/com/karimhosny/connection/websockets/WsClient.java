package com.karimhosny.connection.websockets;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.karimhosny.auth.api.UserSession;
import com.karimhosny.crypto.dto.FileMetadata;
import com.karimhosny.file.FileMetadataService;

public class WsClient implements WebSocket.Listener {

    private WebSocket webSocket;
    private FileMetadataService fileMetadataService;

    public WsClient(FileMetadataService fileMetadataService) {
        this.fileMetadataService = fileMetadataService;
    }

    

    public void connect(String uri) {
        HttpClient client = HttpClient.newHttpClient();
        this.webSocket = client.newWebSocketBuilder()
                .header("Authorization", UserSession.getInstance().getCurrentUser().getJwtToken())
                .buildAsync(URI.create(uri), this)
                .join();
    }

    public void send(String message) {
        webSocket.sendText(message, true);
    }

    public void send(ByteBuffer file, boolean last) {
        webSocket.sendBinary(file, last);
    }

@Override
public void onOpen(WebSocket webSocket) {
    System.out.println("Connected!");
    // this.webSocket = webSocket;
    webSocket.request(1); // 🔑 start receiving messages
}
    @Override
    public CompletionStage<?> onBinary(WebSocket webSocket,ByteBuffer data,boolean last){
        webSocket.request(1);
        System.out.println("new message");
        System.out.println("Received: " + data.toString());
        return CompletableFuture.completedFuture(null);
    }
    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        webSocket.request(1);
        System.out.println("new message");
        System.out.println("Received: " + data.toString());
        ObjectMapper mapper = new ObjectMapper();
        try {
            if (data.toString().startsWith("META|")) {
                String json = data.subSequence(5, data.length()).toString();
                FileMetadata metadata = mapper.readValue(json, FileMetadata.class);
                fileMetadataService.save(metadata);
                // ✅ handle metadata (update index, trigger download, etc.)
                System.out.println("Received metadata for fileId: " + metadata.getFileId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return CompletableFuture.completedFuture(null);
    }


    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        System.err.println("Error: " + error.getMessage());
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        System.out.println("Closed: " + reason);
        return null;
    }
}
