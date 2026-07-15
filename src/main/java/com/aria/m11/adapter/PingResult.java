package com.aria.m11.adapter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/** Outcome of an {@link IntegrationAdapter#ping} call. */
@Data
@Builder
@AllArgsConstructor
public class PingResult {
    private boolean ok;
    private String message;
    private Integer httpStatus;
    private Long latencyMs;

    public static PingResult ok(long latencyMs) {
        return PingResult.builder().ok(true).message("ok").latencyMs(latencyMs).build();
    }
    public static PingResult fail(String message) {
        return PingResult.builder().ok(false).message(message).build();
    }
    public static PingResult notImplemented(String code) {
        return PingResult.builder().ok(false)
                .message("ping not implemented for " + code).build();
    }
}
