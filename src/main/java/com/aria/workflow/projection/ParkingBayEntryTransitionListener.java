package com.aria.workflow.projection;

import com.aria.security.TenantContext;
import com.aria.workflow.engine.WorkflowTransitionListener;
import com.aria.workflow.model.WorkflowInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Parking bay in/out log (dispatch_parking_bay_entry) driven by
 * aria:parking-scan. Kept as its own listener, separate from
 * WorkflowProjectionListener, because the write here is genuinely
 * conditional (open row exists -> close it with out_time; no open row ->
 * insert a new one with in_time) — the generic config-driven projection
 * can only map submitted fields onto columns unconditionally, it can't
 * express "only touch this column sometimes."
 *
 * <p>V143 removed the page's manual Direction field: aria:parking-scan is
 * bound (V7_14) to four M10.TankerInbound transitions
 * (GATE_PASS_CREATED-&gt;PARKING_IN, QC_APPROVED-&gt;PARKING_OUT_AFTER_QC,
 * WEIGHMENT_EMPTY-&gt;PARKING_IN_AFTER_UNLOAD,
 * QTY_RECONCILED-&gt;PARKING_OUT_FINAL) so the engine already knows which
 * one transition applies from the instance's current state — IN/OUT no
 * longer needs to be told to us, we derive it from whether an open entry
 * already exists for the gate pass: first scan opens it (IN), next scan
 * closes it (OUT), next scan opens a new one (IN), alternating.
 *
 * <p>Vehicle/flow-variant/loading-type context is looked up from gate_pass
 * by gp_number (server-side), not read off the submitted form bag — the
 * page only submits gpNumber (+ instanceId, used by the engine before
 * this listener runs), and gate_pass already has those columns from when
 * the pass was created.
 *
 * <p>Not scoped to a single workflow code — aria:parking-scan is reused
 * across all 6 M10 gate-pass workflows (TankerInbound, PackUnload,
 * ReturnStockIn, TankerOutbound, PackLoading, FGLoading), so this only
 * gates on a submitted gpNumber/gp_number key being present.
 */
@Component
public class ParkingBayEntryTransitionListener implements WorkflowTransitionListener {

    private static final Logger log = LoggerFactory.getLogger(ParkingBayEntryTransitionListener.class);

    private final JdbcTemplate jdbc;

    public ParkingBayEntryTransitionListener(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void onTransition(WorkflowInstance instance, String previousState, Map<String, Object> bag) {
        if (bag == null ) return;
        String gateNo = firstNonBlank(str(bag, "gpNumber"), str(bag, "gp_number"), str(bag, "gateNo"));
        if (gateNo == null || gateNo.isBlank()) return;
        Long tenantId = TenantContext.get();
        try {
            int closed = jdbc.update(
                "UPDATE dispatch_parking_bay_entry SET out_time=CURRENT_TIMESTAMP " +
                "WHERE tenant_id=? AND gate_pass_no=? AND out_time IS NULL " +
                "ORDER BY in_time DESC LIMIT 1",
                tenantId, gateNo);
            if (closed > 0) {
                log.info("parking bay OUT logged for gate pass {}", gateNo);
                return;
            }
            Map<String, Object> gp;
            try {
                gp = jdbc.queryForMap(
                    "SELECT vehicle_no, flow_variant_code, loading_type FROM gate_pass " +
                    "WHERE tenant_id=? AND gp_number=?", tenantId, gateNo);
            } catch (Exception e) {
                log.warn("parking bay in: gate pass {} not found ({})", gateNo, e.getMessage());
                return;
            }
            jdbc.update(
                "INSERT INTO dispatch_parking_bay_entry " +
                "(tenant_id, gate_pass_no, vehicle_no, flow_variant_code, loading_type, in_time) " +
                "VALUES (?,?,?,?,?,CURRENT_TIMESTAMP)",
                tenantId, gateNo, gp.get("vehicle_no"), gp.get("flow_variant_code"), gp.get("loading_type"));
            log.info("parking bay IN logged for gate pass {}", gateNo);
        } catch (Exception e) {
            log.warn("parking bay entry log failed for gate pass {}: {}", gateNo, e.getMessage());
        }
    }

    private static String str(Map<String, Object> bag, String key) {
        Object v = bag.get(key);
        return v == null ? null : String.valueOf(v);
    }

    private static String firstNonBlank(String... vals) {
        for (String v : vals) if (v != null && !v.isBlank()) return v;
        return null;
    }
}
