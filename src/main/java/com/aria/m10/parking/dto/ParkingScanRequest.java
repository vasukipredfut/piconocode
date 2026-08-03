package com.aria.m10.parking.dto;

/**
 * {@code POST /api/parking/scan} request body — the Pi only ever decodes a
 * gate-pass QR (which encodes the plain {@code gp_number} string, nothing
 * else) and submits it here. IN vs OUT is never sent by the device; the
 * backend derives it from whether an open entry already exists for this
 * gate pass — see {@link com.aria.m10.parking.service.ParkingBayScanService}.
 */
public record ParkingScanRequest(String gpNumber) {
}
