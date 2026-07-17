package com.aria.page.service;

import com.aria.page.dao.PageDefinitionRepository;
import com.aria.page.dao.PageVersionRepository;
import com.aria.page.dto.PageResponse;
import com.aria.page.model.PageDefinition;
import com.aria.page.model.PageVersion;
import com.aria.security.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

/**
 * Read-path for the runtime Renderer.
 *
 * <p>Published page bodies are cached in Redis with the key shape
 * {@code m0:page:{tenant}:{code}}. The TTL is intentionally long
 * (1 hour) — invalidation happens via {@link #evict(Long, String)}
 * called from {@link PageDefinitionService#publish}, {@code rollback},
 * and {@code retire}.
 *
 * <p>The cached payload is the *resolved* {@link PageResponse} (id, code,
 * version, body) — not just the body — so the renderer sees a consistent
 * snapshot of metadata + body even if the underlying rows change.
 *
 * <p>We reuse the existing {@link StringRedisTemplate} configured by
 * {@code com.aria.redis.RedisConfig}. We deliberately do NOT use the
 * {@code @Cacheable} machinery in {@code MasterCacheFacade} — that pattern
 * is hard-coded to {@code master:*} cache names with a single TTL; we want
 * a per-tenant key shape so we manage it manually.
 */
@Service
public class PageRendererService {

    private static final Duration TTL = Duration.ofHours(1);

    @Autowired private PageDefinitionRepository defRepo;
    @Autowired private PageVersionRepository versionRepo;
    @Autowired private StringRedisTemplate redis;

    /** Cache key for a (tenant, page-code) pair. Shared with the IT for verification. */
    public static String cacheKey(Long tenantId, String code) {
        return "m0:page:" + tenantId + ":" + code;
    }

    /**
     * Returns the currently-published body for the page, or {@link Optional#empty()}
     * if no published version exists (or the page has been retired).
     */
    @Transactional(value = "m0PageTransactionManager", readOnly = true)
    public Optional<PageResponse> loadPublishedFor(Long tenantId, String code) {
        String key = cacheKey(tenantId, code);

        // Cache hit? Inflate.
        String cached = redis.opsForValue().get(key);
        if (cached != null) {
            return Optional.of(deserialise(cached));
        }

        // Miss: hit the DB. Resolve tenant-override -> global product page, so a
        // tenant inherits the whole catalogue and only owns what they changed.
        Optional<PageDefinition> defOpt = defRepo.resolveByCode(tenantId, code);
        if (defOpt.isEmpty()) return Optional.empty();
        PageDefinition def = defOpt.get();
        if ("RETIRED".equals(def.getStatus()) || def.getCurrentVersionId() == null) {
            return Optional.empty();
        }
        PageVersion v = versionRepo.findById(def.getCurrentVersionId()).orElse(null);
        if (v == null || !"PUBLISHED".equals(v.getStatus())) return Optional.empty();

        PageResponse resp = PageResponse.builder()
                .id(def.getId())
                .code(def.getCode())
                .title(def.getTitle())
                .description(def.getDescription())
                .category(def.getCategory())
                .status(def.getStatus())
                .version(v.getVersion())
                .bodyJson(v.getBodyJson())
                .updatedAt(def.getUpdatedAt())
                .build();

        // Populate cache best-effort. Redis outage must not break the API.
        try {
            redis.opsForValue().set(key, serialise(resp), TTL);
        } catch (RuntimeException ignored) {
            // Logged at the StringRedisTemplate layer; we serve stale-cache or DB anyway.
        }
        return Optional.of(resp);
    }

    /**
     * Drop the cached entry. Called on publish, rollback, retire.
     *
     * <p>Editing a GLOBAL ({@link TenantContext#GLOBAL_TENANT_ID}) page must
     * invalidate that code for <em>every</em> tenant, not just one: each tenant
     * caches its own <em>resolved</em> view under {@code m0:page:{tenant}:{code}},
     * and a tenant with no override is caching the global body. Evicting only
     * key -1 would leave every tenant serving the pre-edit page for up to the
     * 1-hour TTL — the same staleness class that made direct-SQL edits look like
     * they had silently failed.
     */
    public void evict(Long tenantId, String code) {
        try {
            if (TenantContext.GLOBAL_TENANT_ID.equals(tenantId)) {
                evictAllTenants(code);
            } else {
                redis.delete(cacheKey(tenantId, code));
            }
        } catch (RuntimeException ignored) {
            // tolerate Redis outage; next read will repopulate.
        }
    }

    /** Evict one page code across every tenant that has it cached. */
    public void evictAllTenants(String code) {
        try {
            Set<String> keys = redis.keys("m0:page:*:" + code);
            if (keys != null && !keys.isEmpty()) redis.delete(keys);
        } catch (RuntimeException ignored) {
            // tolerate Redis outage; next read will repopulate.
        }
    }

    // ----- private serialisation helpers ---------------------------------
    // We hand-roll a tiny pipe-delimited envelope to dodge Jackson type-info
    // headaches in the GenericJackson2JsonRedisSerializer path. The cache
    // payload is read-only and lives only for an hour, so a custom format
    // is fine — keeps this service free of new Jackson modules.
    private String serialise(PageResponse r) {
        // SAFE: code/title/etc come from VARCHAR columns; pipe is not allowed
        // in code (regex-checked at the controller) — but we still base64 the
        // body to keep multiline JSON whole.
        StringBuilder sb = new StringBuilder();
        sb.append(r.getId()).append('|').append(r.getVersion()).append('|')
          .append(nz(r.getCode())).append('|').append(nz(r.getTitle())).append('|')
          .append(nz(r.getStatus())).append('|').append(nz(r.getCategory())).append('|')
          .append(nz(r.getDescription())).append('|')
          .append(java.util.Base64.getEncoder().encodeToString(
                  nz(r.getBodyJson()).getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        return sb.toString();
    }

    private PageResponse deserialise(String s) {
        String[] parts = s.split("\\|", -1);
        return PageResponse.builder()
                .id(Long.parseLong(parts[0]))
                .version(Integer.parseInt(parts[1]))
                .code(parts[2])
                .title(parts[3])
                .status(parts[4])
                .category(orNull(parts[5]))
                .description(orNull(parts[6]))
                .bodyJson(new String(
                        java.util.Base64.getDecoder().decode(parts[7]),
                        java.nio.charset.StandardCharsets.UTF_8))
                .build();
    }

    private static String nz(String s) { return s == null ? "" : s; }
    private static String orNull(String s) { return s == null || s.isEmpty() ? null : s; }
}
