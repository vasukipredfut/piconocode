package com.aria.m0.savedview;

import com.aria.m0.savedview.dao.SavedViewRepository;
import com.aria.m0.savedview.model.SavedView;
import com.aria.security.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * CRUD over {@code tenant_list_view} with scope-aware visibility (Schema
 * V2 §18 #2).
 *
 * <p>Scope semantics (Round 1G):
 * <ul>
 *   <li>{@code TENANT} — visible to everyone in the tenant.</li>
 *   <li>{@code USER} — visible only to the creator.</li>
 *   <li>{@code ROLE} — accepted at create-time but treated as TENANT for
 *       visibility in Round 1G; Round 1G.2 introduces role-aware filtering.</li>
 * </ul>
 */
@Service
public class SavedViewService {

    private static final Set<String> SCOPES = Set.of("TENANT", "ROLE", "USER");

    @Autowired private SavedViewRepository repo;

    @Transactional("m0PageTransactionManager")
    public SavedView create(String pageSlug, String name, String scope, String scopeValue,
                            String bodyJson, Long userId) {
        if (pageSlug == null || pageSlug.isBlank())
            throw new IllegalArgumentException("pageSlug required");
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("name required");
        if (!SCOPES.contains(scope))
            throw new IllegalArgumentException("scope must be TENANT|ROLE|USER, got: " + scope);
        SavedView v = SavedView.builder()
                .tenantId(TenantContext.get())
                .pageSlug(pageSlug)
                .name(name)
                .scope(scope)
                .scopeValue(scopeValue)
                .isDefault(false)
                .bodyJson(bodyJson == null ? "{}" : bodyJson)
                .createdBy(userId)
                .build();
        return repo.save(v);
    }

    public List<SavedView> listVisible(String pageSlug, Long userId) {
        return repo.findVisible(TenantContext.get(), pageSlug, userId);
    }

    public Optional<SavedView> findById(Long id) {
        return repo.findByIdAndTenantId(id, TenantContext.get());
    }

    @Transactional("m0PageTransactionManager")
    public SavedView update(Long id, String name, String bodyJson, Long userId) {
        SavedView v = repo.findByIdAndTenantId(id, TenantContext.get())
                .orElseThrow(() -> new IllegalArgumentException("SavedView not found: " + id));
        // USER-scope can only be edited by creator.
        if ("USER".equals(v.getScope()) && userId != null && !userId.equals(v.getCreatedBy())) {
            throw new IllegalArgumentException("USER-scoped view is owned by another user");
        }
        if (name != null && !name.isBlank()) v.setName(name);
        if (bodyJson != null) v.setBodyJson(bodyJson);
        return repo.save(v);
    }

    @Transactional("m0PageTransactionManager")
    public void delete(Long id, Long userId) {
        SavedView v = repo.findByIdAndTenantId(id, TenantContext.get())
                .orElseThrow(() -> new IllegalArgumentException("SavedView not found: " + id));
        if ("USER".equals(v.getScope()) && userId != null && !userId.equals(v.getCreatedBy())) {
            throw new IllegalArgumentException("USER-scoped view is owned by another user");
        }
        repo.delete(v);
    }
}
