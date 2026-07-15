package com.aria.m0.page;

import com.aria.m0.page.dao.PageDefinitionRepository;
import com.aria.m0.page.dao.PageVersionRepository;
import com.aria.m0.page.dao.PageWorkflowBindingRepository;
import com.aria.m0.page.dto.PageCreateRequest;
import com.aria.m0.page.dto.PageUpdateRequest;
import com.aria.m0.page.model.PageDefinition;
import com.aria.m0.page.model.PageVersion;
import com.aria.m0.page.service.PageConflictException;
import com.aria.m0.page.service.PageDefinitionService;
import com.aria.m0.page.service.PageNotFoundException;
import com.aria.m0.page.service.PageRendererService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Pure-Mockito unit tests for {@link PageDefinitionService}. Verifies the
 * create / updateDraft / publish / rollback business logic in isolation —
 * Redis eviction is asserted via verify() on the mock {@link PageRendererService}.
 *
 * <p>No Spring context, no DB.
 */
@ExtendWith(MockitoExtension.class)
class PageDefinitionServiceTest {

    @Mock PageDefinitionRepository defRepo;
    @Mock PageVersionRepository versionRepo;
    @Mock PageWorkflowBindingRepository bindingRepo;
    @Mock PageRendererService renderer;

    @InjectMocks PageDefinitionService svc;

    @BeforeEach void resetMocks() {
        // Mockito's reset is implicit per-method via @Mock; kept for clarity.
    }

    @Test
    void create_rejectsDuplicateCode() {
        when(defRepo.findByTenantIdAndCode(1L, "p")).thenReturn(Optional.of(new PageDefinition()));
        PageCreateRequest req = new PageCreateRequest();
        req.setCode("p"); req.setTitle("T");
        assertThatThrownBy(() -> svc.create(1L, req))
                .isInstanceOf(PageConflictException.class);
    }

    @Test
    void create_persistsDefinitionAndDraftV1() {
        when(defRepo.findByTenantIdAndCode(1L, "p")).thenReturn(Optional.empty());
        when(defRepo.save(any())).thenAnswer(inv -> {
            PageDefinition d = inv.getArgument(0); d.setId(42L); return d;
        });
        PageCreateRequest req = new PageCreateRequest();
        req.setCode("p"); req.setTitle("T"); req.setBodyJson("{\"v\":1}");

        PageDefinition out = svc.create(1L, req);

        assertThat(out.getId()).isEqualTo(42L);
        ArgumentCaptor<PageVersion> v = ArgumentCaptor.forClass(PageVersion.class);
        verify(versionRepo).save(v.capture());
        assertThat(v.getValue().getVersion()).isEqualTo(1);
        assertThat(v.getValue().getStatus()).isEqualTo("DRAFT");
        assertThat(v.getValue().getBodyJson()).isEqualTo("{\"v\":1}");
    }

    @Test
    void updateDraft_mutatesExistingDraftRow() {
        PageDefinition def = page(7L);
        when(defRepo.findByTenantIdAndCode(1L, "p")).thenReturn(Optional.of(def));
        PageVersion draft = PageVersion.builder().id(11L).pageId(7L).version(1).status("DRAFT").bodyJson("{}").build();
        when(versionRepo.findFirstByPageIdAndStatusOrderByVersionDesc(7L, "DRAFT")).thenReturn(Optional.of(draft));
        when(versionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PageUpdateRequest req = new PageUpdateRequest();
        req.setBodyJson("{\"updated\":true}");
        req.setTitle("New T");
        PageVersion out = svc.updateDraft(1L, "p", req);

        assertThat(out.getBodyJson()).isEqualTo("{\"updated\":true}");
        assertThat(def.getTitle()).isEqualTo("New T");
    }

    @Test
    void publish_flipsDraftToPublishedAndArchivesPrior() {
        PageDefinition def = page(7L);
        when(defRepo.findByTenantIdAndCode(1L, "p")).thenReturn(Optional.of(def));
        PageVersion draft = PageVersion.builder().id(2L).pageId(7L).version(2).status("DRAFT").bodyJson("{}").build();
        PageVersion prior = PageVersion.builder().id(1L).pageId(7L).version(1).status("PUBLISHED").bodyJson("{}").build();
        when(versionRepo.findFirstByPageIdAndStatusOrderByVersionDesc(7L, "DRAFT")).thenReturn(Optional.of(draft));
        when(versionRepo.findByPageIdOrderByVersionDesc(7L)).thenReturn(List.of(draft, prior));
        when(versionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(defRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PageVersion out = svc.publish(1L, "p", 9L);

        assertThat(out.getStatus()).isEqualTo("PUBLISHED");
        assertThat(out.getPublishedBy()).isEqualTo(9L);
        assertThat(prior.getStatus()).isEqualTo("ARCHIVED");
        assertThat(def.getCurrentVersionId()).isEqualTo(2L);
        verify(renderer).evict(1L, "p");
    }

    @Test
    void publish_failsIfNoDraft() {
        PageDefinition def = page(7L);
        when(defRepo.findByTenantIdAndCode(1L, "p")).thenReturn(Optional.of(def));
        when(versionRepo.findFirstByPageIdAndStatusOrderByVersionDesc(7L, "DRAFT")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> svc.publish(1L, "p", null))
                .isInstanceOf(PageConflictException.class);
    }

    @Test
    void rollback_copiesOlderVersionForwardAsNewPublished() {
        PageDefinition def = page(7L);
        when(defRepo.findByTenantIdAndCode(1L, "p")).thenReturn(Optional.of(def));
        PageVersion older = PageVersion.builder().id(10L).pageId(7L).version(1).status("ARCHIVED").bodyJson("{\"v\":1}").build();
        PageVersion current = PageVersion.builder().id(11L).pageId(7L).version(3).status("PUBLISHED").bodyJson("{}").build();
        when(versionRepo.findByPageIdAndVersion(7L, 1)).thenReturn(Optional.of(older));
        when(versionRepo.findByPageIdOrderByVersionDesc(7L)).thenReturn(List.of(current, older));
        when(versionRepo.findFirstByPageIdOrderByVersionDesc(7L)).thenReturn(Optional.of(current));
        when(versionRepo.save(any())).thenAnswer(inv -> {
            PageVersion v = inv.getArgument(0);
            if (v.getId() == null) v.setId(99L);
            return v;
        });
        when(defRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PageVersion forward = svc.rollback(1L, "p", 1, 5L);

        assertThat(forward.getStatus()).isEqualTo("PUBLISHED");
        assertThat(forward.getVersion()).isEqualTo(4);
        assertThat(forward.getBodyJson()).isEqualTo("{\"v\":1}");
        assertThat(current.getStatus()).isEqualTo("ARCHIVED");
        verify(renderer).evict(1L, "p");
    }

    @Test
    void rollback_unknownVersionThrows() {
        when(defRepo.findByTenantIdAndCode(1L, "p")).thenReturn(Optional.of(page(7L)));
        when(versionRepo.findByPageIdAndVersion(7L, 99)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> svc.rollback(1L, "p", 99, null))
                .isInstanceOf(PageNotFoundException.class);
    }

    @Test
    void mustFind_throwsOnMiss() {
        when(defRepo.findByTenantIdAndCode(eq(1L), eq("p"))).thenReturn(Optional.empty());
        assertThatThrownBy(() -> svc.mustFind(1L, "p"))
                .isInstanceOf(PageNotFoundException.class);
    }

    private PageDefinition page(long id) {
        PageDefinition d = new PageDefinition();
        d.setId(id); d.setTenantId(1L); d.setCode("p"); d.setTitle("T"); d.setStatus("DRAFT");
        return d;
    }
}
