package com.example.maychatapplication.Model;

import java.io.Serializable;

public class Group implements Serializable {
    private String groupID;
    private String groupName;
    private String description;
    private String groupImage;
    private String userCreate;
    private String createDate;

    public Group(String groupID, String groupName, String description, String groupImage, String userCreate, String createDate) {
        this.groupID = groupID;
        this.groupName = groupName;
        this.description = description;
        this.groupImage = groupImage;
        this.userCreate = userCreate;
        this.createDate = createDate;
    }

    public Group() {
    }

    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGroupImage() {
        return groupImage;
    }

    public void setGroupImage(String groupImage) {
        this.groupImage = groupImage;
    }

    public String getUserCreate() {
        return userCreate;
    }

    public void setUserCreate(String userCreate) {
        this.userCreate = userCreate;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }
}
