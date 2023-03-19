package com.mobile.groupchat4.models;

public class ModelChatlist {
    String id; //need this id to get chat list, send/receiver uid

    public ModelChatlist() {
    }

    public ModelChatlist(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
