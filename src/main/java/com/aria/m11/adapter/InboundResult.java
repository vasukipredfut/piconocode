package com.aria.m11.adapter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

/** Outcome of an inbound webhook ingestion. */
@Data
@Builder
@AllArgsConstructor
public class InboundResult {

    public static final String STATUS_ACCEPTED = "ACCEPTED";
    public static final String STATUS_REJECTED = "REJECTED";

    private String status;
    private boolean signatureOk;
    private Map<String, Object> parsedBody;
    private String reason;

    public static InboundResult accepted(boolean signatureOk, Map<String, Object> body) {
        return InboundResult.builder().status(STATUS_ACCEPTED)
                .signatureOk(signatureOk).parsedBody(body).build();
    }
    public static InboundResult rejected(String reason) {
        return InboundResult.builder().status(STATUS_REJECTED)
                .signatureOk(false).reason(reason).build();
    }
}
