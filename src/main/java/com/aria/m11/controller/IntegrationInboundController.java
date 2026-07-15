package com.aria.m11.controller;

import com.aria.m11.service.IntegrationInboundReceiver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Collections;
import java.util.Map;

/**
 * Inbound webhook endpoint. URL pattern:
 *   {@code POST /api/integrations/inbound/{instanceId}/{token}}
 *
 * <p>Permit-all in {@link com.aria.config.WebSecurityConfig} — the path
 * token itself provides authentication.
 */
@RestController
@RequestMapping("/api/integrations/inbound")
public class IntegrationInboundController {

    @Autowired private IntegrationInboundReceiver receiver;

    @PostMapping(path = "/{instanceId}/{token}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> receive(@PathVariable Long instanceId,
                                          @PathVariable String token,
                                          @RequestBody(required = false) byte[] body,
                                          HttpServletRequest req) {
        Map<String, String> headers = new HashMap<>();
        for (String name : Collections.list(req.getHeaderNames())) {
            headers.put(name, req.getHeader(name));
        }
        IntegrationInboundReceiver.Outcome out =
                receiver.receive(instanceId, token, headers, body);
        ResponseEntity.BodyBuilder rb = ResponseEntity.status(out.httpStatus());
        rb.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        return rb.body(out.body());
    }
}
