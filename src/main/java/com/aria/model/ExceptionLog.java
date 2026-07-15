package com.aria.model;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "log_exception")
public class ExceptionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @Column(name = "http_status")
    private int httpStatus;

    private String path;

    private String method;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "exception_message")
    private String exceptionMessage;

    private LocalDateTime timestamp = LocalDateTime.now();

    // Getters and Setters
}
