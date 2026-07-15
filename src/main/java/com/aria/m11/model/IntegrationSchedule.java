package com.aria.m11.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Cron-driven trigger for pull/batch/run-now executions of an instance.
 * The scheduler (Round 1H.2) walks rows where {@code enabled=true} and
 * {@code nextFireAt <= now}.
 */
@Entity
@Table(name = "integration_schedule")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntegrationSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "instance_id", nullable = false)
    private Long instanceId;

    @Column(nullable = false, length = 64)
    private String cron;

    @Column(nullable = false, length = 64)
    private String timezone;

    @Column(nullable = false)
    private Boolean enabled;

    @Column(name = "last_fired_at")
    private Instant lastFiredAt;

    @Column(name = "next_fire_at")
    private Instant nextFireAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
}
