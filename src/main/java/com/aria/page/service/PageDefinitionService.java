package com.aria.page.service;

import com.aria.page.dao.PageDefinitionRepository;
import com.aria.page.dao.PageVersionRepository;
import com.aria.page.dao.PageWorkflowBindingRepository;
import com.aria.page.dto.PageBindingRequest;
import com.aria.page.dto.PageCreateRequest;
import com.aria.page.dto.PageUpdateRequest;
import com.aria.page.model.PageDefinition;
import com.aria.page.model.PageVersion;
import com.aria.page.model.PageWorkflowBinding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Lifecycle owner for page definitions.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Create a page (writes the definition row + a v1 DRAFT version).</li>
 *   <li>Update the active DRAFT version (mutates in-place, no new row).</li>
 *   <li>Publish: snapshots the DRAFT body to a new PUBLISHED row, demotes
 *       prior PUBLISHED versions to ARCHIVED, points
 *       {@link PageDefinition#currentVersionId} at the new row.</li>
 *   <li>Rollback: copies an older version forward as a new PUBLISHED row
 *       (audit-friendly — no time-travel ambiguity).</li>
 *   <li>Retire: sets the definition status to RETIRED so it stops appearing
 *       in active listings (versions remain queryable).</li>
 * </ul>
 *
 * <p>Cache eviction is delegated to {@link PageRendererService} so this
 * service stays Redis-free. Tenant id is supplied by the caller until the
 * M0-tenant-context task in Round 1H lifts it to the JWT subject.
 */
@Service
public class PageDefinitionService {

    @Autowired private PageDefinitionRepository defRepo;
    @Autowired private PageVersionRepository versionRepo;
    @Autowired private PageWorkflowBindingRepository bindingRepo;
    @Autowired private PageRendererService renderer;

    @Transactional("m0PageTransactionManager")
    public PageDefinition create(Long tenantId, PageCreateRequest req) {
        defRepo.findByTenantIdAndCode(tenantId, req.getCode()).ifPresent(p -> {
            throw new PageConflictException("Page code already exists: " + req.getCode());
        });
        PageDefinition def = PageDefinition.builder()
                .tenantId(tenantId)
                .code(req.getCode())
                .title(req.getTitle())
                .description(req.getDescription())
                .category(req.getCategory())
                .status("DRAFT")
                .build();
        def = defRepo.save(def);

        String body = req.getBodyJson() == null ? "{}" : req.getBodyJson();
        PageVersion v1 = PageVersion.builder()
                .pageId(def.getId())
                .version(1)
                .status("DRAFT")
                .bodyJson(body)
                .build();
        versionRepo.save(v1);
        return def;
    }

    @Transactional("m0PageTransactionManager")
    public PageVersion updateDraft(Long tenantId, String code, PageUpdateRequest req) {
        PageDefinition def = mustFind(tenantId, code);
        if (req.getTitle() != null) def.setTitle(req.getTitle());
        if (req.getDescription() != null) def.setDescription(req.getDescription());
        if (req.getCategory() != null) def.setCategory(req.getCategory());
        defRepo.save(def);

        PageVersion draft = versionRepo
                .findFirstByPageIdAndStatusOrderByVersionDesc(def.getId(), "DRAFT")
                .orElseGet(() -> {
                    Integer next = versionRepo.findFirstByPageIdOrderByVersionDesc(def.getId())
                            .map(v -> v.getVersion() + 1).orElse(1);
                    PageVersion fresh = PageVersion.builder()
                            .pageId(def.getId())
                            .version(next)
                            .status("DRAFT")
                            .bodyJson("{}")
                            .build();
                    return versionRepo.save(fresh);
                });
        if (req.getBodyJson() != null) draft.setBodyJson(req.getBodyJson());
        if (req.getRationale() != null) draft.setRationale(req.getRationale());
        return versionRepo.save(draft);
    }

    @Transactional("m0PageTransactionManager")
    public PageVersion publish(Long tenantId, String code, Long actorId) {
        PageDefinition def = mustFind(tenantId, code);
        PageVersion draft = versionRepo
                .findFirstByPageIdAndStatusOrderByVersionDesc(def.getId(), "DRAFT")
                .orElseThrow(() -> new PageConflictException("No DRAFT to publish for " + code));

        // Demote prior published versions to archived.
        versionRepo.findByPageIdOrderByVersionDesc(def.getId()).stream()
                .filter(v -> "PUBLISHED".equals(v.getStatus()))
                .forEach(v -> { v.setStatus("ARCHIVED"); versionRepo.save(v); });

        draft.setStatus("PUBLISHED");
        draft.setPublishedAt(Instant.now());
        draft.setPublishedBy(actorId);
        versionRepo.save(draft);

        def.setStatus("PUBLISHED");
        def.setCurrentVersionId(draft.getId());
        defRepo.save(def);

        renderer.evict(tenantId, code);
        return draft;
    }

    @Transactional("m0PageTransactionManager")
    public PageVersion rollback(Long tenantId, String code, Integer toVersion, Long actorId) {
        PageDefinition def = mustFind(tenantId, code);
        PageVersion source = versionRepo.findByPageIdAndVersion(def.getId(), toVersion)
                .orElseThrow(() -> new PageNotFoundException("Version " + toVersion + " not found"));

        // Demote current published rows.
        versionRepo.findByPageIdOrderByVersionDesc(def.getId()).stream()
                .filter(v -> "PUBLISHED".equals(v.getStatus()))
                .forEach(v -> { v.setStatus("ARCHIVED"); versionRepo.save(v); });

        Integer next = versionRepo.findFirstByPageIdOrderByVersionDesc(def.getId())
                .map(v -> v.getVersion() + 1).orElse(1);
        PageVersion forward = PageVersion.builder()
                .pageId(def.getId())
                .version(next)
                .status("PUBLISHED")
                .bodyJson(source.getBodyJson())
                .rationale("Rollback to v" + toVersion)
                .publishedAt(Instant.now())
                .publishedBy(actorId)
                .build();
        forward = versionRepo.save(forward);

        def.setStatus("PUBLISHED");
        def.setCurrentVersionId(forward.getId());
        defRepo.save(def);

        renderer.evict(tenantId, code);
        return forward;
    }

    @Transactional("m0PageTransactionManager")
    public void retire(Long tenantId, String code) {
        PageDefinition def = mustFind(tenantId, code);
        def.setStatus("RETIRED");
        defRepo.save(def);
        renderer.evict(tenantId, code);
    }

    /**
     * Listing always resolves tenant-override -> global (-1), so a tenant sees the
     * whole product catalogue plus its own pages, each exactly once.
     *
     * <p>Default sidebar listing hides ARCHIVED so deleted/superseded pages stop
     * appearing in the menu. Callers that want ARCHIVED back pass ?status=ARCHIVED,
     * in which case the explicit status wins and no exclusion is applied.
     */
    @Transactional(value = "m0PageTransactionManager", readOnly = true)
    public Page<PageDefinition> list(Long tenantId, String category, String status, Pageable pageable) {
        String excludeStatus = (status == null) ? "ARCHIVED" : null;
        return defRepo.findResolved(tenantId, category, status, excludeStatus, pageable);
    }

    @Transactional(value = "m0PageTransactionManager", readOnly = true)
    public Optional<PageVersion> loadDraft(Long tenantId, String code) {
        PageDefinition def = mustFind(tenantId, code);
        return versionRepo.findFirstByPageIdAndStatusOrderByVersionDesc(def.getId(), "DRAFT");
    }

    @Transactional(value = "m0PageTransactionManager", readOnly = true)
    public List<PageVersion> listVersions(Long tenantId, String code) {
        PageDefinition def = mustFind(tenantId, code);
        return versionRepo.findByPageIdOrderByVersionDesc(def.getId());
    }

    @Transactional(value = "m0PageTransactionManager", readOnly = true)
    public PageDefinition mustFind(Long tenantId, String code) {
        return defRepo.findByTenantIdAndCode(tenantId, code)
                .orElseThrow(() -> new PageNotFoundException("Page not found: " + code));
    }

    @Transactional(value = "m0PageTransactionManager", readOnly = true)
    public List<PageWorkflowBinding> listBindings(Long tenantId, String code) {
        return bindingRepo.findByTenantIdAndPageCode(tenantId, code);
    }

    @Transactional("m0PageTransactionManager")
    public PageWorkflowBinding createBinding(Long tenantId, String code, PageBindingRequest req) {
        mustFind(tenantId, code);
        PageWorkflowBinding b = PageWorkflowBinding.builder()
                .tenantId(tenantId)
                .pageCode(code)
                .workflowCode(req.getWorkflowCode())
                .stepCode(req.getStepCode())
                .role(req.getRole() == null ? PageWorkflowBinding.BindingRole.INPUT : req.getRole())
                .fieldMapJson(req.getFieldMapJson())
                .build();
        return bindingRepo.save(b);
    }
}
