package com.aria.m0.savedview;

import com.aria.m0.savedview.dao.SavedViewRepository;
import com.aria.m0.savedview.model.SavedView;
import com.aria.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Unit test for {@link SavedViewService}: scope validation + visibility. */
class SavedViewServiceTest {

    private SavedViewRepository repo;
    private SavedViewService svc;

    @BeforeEach
    void setUp() {
        repo = mock(SavedViewRepository.class);
        svc = new SavedViewService();
        ReflectionTestUtils.setField(svc, "repo", repo);
        TenantContext.set(1L);
    }

    @AfterEach
    void tearDown() { TenantContext.clear(); }

    @Test
    void create_persistsRow_withTenantAndDefaults() {
        when(repo.save(any(SavedView.class))).thenAnswer(i -> i.getArgument(0));
        SavedView v = svc.create("pico:vendor-list", "All Active", "TENANT", null,
                "{\"k\":1}", 99L);
        assertThat(v.getTenantId()).isEqualTo(1L);
        assertThat(v.getPageSlug()).isEqualTo("pico:vendor-list");
        assertThat(v.getScope()).isEqualTo("TENANT");
        assertThat(v.isDefault()).isFalse();
        assertThat(v.getBodyJson()).isEqualTo("{\"k\":1}");
        assertThat(v.getCreatedBy()).isEqualTo(99L);
    }

    @Test
    void create_unknownScope_throws() {
        assertThatThrownBy(() -> svc.create("pico:x", "n", "GLOBAL", null, "{}", 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("scope");
    }

    @Test
    void create_missingPageSlug_throws() {
        assertThatThrownBy(() -> svc.create(null, "n", "USER", null, "{}", 1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void create_missingName_throws() {
        assertThatThrownBy(() -> svc.create("pico:x", "", "USER", null, "{}", 1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void listVisible_delegatesToRepo() {
        when(repo.findVisible(1L, "pico:vendor-list", 99L)).thenReturn(List.of(
                SavedView.builder().id(1L).build(), SavedView.builder().id(2L).build()));
        List<SavedView> rows = svc.listVisible("pico:vendor-list", 99L);
        assertThat(rows).hasSize(2);
    }

    @Test
    void update_userScopeByDifferentUser_throws() {
        SavedView v = SavedView.builder().id(1L).tenantId(1L).scope("USER")
                .createdBy(10L).name("orig").bodyJson("{}").build();
        when(repo.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(v));
        assertThatThrownBy(() -> svc.update(1L, "new name", null, 999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("owned by another user");
    }

    @Test
    void update_byCreator_persists() {
        SavedView v = SavedView.builder().id(1L).tenantId(1L).scope("USER")
                .createdBy(10L).name("orig").bodyJson("{}").build();
        when(repo.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(v));
        when(repo.save(any(SavedView.class))).thenAnswer(i -> i.getArgument(0));
        SavedView updated = svc.update(1L, "new", "{\"x\":1}", 10L);
        assertThat(updated.getName()).isEqualTo("new");
        assertThat(updated.getBodyJson()).isEqualTo("{\"x\":1}");
    }

    @Test
    void delete_notFound_throws() {
        when(repo.findByIdAndTenantId(99L, 1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> svc.delete(99L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void delete_tenantScope_succeeds() {
        SavedView v = SavedView.builder().id(1L).tenantId(1L).scope("TENANT").build();
        when(repo.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(v));
        svc.delete(1L, 50L);
        verify(repo).delete(v);
    }
}
