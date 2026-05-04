package com.karimhosny.file.downloadPipeline.installer;

import java.util.concurrent.BlockingQueue;

import com.karimhosny.file.downloadPipeline.jobs.InstallJob;

public class Installer implements  Runnable {
    private BlockingQueue<InstallJob> InstallQueue;
    

    public Installer(BlockingQueue<InstallJob> InstallQueue ) {
        this.InstallQueue = InstallQueue;
    }

    @Override
    public void run() {
        System.out.println("from FileInstaller");
        while (!Thread.currentThread().isInterrupted()) {
            // for (InstallJob installJob : InstallQueue) {
                try {
                    InstallQueue.take().execute();
                    System.out.println("Install Job executing...");
                } catch (InterruptedException ex) {
                    System.getLogger(Installer.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                }
            // }
        }
    }

}
