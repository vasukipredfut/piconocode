package com.aria.m0.page;

import com.aria.m0.page.dao.PageDefinitionRepository;
import com.aria.m0.page.dao.PageVersionRepository;
import com.aria.m0.page.dto.PageResponse;
import com.aria.m0.page.model.PageDefinition;
import com.aria.m0.page.model.PageVersion;
import com.aria.m0.page.service.PageRendererService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the Redis-cached read path. Verifies:
 * <ul>
 *   <li>Cache miss -> DB hit -> cache populate.</li>
 *   <li>Cache hit -> DB untouched.</li>
 *   <li>{@link PageRendererService#evict} deletes the right key.</li>
 *   <li>{@code RETIRED} / unpublished pages return empty.</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class PageRendererServiceTest {

    @Mock PageDefinitionRepository defRepo;
    @Mock PageVersionRepository versionRepo;
    @Mock StringRedisTemplate redis;
    @Mock ValueOperations<String, String> valueOps;

    @InjectMocks PageRendererService svc;

    @Test
    void cacheMiss_hitsDbAndPopulatesCache() {
        when(redis.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("m0:page:1:p")).thenReturn(null);
        PageDefinition d = new PageDefinition();
        d.setId(10L); d.setTenantId(1L); d.setCode("p"); d.setTitle("T");
        d.setStatus("PUBLISHED"); d.setCurrentVersionId(20L);
        when(defRepo.findByTenantIdAndCode(1L, "p")).thenReturn(Optional.of(d));
        PageVersion v = PageVersion.builder().id(20L).pageId(10L).version(3)
                .status("PUBLISHED").bodyJson("{\"layout\":{}}").build();
        when(versionRepo.findById(20L)).thenReturn(Optional.of(v));

        Optional<PageResponse> out = svc.loadPublishedFor(1L, "p");

        assertThat(out).isPresent();
        assertThat(out.get().getBodyJson()).isEqualTo("{\"layout\":{}}");
        assertThat(out.get().getVersion()).isEqualTo(3);
        verify(valueOps).set(eq("m0:page:1:p"), any(), eq(Duration.ofHours(1)));
    }

    @Test
    void cacheHit_skipsDb() {
        when(redis.opsForValue()).thenReturn(valueOps);
        // Build a known cache payload by going through serialise once (write-through round-trip).
        when(valueOps.get("m0:page:1:p")).thenReturn(null);
        // First call: populate the cache by hitting the DB.
        PageDefinition d = new PageDefinition();
        d.setId(10L); d.setTenantId(1L); d.setCode("p"); d.setTitle("T");
        d.setStatus("PUBLISHED"); d.setCurrentVersionId(20L);
        when(defRepo.findByTenantIdAndCode(1L, "p")).thenReturn(Optional.of(d));
        PageVersion v = PageVersion.builder().id(20L).pageId(10L).version(3)
                .status("PUBLISHED").bodyJson("{\"a\":1}").build();
        when(versionRepo.findById(20L)).thenReturn(Optional.of(v));
        svc.loadPublishedFor(1L, "p");

        // Capture what was written + replay it on the second call.
        org.mockito.ArgumentCaptor<String> cap = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(valueOps).set(eq("m0:page:1:p"), cap.capture(), any(Duration.class));
        when(valueOps.get("m0:page:1:p")).thenReturn(cap.getValue());

        reset(defRepo, versionRepo);
        Optional<PageResponse> hit = svc.loadPublishedFor(1L, "p");
        assertThat(hit).isPresent();
        assertThat(hit.get().getBodyJson()).isEqualTo("{\"a\":1}");
        verifyNoInteractions(defRepo);
        verifyNoInteractions(versionRepo);
    }

    @Test
    void retiredPageReturnsEmpty() {
        when(redis.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(any())).thenReturn(null);
        PageDefinition d = new PageDefinition();
        d.setId(10L); d.setCode("p"); d.setStatus("RETIRED"); d.setCurrentVersionId(20L);
        when(defRepo.findByTenantIdAndCode(1L, "p")).thenReturn(Optional.of(d));
        assertThat(svc.loadPublishedFor(1L, "p")).isEmpty();
    }

    @Test
    void unknownPageReturnsEmpty() {
        when(redis.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(any())).thenReturn(null);
        when(defRepo.findByTenantIdAndCode(1L, "p")).thenReturn(Optional.empty());
        assertThat(svc.loadPublishedFor(1L, "p")).isEmpty();
    }

    @Test
    void evict_deletesNamespacedKey() {
        svc.evict(1L, "p");
        verify(redis).delete("m0:page:1:p");
    }

    @Test
    void redisOutage_doesNotBreakReadPath() {
        when(redis.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(any())).thenThrow(new RuntimeException("redis down"));
        // The service catches in evict; but for read we deliberately let exceptions
        // surface so the controller can 503. Assert that.
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> svc.loadPublishedFor(1L, "p"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("redis down");
    }
}
