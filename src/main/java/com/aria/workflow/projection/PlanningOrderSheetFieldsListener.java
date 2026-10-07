package com.aria.workflow.projection;

import com.aria.security.TenantContext;
import com.aria.workflow.engine.WorkflowTransitionListener;
import com.aria.workflow.model.WorkflowInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * M10.TankerOutbound's Planning Dispatch Order Sheet page
 * ({@code aria:loading-planning-order-sheet-upload}, DISPATCH_VEHICLE_ENTRY
 * -&gt; DISPATCH_LOADING_PLANNING) shows five extra tanker-only form fields the
 * operator types in after uploading the sheet: PO Execution, Payment Type,
 * Loading Type, Delivery Date &amp; Time, Remark. On submit they are saved onto
 * every {@code vehicle_status='active'} row of {@code m5_order_dispatch_detail}
 * for that vehicle (the table that page's upload fills), columns
 * {@code po_execution}/{@code payment_type}/{@code loading_type}/
 * {@code delivery_datetime}/{@code remark} (V354).
 *
 * <p>Only keys actually present in the submitted page data are written, so a
 * resubmission that omits a field leaves its stored value alone. The vehicle is
 * read from the page's {@code vehicle_no}, falling back to the instance's
 * gate_pass row.
 */
@Component
public class PlanningOrderSheetFieldsListener implements WorkflowTransitionListener {

    private static final Logger log = LoggerFactory.getLogger(PlanningOrderSheetFieldsListener.class);
    private static final String PAGE_CODE = "aria:loading-planning-order-sheet-upload";

    private static final List<DateTimeFormatter> DATE_FORMATS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a", Locale.ENGLISH));

    private final JdbcTemplate jdbc;

    public PlanningOrderSheetFieldsListener(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onTransition(WorkflowInstance instance, String previousState, Map<String, Object> bag) {
        if (!"M10.TankerOutbound".equals(instance.getWorkflowCode())) return;
        if (!"DISPATCH_VEHICLE_ENTRY".equals(previousState) || !"DISPATCH_LOADING_PLANNING".equals(instance.getCurrentState())) return;
        if (bag == null || !(bag.get(PAGE_CODE) instanceof Map)) return;
        Map<String, Object> page = (Map<String, Object>) bag.get(PAGE_CODE);

        List<String> sets = new ArrayList<>();
        List<Object> args = new ArrayList<>();
        addText(page, sets, args, "po_execution", "poExecution");
        addText(page, sets, args, "payment_type", "paymentType");
        addText(page, sets, args, "loading_type", "loadingType");
        addText(page, sets, args, "remark");
        if (hasKey(page, "delivery_datetime", "deliveryDatetime", "delivery_date_time")) {
            sets.add("delivery_datetime = ?");
            args.add(parseDateTime(first(page, "delivery_datetime", "deliveryDatetime", "delivery_date_time")));
        }
        if (sets.isEmpty()) return;

        long tenantId = TenantContext.get();
        try {
            String vehicleNo = text(first(page, "vehicle_no", "vehicleNo"));
            if (vehicleNo == null) {
                List<String> rows = jdbc.queryForList(
                        "SELECT vehicle_no FROM gate_pass WHERE tenant_id = ? AND workflow_instance_id = ?",
                        String.class, tenantId, instance.getId());
                vehicleNo = rows.isEmpty() ? null : rows.get(0);
            }
            if (vehicleNo == null) {
                log.warn("planning order-sheet fields: instance={} has no vehicle_no -- nothing saved", instance.getId());
                return;
            }
            args.add(tenantId);
            args.add(vehicleNo);
            int n = jdbc.update("UPDATE m5_order_dispatch_detail SET " + String.join(", ", sets)
                    + " WHERE tenant_id = ? AND vehicle_no = ? AND vehicle_status = 'active'", args.toArray());
            if (n == 0) {
                log.warn("planning order-sheet fields: instance={} vehicle={} has no active order-sheet rows -- upload the sheet first", instance.getId(), vehicleNo);
            }
        } catch (Exception e) {
            log.warn("planning order-sheet fields save failed for instance={}: {}", instance.getId(), e.getMessage());
        }
    }

    private static void addText(Map<String, Object> page, List<String> sets, List<Object> args, String column, String... aliases) {
        String[] keys = new String[aliases.length + 1];
        keys[0] = column;
        System.arraycopy(aliases, 0, keys, 1, aliases.length);
        if (!hasKey(page, keys)) return;
        sets.add(column + " = ?");
        args.add(text(first(page, keys)));
    }

    private static boolean hasKey(Map<String, Object> m, String... keys) {
        for (String k : keys) if (m.containsKey(k)) return true;
        return false;
    }

    private static Object first(Map<String, Object> m, String... keys) {
        for (String k : keys) if (m.get(k) != null) return m.get(k);
        return null;
    }

    private static String text(Object o) {
        if (o == null) return null;
        String s = o.toString().trim();
        return s.isEmpty() ? null : s;
    }

    /** Date-time picker output: ISO local (with or without seconds), ISO with offset/Z, or a few typed layouts. */
    private static Timestamp parseDateTime(Object o) {
        String s = text(o);
        if (s == null) return null;
        try { return Timestamp.valueOf(OffsetDateTime.parse(s).toLocalDateTime()); } catch (Exception ignored) { /* not offset */ }
        for (DateTimeFormatter f : DATE_FORMATS) {
            try { return Timestamp.valueOf(LocalDateTime.parse(s, f)); } catch (Exception ignored) { /* next */ }
        }
        return null;
    }
}
