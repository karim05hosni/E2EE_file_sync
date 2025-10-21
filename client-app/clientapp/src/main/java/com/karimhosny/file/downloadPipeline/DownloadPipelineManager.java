package com.karimhosny.file.downloadPipeline;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.karimhosny.file.downloadPipeline.decryptor.Decryptor;
import com.karimhosny.file.downloadPipeline.downloader.Downloader;
import com.karimhosny.file.downloadPipeline.installer.Installer;

public class DownloadPipelineManager {
    private Downloader downloader;
    private Decryptor decryptor;
    private Installer installer;

    public DownloadPipelineManager(Downloader downloader, Decryptor decryptor, Installer installer) {
        this.downloader = downloader;
        this.decryptor = decryptor;
        this.installer = installer;
    }


    public void start() {
        System.out.println("Hello from DownloadPipeline manager");
        ExecutorService executor = Executors.newFixedThreadPool(4);
        executor.submit(downloader);
        executor.submit(decryptor);
        executor.submit(installer);
    }
}
