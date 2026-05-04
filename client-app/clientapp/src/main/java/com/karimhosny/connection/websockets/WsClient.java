package com.karimhosny.connection.websockets;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.karimhosny.auth.api.UserSession;
import com.karimhosny.crypto.dto.FileMetadata;
import com.karimhosny.file.EventsSuppressor;
import com.karimhosny.file.FileMetadataService;
import com.karimhosny.file.IndexManager;
import com.karimhosny.file.downloadPipeline.jobs.DownloadJob;
import com.karimhosny.file.downloadPipeline.jobs.InstallJob;
import com.karimhosny.file.uploadPipeline.jobs.UploadFileJob;
import com.karimhosny.file.uploadPipeline.jobs.UploadJob;
import com.karimhosny.file.uploadPipeline.pendingUpload.pendingUpload;
import com.karimhosny.storage.config.StorageConfig;
import com.karimhosny.storage.services.contracts.IFileStorageService;

public class WsClient implements WebSocket.Listener {

    private WebSocket webSocket;
    private FileMetadataService fileMetadataService;
    private IndexManager fileIndexManager;
    private BlockingQueue<pendingUpload> pendingUploadsQueue;
    private BlockingQueue<UploadJob> uploadQueue;
    private IFileStorageService fileStorageService;
    private BlockingQueue<DownloadJob> downloadQueue;
    private EventsSuppressor eventsSuppressor;
    private StorageConfig storageConfig;
    private BlockingQueue<InstallJob> installQueue;

    public WsClient(BlockingQueue<InstallJob> installQueue, StorageConfig storageConfig, EventsSuppressor eventsSuppressor, BlockingQueue<DownloadJob> downloadQueue, IFileStorageService fileStorageService, BlockingQueue<UploadJob> uploadQueue, BlockingQueue<pendingUpload> pendingUploadsQueue, IndexManager fileIndexManager, FileMetadataService fileMetadataService) {
        this.pendingUploadsQueue = pendingUploadsQueue;
        this.fileMetadataService = fileMetadataService;
        this.fileIndexManager = fileIndexManager;
        this.uploadQueue = uploadQueue;
        this.fileStorageService = fileStorageService;
        this.downloadQueue = downloadQueue;
        this.storageConfig = storageConfig;
        this.eventsSuppressor = eventsSuppressor;
        this.installQueue = installQueue;
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
        webSocket.request(1); //  start receiving messages
    }

    @Override
    public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
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
                String fullPath = metadata.getLocalPath();
                int index = fullPath.indexOf("workspace");
                String relativePath = fullPath.substring(index + 10);
                Path workspace = storageConfig.getWorkspacePath().resolve(relativePath);
                // System.out.println("");
                fileIndexManager.addFile(workspace.toString(), metadata.getFileId());
                // ✅ handle metadata (update index, trigger download, etc.)
                System.out.println("Received metadata for fileId: " + metadata.getFileId());
            }
            if (data.toString().startsWith("UPLOAD_REQUIRED")) {
                // take from pending uploads
                pendingUpload pending = pendingUploadsQueue.take();
                // make new upload job
                uploadQueue.add(new UploadFileJob(pending.getCipherFile(), pending.getmetadata()));
                System.out.println("added to upload queue from wsClient");
            }
            if (data.toString().startsWith("DOWNLOAD_REQUIRED|")) {
                System.out.println("download event");
                String json = data.subSequence(18, data.length()).toString();
                FileMetadata metadata = mapper.readValue(json, FileMetadata.class);
                Integer fileId = metadata.getFileId();
                FileMetadata localMetadata = fileMetadataService.load(fileId.toString());
                if (localMetadata != null) {
                    String localChecksum = localMetadata.getChecksum();
                    // System.out.println("local checksum: "+localChecksum);
                    String remoteChecksum = metadata.getChecksum();
                    // System.out.println("remote checksum"+ remoteChecksum);
                    if (!localChecksum.equals(remoteChecksum)) {
                        // download file (http request to /download endpoint
                        System.out.println("Downloading.....");
                        fileMetadataService.save(metadata);
                        System.out.println("saved metadata for downloaded file");
                        String fullPath = metadata.getLocalPath();
                        int index = fullPath.indexOf("workspace");
                        String relativePath = fullPath.substring(index + 10);
                        Path workspace = storageConfig.getWorkspacePath().resolve(relativePath);
                        // System.out.println("");
                        fileIndexManager.addFile(workspace.toString(), metadata.getFileId());
                        // ✅ handle metadata (update index, trigger download, etc.)
                        downloadQueue.add(new DownloadJob(metadata, fileStorageService));
                    }
                }
                // download without checking checksum
                // download file (http request to /download endpoint
                System.out.println("Downloading.....");
                fileMetadataService.save(metadata);
                String fullPath = metadata.getLocalPath();
                int index = fullPath.indexOf("workspace");
                String relativePath = fullPath.substring(index + 10);
                Path workspace = storageConfig.getWorkspacePath().resolve(relativePath);
                // System.out.println("");
                fileIndexManager.addFile(workspace.toString(), metadata.getFileId());
                // ✅ handle metadata (update index, trigger download, etc.)
                downloadQueue.add(new DownloadJob(metadata, fileStorageService));

            }
            if (data.toString().startsWith("DELETE|")) {
                System.out.println("Delete event");
                String json = data.subSequence(7, data.length()).toString();
                FileMetadata metadata = mapper.readValue(json, FileMetadata.class);
                InstallJob installJob = new InstallJob(eventsSuppressor, storageConfig, metadata, fileStorageService);
                installQueue.add(installJob);

                // String fullPath = metadata.getLocalPath();
                // int index = fullPath.indexOf("workspace");
                // Path relativePath = Path.of(fullPath.substring(index + 10));
                // System.out.println("relativePath: " + relativePath);
                // fileStorageService.deleteFile((Path) relativePath);
                // System.out.println("file deleted successfuly");
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
