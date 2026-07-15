package com.aria.m11.adapter;

import com.aria.m11.model.IntegrationInstance;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the generic OUTBOUND_REST adapter. Uses the JDK's
 * built-in {@link com.sun.net.httpserver.HttpServer} so we don't take a new
 * test-time dependency.
 */
class OutboundRestAdapterTest {

    private HttpServer server;
    private int port;

    @BeforeEach
    void up() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        port = server.getAddress().getPort();
        server.start();
    }

    @AfterEach
    void down() { if (server != null) server.stop(0); }

    private IntegrationInstance instWith(String configJson) {
        return IntegrationInstance.builder()
                .id(1L).tenantId(1L).code("o").connectorCode(OutboundRestAdapter.CODE)
                .connectorVersion(1).name("n").status("ACTIVE").currentVersion(1)
                .configJson(configJson).dataResidencyFlag("ANY").errorCount24h(0).build();
    }

    @Test
    void invoke_post_happy() {
        AtomicReference<String> seenBody = new AtomicReference<>();
        AtomicInteger calls = new AtomicInteger();
        server.createContext("/payments", ex -> {
            calls.incrementAndGet();
            byte[] body = ex.getRequestBody().readAllBytes();
            seenBody.set(new String(body));
            byte[] resp = "{\"ok\":true,\"echo\":\"saw\"}".getBytes();
            ex.sendResponseHeaders(201, resp.length);
            try (OutputStream os = ex.getResponseBody()) { os.write(resp); }
        });
        OutboundRestAdapter adapter = new OutboundRestAdapter();
        var inst = instWith("{\"baseUrl\":\"http://localhost:" + port + "\"}");
        var res = adapter.invoke(inst, "POST /payments",
                Map.of("body", Map.of("amount", 100), "headers", Map.of("X-Custom", "x")),
                Map.of());
        assertThat(res.isOk()).isTrue();
        assertThat(res.getStatus()).isEqualTo(201);
        assertThat(res.getBody()).contains("\"ok\":true");
        assertThat(calls.get()).isEqualTo(1);
        assertThat(seenBody.get()).contains("100");
    }

    @Test
    void invoke_pathTemplate_substituted() {
        AtomicReference<String> seenPath = new AtomicReference<>();
        server.createContext("/items/42", ex -> {
            seenPath.set(ex.getRequestURI().getPath());
            ex.sendResponseHeaders(200, 0);
            ex.getResponseBody().close();
        });
        OutboundRestAdapter adapter = new OutboundRestAdapter();
        var inst = instWith("{\"baseUrl\":\"http://localhost:" + port + "\"}");
        var res = adapter.invoke(inst, "GET /items/{id}",
                Map.of("pathParams", Map.of("id", 42)), Map.of());
        assertThat(res.isOk()).isTrue();
        assertThat(seenPath.get()).isEqualTo("/items/42");
    }

    @Test
    void serverError_marksRetryable() {
        server.createContext("/x", ex -> {
            byte[] b = "boom".getBytes(StandardCharsets.UTF_8);
            ex.sendResponseHeaders(503, b.length);
            try (var os = ex.getResponseBody()) { os.write(b); }
        });
        OutboundRestAdapter adapter = new OutboundRestAdapter();
        var inst = instWith("{\"baseUrl\":\"http://localhost:" + port + "\"}");
        var res = adapter.invoke(inst, "GET /x", Map.of(), Map.of());
        assertThat(res.isOk()).isFalse();
        assertThat(res.isRetryable()).isTrue();
        assertThat(res.getErrorCode()).isEqualTo("http_503");
    }

    @Test
    void clientError_notRetryable() {
        server.createContext("/y", ex -> {
            byte[] b = "no".getBytes();
            ex.sendResponseHeaders(404, b.length);
            try (var os = ex.getResponseBody()) { os.write(b); }
        });
        OutboundRestAdapter adapter = new OutboundRestAdapter();
        var inst = instWith("{\"baseUrl\":\"http://localhost:" + port + "\"}");
        var res = adapter.invoke(inst, "GET /y", Map.of(), Map.of());
        assertThat(res.isOk()).isFalse();
        assertThat(res.isRetryable()).isFalse();
        assertThat(res.getErrorCode()).isEqualTo("http_404");
    }

    @Test
    void badOperation_failsImmediately() {
        OutboundRestAdapter adapter = new OutboundRestAdapter();
        var inst = instWith("{\"baseUrl\":\"http://localhost:" + port + "\"}");
        var res = adapter.invoke(inst, "weird-op", Map.of(), Map.of());
        assertThat(res.isOk()).isFalse();
        assertThat(res.getErrorCode()).isEqualTo("bad_request");
    }

    @Test
    void missingBaseUrl_failsImmediately() {
        OutboundRestAdapter adapter = new OutboundRestAdapter();
        var inst = instWith("{}");
        var res = adapter.invoke(inst, "GET /x", Map.of(), Map.of());
        assertThat(res.isOk()).isFalse();
        assertThat(res.getErrorCode()).isEqualTo("config_invalid");
    }

    @Test
    void apiKeyHeaderInjected_whenSecretsPresent() {
        AtomicReference<String> auth = new AtomicReference<>();
        server.createContext("/k", ex -> {
            auth.set(ex.getRequestHeaders().getFirst("Authorization"));
            ex.sendResponseHeaders(200, 0); ex.getResponseBody().close();
        });
        OutboundRestAdapter adapter = new OutboundRestAdapter();
        var inst = instWith("{\"baseUrl\":\"http://localhost:" + port + "\"}");
        var res = adapter.invoke(inst, "GET /k", Map.of(), Map.of("apiKey", "TOK-123"));
        assertThat(res.isOk()).isTrue();
        assertThat(auth.get()).isEqualTo("Bearer TOK-123");
    }
}
