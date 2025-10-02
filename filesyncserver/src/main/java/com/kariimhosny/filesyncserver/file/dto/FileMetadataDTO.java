package com.kariimhosny.filesyncserver.file.dto;

import java.util.Map;

public class FileMetadataDTO {

    private String checksum;
    private int version;
    private byte[] iv;
    private Map<Long, byte[]> encryptedDEK;
    private String action; // UPLOAD or DOWNLOAD
    private long size;
    private Long by;
    private long timestamp;
    private String ext;
    private String localPath;
    private Long spaceId;
    private Long owner;
    // optional version info
    private Integer fileId;

    // getters and setters
    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void setIv(byte[] iv) {
        this.iv = iv;
    }

    public void setEncryptedDEK(Map<Long, byte[]> encryptedDEK) {
        this.encryptedDEK = encryptedDEK;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setBy(Long by) {
        this.by = by;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public String getChecksum() {
        return checksum;
    }

    public int getVersion() {
        return version;
    }

    public byte[] getIv() {
        return iv;
    }

    public Map<Long, byte[]> getEncryptedDEK() {
        return encryptedDEK;
    }

    public long getSize() {
        return size;
    }

    public Long getBy() {
        return by;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getExt() {
        return ext;
    }

    public String getLocalPath() {
        return localPath;
    }

    public Long getSpaceId() {
        return spaceId;
    }

    public Long getOwner() {
        return owner;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public void setSpaceId(Long spaceId) {
        this.spaceId = spaceId;
    }

    public void setOwner(Long owner) {
        this.owner = owner;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Integer getFileId() {
        return fileId;
    }

    public void setFileId(Integer fileId) {
        this.fileId = fileId;
    }

}
