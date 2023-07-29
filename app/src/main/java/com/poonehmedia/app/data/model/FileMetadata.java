package com.poonehmedia.app.data.model;

public class FileMetadata {
    private final String key;
    private final String mimeType;
    private final int fileSize;

    public FileMetadata(String key, String mimeType, int fileSize) {
        this.key = key;
        this.mimeType = mimeType;
        this.fileSize = fileSize;
    }

    public String getKey() {
        return key;
    }

    public String getMimeType() {
        return mimeType;
    }

    public int getFileSize() {
        return fileSize;
    }
}