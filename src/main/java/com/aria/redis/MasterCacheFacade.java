package com.aria.redis;

import com.aria.service.HsnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Cache wrapper around master-data read methods (REARCH ADR-007).
 *
 * Wrapping is necessary because the controller methods return
 * {@code ResponseEntity}, which Jackson cannot deserialize round-trip
 * (no default constructor). This facade exposes the same data via a
 * Serializable DTO that the {@link RedisConfig} JSON serializer handles
 * cleanly, then re-wraps in {@code ResponseEntity} at the call site.
 *
 * Methods here are intentionally thin — they just call the existing
 * service and unwrap the body. We keep the cache annotations off the
 * controllers so the call-site contract is unchanged.
 */
@Service
public class MasterCacheFacade {

    @Autowired
    private HsnService hsnService;

    /**
     * Cache the page body for the HSN list. Key includes filters + paging so
     * paginated callers don't trample each other.
     */
    @Cacheable(value = "master:hsn",
            key = "T(java.util.Objects).hash(#hsn,#category,#itemCategory," +
                  "#pageable?.pageNumber,#pageable?.pageSize," +
                  "#pageable?.sort?.toString())")
    public Object hsnList(String hsn, String category, String itemCategory, Pageable pageable) {
        ResponseEntity<?> r = hsnService.getHsnDetailAll(hsn, category, itemCategory, pageable);
        // Bodies here are {@code PageDataDto<HsnMasterDto>} which the
        // GenericJackson2JsonRedisSerializer can round-trip.
        return r.getBody();
    }

    @CacheEvict(value = "master:hsn", allEntries = true)
    public void evictHsn() {
        // hook for service-layer evictions (e.g. cron rebuild)
    }
}
