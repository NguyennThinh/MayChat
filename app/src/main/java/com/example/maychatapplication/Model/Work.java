package com.example.maychatapplication.Model;

public class Work {
    private String workName;
    private String userCreate;
    private String workDescription;
    private String workStart;
    private String workEnd;
    private String leader;
    private long workCreateDate;
    private int workStatus;
    private String dateComplete;

    public Work(String workName, String userCreate, String workDescription, String workStart, String workEnd, String leader, long workCreateDate, int workStatus, String dateComplete) {
        this.workName = workName;
        this.userCreate = userCreate;
        this.workDescription = workDescription;
        this.workStart = workStart;
        this.workEnd = workEnd;
        this.leader = leader;
        this.workCreateDate = workCreateDate;
        this.workStatus = workStatus;
        this.dateComplete = dateComplete;
    }

    public Work() {
    }

    public String getWorkName() {
        return workName;
    }

    public void setWorkName(String workName) {
        this.workName = workName;
    }

    public String getUserCreate() {
        return userCreate;
    }

    public void setUserCreate(String userCreate) {
        this.userCreate = userCreate;
    }

    public String getWorkDescription() {
        return workDescription;
    }

    public void setWorkDescription(String workDescription) {
        this.workDescription = workDescription;
    }

    public String getWorkStart() {
        return workStart;
    }

    public void setWorkStart(String workStart) {
        this.workStart = workStart;
    }

    public String getWorkEnd() {
        return workEnd;
    }

    public void setWorkEnd(String workEnd) {
        this.workEnd = workEnd;
    }

    public String getLeader() {
        return leader;
    }

    public void setLeader(String leader) {
        this.leader = leader;
    }

    public long getWorkCreateDate() {
        return workCreateDate;
    }

    public void setWorkCreateDate(long workCreateDate) {
        this.workCreateDate = workCreateDate;
    }

    public int getWorkStatus() {
        return workStatus;
    }

    public void setWorkStatus(int workStatus) {
        this.workStatus = workStatus;
    }

    public String getDateComplete() {
        return dateComplete;
    }

    public void setDateComplete(String dateComplete) {
        this.dateComplete = dateComplete;
    }
}
