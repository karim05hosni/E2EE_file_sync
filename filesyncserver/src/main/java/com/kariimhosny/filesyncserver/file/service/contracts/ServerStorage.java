package com.kariimhosny.filesyncserver.file.service.contracts;

public interface ServerStorage {
    void saveMetadata(byte[] data);
    void saveFile(byte[] data, Integer fileId, int version_no, Long spaceId);
    byte[] readFile(String path);
    void saveFileVersionDEKs(Long fileId, Long versionId, byte[] dek);
    boolean exists(String path);
    // List<String> listFiles(String directory);
}
