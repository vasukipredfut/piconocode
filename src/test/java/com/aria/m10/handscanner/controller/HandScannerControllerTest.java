package com.aria.m10.handscanner.controller;

import com.aria.m10.handscanner.service.HandScannerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link HandScannerController}: it's a thin HTTP shell over
 * {@link HandScannerService}, so these only cover the controller's own
 * gpNumber-required validation and that a valid request delegates verbatim.
 */
class HandScannerControllerTest {

    private HandScannerService service;
    private HandScannerController controller;

    @BeforeEach
    void setUp() {
        service = mock(HandScannerService.class);
        controller = new HandScannerController(service);
    }

    @Test
    void scanGatePass_nullBody_returnsErrorWithoutCallingService() {
        Map<String, Object> result = controller.scanGatePass(null);

        assertThat(result).containsExactly(Map.entry("error", "gpNumber is required"));
        verifyNoInteractions(service);
    }

    @Test
    void scanGatePass_missingGpNumberKey_returnsErrorWithoutCallingService() {
        Map<String, Object> result = controller.scanGatePass(new HashMap<>());

        assertThat(result).containsExactly(Map.entry("error", "gpNumber is required"));
        verifyNoInteractions(service);
    }

    @Test
    void scanGatePass_blankGpNumber_returnsErrorWithoutCallingService() {
        Map<String, String> body = new HashMap<>();
        body.put("gpNumber", "   ");

        Map<String, Object> result = controller.scanGatePass(body);

        assertThat(result).containsExactly(Map.entry("error", "gpNumber is required"));
        verifyNoInteractions(service);
    }

    @Test
    void scanGatePass_validGpNumber_delegatesToServiceAndReturnsItsResult() {
        Map<String, String> body = new HashMap<>();
        body.put("gpNumber", "GP-1-1493");
        Map<String, Object> serviceResult = Map.of("gatePass", Map.of("gp_number", "GP-1-1493"));
        when(service.scanGatePass("GP-1-1493")).thenReturn(serviceResult);

        Map<String, Object> result = controller.scanGatePass(body);

        assertThat(result).isEqualTo(serviceResult);
        verify(service).scanGatePass(eq("GP-1-1493"));
    }
}
