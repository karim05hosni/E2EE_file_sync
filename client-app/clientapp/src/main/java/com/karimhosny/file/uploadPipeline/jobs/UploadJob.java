package com.karimhosny.file.uploadPipeline.jobs;

import com.karimhosny.connection.websockets.WsClient;

public interface UploadJob {
    void execute(WsClient session);
}