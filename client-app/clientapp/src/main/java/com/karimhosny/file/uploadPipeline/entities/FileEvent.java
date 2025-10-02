package com.karimhosny.file.uploadPipeline.entities;

import java.nio.file.Path;

public class FileEvent {    
    public enum Type { CREATE, MODIFY, DELETE }
    private final Path path;
    private final Type type;

    public FileEvent(Path path, Type type) {
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
