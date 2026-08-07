package com.aria.action.handler;

import com.aria.action.ActionHandler;
import com.aria.action.ActionInvocation;
import com.aria.action.ActionResult;
import com.aria.security.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * {@code gatepass.create} — like {@code save} (delegates the actual
 * INSERT/UPDATE to the same {@link EntityWriter}), but on a fresh insert also
 * stamps {@code gp_number} and {@code current_step}/{@code status} on the
 * {@code gate_pass} row.
 *
 * <p>Exists because the plain Gate Pass save (V224/V225 — no Flow Variant
 * picked, no workflow started) has no workflow instance to derive
 * {@code gp_number} from the way {@code WorkflowProjectionListener}'s
 * {@code number_pattern} normally would ("GP-{tenant}-{instance}"). The
 * operator still needs a real, permanent number immediately to print the
 * gate pass slip, so this stamps one from {@code gate_pass_number_seq}
 * instead — a plain incrementing counter per tenant, atomic via the standard
 * MariaDB {@code INSERT ... ON DUPLICATE KEY UPDATE next_val =
 * LAST_INSERT_ID(next_val + 1)} pattern (a bare {@code MAX(...)+1} would race
 * under concurrent saves). {@code current_step}/{@code status} are set to
 * {@code GATE_PASS_CREATED} to match exactly what they would have shown under
 * the old GATE_PASS_CREATED workflow state, for the interim period before
 * {@link com.aria.workflow.projection.GatePassDispatchLinkListener} links the
 * row to a real instance once the workflow actually starts (which does not
 * touch gp_number — it stays the number stamped here, permanently).
 */
@Component
public class GatePassCreateActionHandler implements ActionHandler {

    @Autowired private EntityWriter writer;
    @Autowired private JdbcTemplate jdbc;

    @Override public String code() { return "gatepass.create"; }

    @Override
    public ActionResult handle(ActionInvocation inv) {
        if (inv.getEntity() == null) throw new IllegalArgumentException("entity required");
        boolean freshInsert = inv.getRecordId() == null;
        Long id = writer.upsert(inv.getEntity(), inv.getRecordId(), inv.getFormData());

        if (freshInsert) {
            Long tenantId = TenantContext.get();
            // Both statements MUST run on the same physical connection —
            // LAST_INSERT_ID() is connection-scoped, and separate
            // JdbcTemplate calls can each borrow a different connection from
            // the pool with no active transaction pinning one. A
            // ConnectionCallback guarantees that; two plain jdbc.update()/
            // queryForObject() calls do not.
            Long seq = jdbc.execute((ConnectionCallback<Long>) con -> {
                try (PreparedStatement ps = con.prepareStatement(
                        // LAST_INSERT_ID(1) on the initial INSERT branch too —
                        // without it, a brand-new tenant's first call never
                        // touches LAST_INSERT_ID at all (no real auto-increment
                        // column is involved), so SELECT LAST_INSERT_ID()
                        // below would return whatever it was before this
                        // statement (often 0) instead of 1.
                        "INSERT INTO gate_pass_number_seq (tenant_id, next_val) VALUES (?, LAST_INSERT_ID(1)) " +
                        "ON DUPLICATE KEY UPDATE next_val = LAST_INSERT_ID(next_val + 1)")) {
                    ps.setLong(1, tenantId);
                    ps.executeUpdate();
                }
                try (Statement st = con.createStatement();
                     ResultSet rs = st.executeQuery("SELECT LAST_INSERT_ID()")) {
                    rs.next();
                    return rs.getLong(1);
                }
            });
            String gpNumber = "GP-" + tenantId + "-" + seq;
            jdbc.update("UPDATE gate_pass SET gp_number=?, current_step='GATE_PASS_CREATED', status='GATE_PASS_CREATED' WHERE id=?",
                    gpNumber, id);
        }

        return ActionResult.builder().ok(true).recordId(id).message("saved").build();
    }
}
