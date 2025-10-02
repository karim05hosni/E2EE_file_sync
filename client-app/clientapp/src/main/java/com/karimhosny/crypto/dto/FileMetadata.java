package com.karimhosny.crypto.dto;

import java.util.HashMap;
import java.util.Map;

public class FileMetadata {

    private int fileId;
    private String checksum;
    private int version;
    private byte[] iv;
    private Map<Long,byte[]> encryptedDEK;
    private long size;
    private long timestamp;
    private String ext;
    private Long by;
    private Long owner;
    private Long spaceId;
    // local only
    private String localPath;
    private String action; //UPLOAD or DOWNLOAD 

    public FileMetadata() {
        encryptedDEK = new HashMap<>();
    }

    

    // getters + setters
    public String getChecksum() {
        return checksum;
    }

    public int getVersion() {
        return version;
    }

    public byte[] getIv() {
        return iv;
    }

    public Map<Long,byte[]> getEncryptedDEK() {
        return encryptedDEK;
    }

    public long getSize() {
        return size;
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

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void setIv(byte[] iv) {
        this.iv = iv;
    }

    public void setEncryptedDEKs(Map<Long,byte[]> encryptedDEKs){
        this.encryptedDEK = encryptedDEKs;
    }

    public void addEncryptedDEK(Long userId, byte[] cipherDEK) {
        encryptedDEK.put(userId, cipherDEK);
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public void setBy(Long by) {
        this.by = by;
    }

    public Long getBy() {
        return by;
    }

    public Long getOwner() {
        return owner;
    }

    public Long getSpaceId() {
        return spaceId;
    }

    public void setOwner(Long owner) {
        this.owner = owner;
    }

    public void setSpaceId(Long spaceId) {
        this.spaceId = spaceId;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }

    public void setFileId(int fileId) {
        this.fileId = fileId;
    }

    public int getFileId() {
        return fileId;
    }

}
