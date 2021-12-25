package com.hadjhadji.masjidna.models;

public class Notification {
    String ID;
    String title;
    String message;
    String timestamp;

    public Notification(String ID, String title, String message, String timestamp) {
        this.ID = ID;
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getMessage() {
        return message;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
