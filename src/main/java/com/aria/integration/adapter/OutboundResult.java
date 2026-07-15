package com.aria.integration.adapter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

/** Outcome of an outbound dispatch. */
@Data
@Builder
@AllArgsConstructor
public class OutboundResult {
    private boolean ok;
    private Integer status;
    private Map<String, String> headers;
    private String body;
    private String errorCode;
    private String errorMessage;
    /** Mark retryable transport-level errors so the supervisor knows to retry. */
    private boolean retryable;

    public static OutboundResult success(Integer status, Map<String, String> headers, String body) {
        return OutboundResult.builder().ok(true).status(status).headers(headers).body(body).build();
    }
    public static OutboundResult failed(String errorCode, String message) {
        return OutboundResult.builder().ok(false).errorCode(errorCode).errorMessage(message).build();
    }
    public static OutboundResult retryable(String errorCode, String message) {
        return OutboundResult.builder().ok(false).errorCode(errorCode).errorMessage(message)
                .retryable(true).build();
    }
}
