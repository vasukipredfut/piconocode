package com.aria.redis;

import com.aria.service.HsnService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link MasterCacheFacade}.
 *
 * The {@code @Cacheable} annotation is not in effect here (no Spring context) —
 * we are just verifying the facade's wiring: it must unwrap the
 * {@link ResponseEntity} body so the Jackson-typed serializer can round-trip
 * the value. Real cache hit/miss behaviour is covered by {@code CacheBehaviorIT}.
 */
@ExtendWith(MockitoExtension.class)
class MasterCacheFacadeTest {

    @Mock
    private HsnService hsnService;

    @InjectMocks
    private MasterCacheFacade facade;

    @Test
    @DisplayName("hsnList(): unwraps ResponseEntity body and returns the payload")
    void unwrapsResponseEntity() {
        Object body = new java.util.HashMap<>();
        // doReturn avoids the generic wildcard inference issue on ResponseEntity<?>.
        doReturn(ResponseEntity.ok(body))
                .when(hsnService).getHsnDetailAll("H1", "C1", "I1", PageRequest.of(0, 5));

        Object result = facade.hsnList("H1", "C1", "I1", PageRequest.of(0, 5));

        assertThat(result).isSameAs(body);
    }

    @Test
    @DisplayName("hsnList(): tolerates a null body (empty result)")
    void nullBodyTolerated() {
        doReturn(ResponseEntity.ok().build())
                .when(hsnService).getHsnDetailAll("X", null, null, PageRequest.of(0, 1));

        assertThat(facade.hsnList("X", null, null, PageRequest.of(0, 1))).isNull();
    }

    @Test
    @DisplayName("evictHsn(): is a hookable no-op (annotated for @CacheEvict)")
    void evictNoOp() {
        // The @CacheEvict annotation is processed by Spring's proxy, not here;
        // we just confirm direct invocation is harmless.
        facade.evictHsn();
        verify(hsnService, times(0)).getHsnDetailAll(null, null, null, null);
    }
}
