package com.aria.conveyor.service;

import com.aria.security.TenantIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Sweeps every tenant, marking a device OFFLINE once its heartbeat goes
 * stale. Mirrors {@code com.aria.mes.shift.ShiftRotationScheduler}'s
 * {@link TenantIterator} usage — a {@code @Scheduled} method has no HTTP
 * request and therefore no tenant context of its own, so it must iterate
 * tenants explicitly rather than defaulting to tenant 1.
 */
@Component
public class DeviceHeartbeatStalenessScheduler {

    private static final Logger log = LoggerFactory.getLogger(DeviceHeartbeatStalenessScheduler.class);

    private final DeviceHeartbeatService heartbeats;
    private final TenantIterator tenants;

    @Value("${earms.conveyor.heartbeat.staleness-sweep.enabled:true}")
    private boolean enabled;

    public DeviceHeartbeatStalenessScheduler(DeviceHeartbeatService heartbeats, TenantIterator tenants) {
        this.heartbeats = heartbeats;
        this.tenants = tenants;
    }

    @Scheduled(fixedDelayString = "${earms.conveyor.heartbeat.staleness-sweep.intervalMs:15000}", initialDelay = 30_000)
    public void sweep() {
        if (!enabled) return;
        tenants.forEach(tenantId -> {
            try {
                heartbeats.markStaleOffline();
            } catch (Exception e) {
                log.warn("DeviceHeartbeatStalenessScheduler failed for tenant {}: {}", tenantId, e.toString());
            }
        });
    }
}
