package com.aria.model;

public class Status {

    private String status;
    private String message;
    private boolean success;

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Status(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public Status(boolean success, String status, String message) {
        this.success = success;
        this.status = status;
        this.message = message;
    }
}
