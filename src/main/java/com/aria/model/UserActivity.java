package com.aria.model;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "user_activity")
public class UserActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @Column(name = "activity_type")
    private String activityType;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "activity_time")
    private LocalDateTime activityTime;
    
    
    @Column(name = "token_expiry_time")
    private LocalDateTime tokenExpiryTime;

    // Getters and Setters
}
