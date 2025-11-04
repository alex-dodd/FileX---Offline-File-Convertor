package models;

import java.time.LocalDateTime;

/**
 * tHIS model is what RespRESTNS A SINGLE FILE CONVERSION RECORD.
 * This model helps us keep track of what happened during each conversion.
 */
public class ConversionRecord {
    private int id;
    private String sourcePath;
    private String targetPath;
    private String sourceFormat;
    private String targetFormat;
    private boolean success;
    private LocalDateTime timestamp;

    public ConversionRecord(String sourcePath, String targetPath, String sourceFormat, String targetFormat, boolean success) {
        this.sourcePath = sourcePath;
        this.targetPath = targetPath;
        this.sourceFormat = sourceFormat;
        this.targetFormat = targetFormat;
        this.success = success;
        this.timestamp = LocalDateTime.now();
    }

    public ConversionRecord(int id, String sourcePath, String targetPath, String sourceFormat, String targetFormat, boolean success, LocalDateTime timestamp) {
        this.id = id;
        this.sourcePath = sourcePath;
        this.targetPath = targetPath;
        this.sourceFormat = sourceFormat;
        this.targetFormat = targetFormat;
        this.success = success;
        this.timestamp = timestamp;
    }

    public ConversionRecord(String sourcePath, String targetPath, String sourceFormat, String targetFormat, boolean success, long timestamp) {
        this.sourcePath = sourcePath;
        this.targetPath = targetPath;
        this.sourceFormat = sourceFormat;
        this.targetFormat = targetFormat;
        this.success = success;
        this.timestamp = LocalDateTime.ofEpochSecond(timestamp / 1000, 0, java.time.ZoneOffset.UTC);
    }

    // My getters
    public int getId() {
        return id;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public String getSourceFormat() {
        return sourceFormat;
    }

    public String getTargetFormat() {
        return targetFormat;
    }

    public boolean isSuccess() {
        return success;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    // My setters (although, i did find out they were not needed(cause of immutability needs), I am going to keep them for future use)
    public void setId(int id) {
        this.id = id;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }

    public void setSourceFormat(String sourceFormat) {
        this.sourceFormat = sourceFormat;
    }

    public void setTargetFormat(String targetFormat) {
        this.targetFormat = targetFormat;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
