package com.aria._base;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Loads the sample TENANT-CONFIG bundle used by the integration tests.
 *
 * <p>The product deliberately ships no customer configuration:
 * {@code TenantConfigBundleService.apply(tenantId, bundle)} takes the bundle
 * from its caller, so onboarding any customer is the same call with a different
 * payload. This fixture is simply the test-side caller — which means the tests
 * exercise the same generic path a real onboarding uses, rather than a
 * convenience method that only exists for one customer.
 */
public final class TenantConfigFixture {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final String PATH = "fixtures/sample-tenant-config.json";

    private TenantConfigFixture() {}

    /** The sample bundle, parsed. */
    public static JsonNode sampleBundle() {
        try (InputStream is = new ClassPathResource(PATH).getInputStream()) {
            return JSON.readTree(new String(is.readAllBytes(), StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("cannot load " + PATH + " from test classpath", e);
        }
    }
}
