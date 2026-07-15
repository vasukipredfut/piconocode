package com.aria.controller;

import com.aria.dao.UserRepository;
import com.aria.dto.HsnMasterDto;
import com.aria.dto.SpecificationDetailsDto;
import com.aria.model.User;
import com.aria.redis.DistributedLock;
import com.aria.redis.MasterCacheFacade;
import com.aria.redis.RateLimit;
import com.aria.service.HsnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class HsnController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    HsnService hsnService;

    @Autowired
    DistributedLock distributedLock;

    @Autowired
    MasterCacheFacade masterCache;


    // Redis: distributed lock prevents concurrent CRUD against the HSN master from racing
    // (e.g. two operators creating the same HSN code at the same time). Cache-evict
    // forces a fresh DB read on next list. (REARCH ADR-007)
    @PostMapping("/saveHsn")
    @CacheEvict(value = "master:hsn", allEntries = true)
    public ResponseEntity<?> saveHsnMaster(@RequestBody HsnMasterDto hsnMasterDto, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        String lockKey = "lock:master:hsn:save:" + (hsnMasterDto.getHsnCode() == null ? "_" : hsnMasterDto.getHsnCode());
        Optional<String> token = distributedLock.tryLock(lockKey, Duration.ofSeconds(15));
        if (!token.isPresent()) {
            Map<String, Object> body = new HashMap<>();
            body.put("status", "409");
            body.put("message", "Another save for the same HSN is in progress, please retry");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
        }
        try {
            return hsnService.saveHsnDetail(hsnMasterDto, user.get());
        } finally {
            distributedLock.unlock(lockKey, token.get());
        }
    }

    @PutMapping("/updateHsn")
    @CacheEvict(value = "master:hsn", allEntries = true)
    public ResponseEntity<?> UpdateHsnMaster(@RequestBody HsnMasterDto hsnMasterDto, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        return hsnService.updateHsn(hsnMasterDto, user.get());
    }


    @GetMapping("/hsnItemList")
    public ResponseEntity<?> hsnItemList(Authentication authentication,
                                                       @RequestParam(value = "search", required = false) String search,
                                                       @RequestParam(value = "specType") Long specType) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return hsnService.hsnItemList(user.get(), search, specType);
        } else {
            return new ResponseEntity<List<SpecificationDetailsDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    // Redis: cache HSN list reads for 1h (master TTL) via MasterCacheFacade — the
    // facade returns a Serializable body that the JSON serializer can round-trip,
    // unlike ResponseEntity. Rate-limited at 5 r/s for demo. (REARCH ADR-007)
    @GetMapping("/hsnList")
    @RateLimit(perSecond = 5, burst = 5)
    public ResponseEntity<?> getAllHsn( @RequestParam(value = "hsn", required = false) String hsn,
                                        @RequestParam(value = "category", required = false) String category,
                                        @RequestParam(value = "itemCategory", required = false) String itemCategory,
                                        Pageable pageable,
                                        Authentication authentication) {
        Object body = masterCache.hsnList(hsn, category, itemCategory, pageable);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/hsn/{id}")
    public ResponseEntity<?> getHsnDetail( @PathVariable Long id, Authentication authentication) {
        return hsnService.getHsnDetail(id);
    }

    @GetMapping("/hsn/item")
    public ResponseEntity<?> getHsnItemId( @RequestParam(value = "itemId", required = false) Long itemId,
                                           @RequestParam(value = "category", required = false) String category,
                                           Authentication authentication) {
        return hsnService.getHsnItemId(itemId, category);
    }


    @DeleteMapping("/hsn/{id}")
    @CacheEvict(value = "master:hsn", allEntries = true)
    public ResponseEntity<?> deleteHsn( @PathVariable Long id, Authentication authentication) {
        return hsnService.deleteHsnDetail(id);
    }


    @GetMapping("/validateHsn")
    public ResponseEntity<?> validateHsn( @RequestParam(value = "hsn", required = false) String hsn,
                                        @RequestParam(value = "productId", required = false) Long productId,
                                          @RequestParam(value = "category") String category,
                                        Authentication authentication) {
        return hsnService.validateHsn(hsn, productId, category);
    }


}
