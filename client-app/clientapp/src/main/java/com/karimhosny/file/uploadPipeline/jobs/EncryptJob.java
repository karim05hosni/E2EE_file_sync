package com.karimhosny.file.uploadPipeline.jobs;

import java.nio.file.Path;

public class EncryptJob {
    public enum Type { CREATE, MODIFY, DELETE }
    private final Path path;
    private final Type type;

    public EncryptJob(Path path, Type type) {
        this.path = path;
        this.type = type;
    }

    public Path getPath() {
        return path;
    }

    public Type getType() {
        return type;
    }

    
}
