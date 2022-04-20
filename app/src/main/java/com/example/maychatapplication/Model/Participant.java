package com.example.maychatapplication.Model;

public class Participant {
    private String id;
    private int role;
    private String partDate;

    public Participant(String id, int role, String partDate) {
        this.id = id;
        this.role = role;
        this.partDate = partDate;
    }

    public Participant() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public String getPartDate() {
        return partDate;
    }

    public void setPartDate(String partDate) {
        this.partDate = partDate;
    }
}
