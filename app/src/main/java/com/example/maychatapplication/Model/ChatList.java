package com.example.maychatapplication.Model;

public class ChatList {
    private String id;
    private String type;

    public ChatList(String id, String type) {
        this.id = id;
        this.type = type;
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
}
