package com.example.maychatapplication.Model;

public class ChatList {
    private String id;
    private String type;
    private long timeSend;

    public ChatList(String id, String type, long timeSend) {
        this.id = id;
        this.type = type;
        this.timeSend = timeSend;
    }

    public ChatList() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getTimeSend() {
        return timeSend;
    }

    public void setTimeSend(long timeSend) {
        this.timeSend = timeSend;
    }
}
