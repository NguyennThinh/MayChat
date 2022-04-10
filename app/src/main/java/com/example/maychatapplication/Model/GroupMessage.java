package com.example.maychatapplication.Model;

public class GroupMessage {
    private String idSender;
    private String message;
    private String time;
    private String type;
    private String fileName;
    private String fileSize;
    private boolean seen;

    public GroupMessage(String idSender, String message, String time, String type, String fileName, String fileSize, boolean seen) {
        this.idSender = idSender;
        this.message = message;
        this.time = time;
        this.type = type;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.seen = seen;
    }

    public GroupMessage() {
    }

    public String getIdSender() {
        return idSender;
    }

    public void setIdSender(String idSender) {
        this.idSender = idSender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }
}
