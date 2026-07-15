package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.servlet.http.HttpServletResponse;

@Getter
@Setter
@NoArgsConstructor
public class HOStatus {

    public String status;
    public String error;
    public String success;
    public String message;
    public String path;
    public Long factoryId;

    public HOStatus(String status, String error, String success, String message, String path) {
        this.status = status;
        this.error = error;
        this.success = success;
        this.message = message;
        this.path = path;
    }

    public HOStatus(String status, String error, String success, String message, String path, Long factoryId ) {
        this.status = status;
        this.error = error;
        this.success = success;
        this.message = message;
        this.path = path;
        this.factoryId = factoryId;
    }
}
