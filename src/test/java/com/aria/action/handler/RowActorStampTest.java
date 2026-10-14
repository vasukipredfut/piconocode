package com.aria.action.handler;

import com.aria.auth.AuthUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RowActorStampTest {

    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);

    @AfterEach
    void tearDown() { SecurityContextHolder.clearContext(); }

    private void loginAs(long id) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new AuthUserDetails(id, "officer", "x", 1L), "n/a", List.of()));
    }

    @Test
    void noLoggedInUser_nothingIsWritten() {
        RowActorStamp.startedLoading(jdbc, 1L, 5L, "R1", "O1", "SKU1", "Bay1", null);
        RowActorStamp.completedLoading(jdbc, 1L, 5L, "R1", "O1", "SKU1", null);
        RowActorStamp.startedUnloading(jdbc, 1L, 5L, "R1", "O1", "SKU1", "Bay1", null);
        RowActorStamp.completedUnloading(jdbc, 1L, 5L, "R1", "O1", "SKU1", null);
        verifyNoInteractions(jdbc);
    }

    @Test
    void performedByName_isSavedBesideTheLoginId_onStartAndComplete() {
        loginAs(42L);
        RowActorStamp.startedLoading(jdbc, 1L, 5L, "R1", "O1", "SKU1", "Bay1", "Ravi");
        RowActorStamp.completedLoading(jdbc, 1L, 5L, "R1", "O1", "SKU1", "Bala");
        RowActorStamp.weighedLoadingRow(jdbc, 1L, 11L, "Chitra");
        RowActorStamp.unloadedRow(jdbc, 1L, 12L, "Dev");

        verify(jdbc).update(startsWith("UPDATE loading_progress_detail_bay SET started_by_id = ?, started_by_name = ? WHERE"),
                eq(42L), eq("Ravi"), eq(1L), eq(5L), eq("R1"), eq("O1"), eq("SKU1"), eq("Bay1"));
        verify(jdbc).update(startsWith("UPDATE loading_progress_detail_bay SET weighed_by_id = ?, weighed_by_name = ? WHERE tenant_id = ? AND workflow_instance_id"),
                eq(42L), eq("Bala"), eq(1L), eq(5L), eq("R1"), eq("O1"), eq("SKU1"));
        verify(jdbc).update("UPDATE loading_progress_detail_bay SET weighed_by_id = ?, weighed_by_name = ? WHERE tenant_id = ? AND id = ?", 42L, "Chitra", 1L, 11L);
        verify(jdbc).update("UPDATE unloading_progress_detail_bay SET unloaded_by_id = ?, unloaded_by_name = ? WHERE tenant_id = ? AND id = ?", 42L, "Dev", 1L, 12L);
    }

    @Test
    void performedBy_prefersTheLineThenTheForm_trimmedAndOptional() {
        org.assertj.core.api.Assertions.assertThat(RowActorStamp.performedBy(java.util.Map.of("performed_by_name", " Line "), java.util.Map.of("performed_by_name", "Form"))).isEqualTo("Line");
        org.assertj.core.api.Assertions.assertThat(RowActorStamp.performedBy(java.util.Map.of(), java.util.Map.of("performed_by_name", "Form"))).isEqualTo("Form");
        org.assertj.core.api.Assertions.assertThat(RowActorStamp.performedBy(null, java.util.Map.of())).isNull();
    }

    @Test
    void startBay_stampsOnlyNewlyOpenUnstampedRowsOfThatBay() {
        loginAs(42L);
        RowActorStamp.startedLoading(jdbc, 1L, 5L, "R1", "O1", "SKU1", "Bay1", null);

        verify(jdbc).update(eq("UPDATE loading_progress_detail_bay SET started_by_id = ? WHERE tenant_id = ? AND workflow_instance_id = ? "
                        + "AND route_id <=> ? AND order_id <=> ? AND sku_code = ? AND bay_name <=> ? "
                        + "AND status = 'IN_PROGRESS' AND started_by_id IS NULL"),
                eq(42L), eq(1L), eq(5L), eq("R1"), eq("O1"), eq("SKU1"), eq("Bay1"));
    }

    @Test
    void bayComplete_stampsOnlyJustDoneUnstampedRows_loadingAndUnloadingColumns() {
        loginAs(42L);
        RowActorStamp.completedLoading(jdbc, 1L, 5L, "R1", "O1", "SKU1", null);
        RowActorStamp.completedUnloading(jdbc, 1L, 5L, "R1", "O1", "SKU1", null);

        verify(jdbc).update(eq("UPDATE loading_progress_detail_bay SET weighed_by_id = ? WHERE tenant_id = ? AND workflow_instance_id = ? "
                        + "AND route_id <=> ? AND order_id <=> ? AND sku_code = ? AND status = 'DONE' AND weighed_by_id IS NULL"),
                eq(42L), eq(1L), eq(5L), eq("R1"), eq("O1"), eq("SKU1"));
        verify(jdbc).update(eq("UPDATE unloading_progress_detail_bay SET unloaded_by_id = ? WHERE tenant_id = ? AND workflow_instance_id = ? "
                        + "AND route_id <=> ? AND order_id <=> ? AND sku_code = ? AND status = 'DONE' AND unloaded_by_id IS NULL"),
                eq(42L), eq(1L), eq(5L), eq("R1"), eq("O1"), eq("SKU1"));
    }
}
