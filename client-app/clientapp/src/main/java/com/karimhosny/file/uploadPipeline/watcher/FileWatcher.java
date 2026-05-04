package com.karimhosny.file.uploadPipeline.watcher;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import com.karimhosny.file.EventsSuppressor;
import com.karimhosny.file.uploadPipeline.jobs.EncryptJob;
import com.karimhosny.storage.config.StorageConfig;

public class FileWatcher implements Runnable {

    private StorageConfig storageConfig;
    private EventsSuppressor eventsSuppressor;
    private WatchService watchService;
    private Path workspace;
    // private ExecutorService executorService;
    private Map<WatchEvent.Kind<?>, EncryptJob.Type> eventMap;
    private BlockingQueue<EncryptJob> encryptQueue;

    public FileWatcher(EventsSuppressor eventsSuppressor, StorageConfig storageConfig, BlockingQueue<EncryptJob> encryptQueue) throws IOException {
        this.watchService = FileSystems.getDefault().newWatchService();
        this.workspace = storageConfig.getWorkspacePath();
        this.encryptQueue = encryptQueue;
        this.eventsSuppressor = eventsSuppressor;

        workspace.register(
                watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_DELETE
        );
        // this.executorService = Executors.newFixedThreadPool(2);
        eventMap = new HashMap();
        eventMap.put(StandardWatchEventKinds.ENTRY_CREATE, EncryptJob.Type.CREATE);
        eventMap.put(StandardWatchEventKinds.ENTRY_MODIFY, EncryptJob.Type.MODIFY);
        eventMap.put(StandardWatchEventKinds.ENTRY_DELETE, EncryptJob.Type.DELETE);
    }

    @Override
    public void run() {
        startMonitoring();
    }

    public void startMonitoring() {
        // file watch thread
        System.out.println("File watcher started");
        while (!Thread.currentThread().isInterrupted()) {
            try {
                WatchKey key = watchService.poll(300, TimeUnit.MILLISECONDS);
                if (key != null) {
                    iterateEvents(key, encryptQueue);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("File watcher interrupted: " + e.getMessage());
            }
        }
    }

    private void iterateEvents(WatchKey key, BlockingQueue<EncryptJob> encryptQueue) {
        for (WatchEvent<?> event : key.pollEvents()) {
            String fullPath = event.context().toString();
            System.out.println("From Watcher, fullPath: "+fullPath);

            // System.out.println("From Watcher, relativePath: "+relativePath);
            System.out.println("From Watcher, isSuppressed: "+ workspace.resolve(fullPath));
            if (eventsSuppressor.isSuppressed(workspace.resolve(fullPath))) {
                System.out.println("Suppressed Path: " + event.context());
                continue;
            }
            handleWatchEvent(event, encryptQueue);
        }
        key.reset();
    }

    private void handleWatchEvent(WatchEvent<?> event, BlockingQueue<EncryptJob> encryptQueue) {
        String fullPath = event.context().toString();
        if (eventsSuppressor.isSuppressed(workspace.resolve(fullPath))) {
            System.out.println("Suppressed Path: " + event.context());
            return;
        }

        // protect against null events
        EncryptJob.Type type = eventMap.get(event.kind());
        if (type == null) {
            return;
        }

        // get affected file path
        Path affectedFile = workspace.resolve((Path) event.context());
        System.out.printf("Event: %s | File: %s%n", event.kind(), affectedFile);

        // add to encrypt block queue
        try {
            encryptQueue.put(new EncryptJob(affectedFile, type));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Failed to enqueue file event: " + e.getMessage());
        }
    }

}
