package com.aria.action.handler;

import com.aria.security.CurrentUser;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Records WHO did a bay step on the loading / unloading bay rows the bay actions open and close, so every flow (FG,
 * Pack, Stock inward / outward, Tanker, reject-unload) answers "who started / who finished this bay line".
 *
 * <p>Two facts are kept side by side:
 * <ul>
 *   <li>the logged-in user's id (JWT principal -- cannot be edited by the client): {@code started_by_id},
 *       {@code weighed_by_id}, {@code unloaded_by_id};</li>
 *   <li>the person the form says did the work ({@code performed_by_name}, an editable field -- someone else may
 *       have done it): {@code started_by_name}, {@code weighed_by_name}, {@code unloaded_by_name}.</li>
 * </ul>
 * Everything is skipped when there is no authenticated user, and only rows that are not stamped yet are touched,
 * so another operator's earlier stamp on a sibling row of the same SKU is never overwritten.
 */
final class RowActorStamp {

    private RowActorStamp() {}

    /** {@code performed_by_name} off a line, else off the form; null when neither carries one. */
    static String performedBy(Map<String, Object> line, Map<String, Object> formData) {
        String v = name(line);
        return v != null ? v : name(formData);
    }

    private static String name(Map<String, Object> m) {
        if (m == null) return null;
        Object v = m.get("performed_by_name") != null ? m.get("performed_by_name") : m.get("performedByName");
        if (v == null || v.toString().isBlank()) return null;
        String s = v.toString().trim();
        return s.length() > 128 ? s.substring(0, 128) : s;
    }

    /** After Start Bay inserted the row(s) for this line. */
    static void startedLoading(JdbcTemplate jdbc, long tenantId, Long instanceId, String routeId, String orderId,
                               String skuCode, String bayName, String performedBy) {
        started(jdbc, "loading_progress_detail_bay", tenantId, instanceId, routeId, orderId, skuCode, bayName, performedBy);
    }

    static void startedUnloading(JdbcTemplate jdbc, long tenantId, Long instanceId, String routeId, String orderId,
                                 String skuCode, String bayName, String performedBy) {
        started(jdbc, "unloading_progress_detail_bay", tenantId, instanceId, routeId, orderId, skuCode, bayName, performedBy);
    }

    /** After Bay Complete closed this line's loading row(s). */
    static void completedLoading(JdbcTemplate jdbc, long tenantId, Long instanceId, String routeId, String orderId,
                                 String skuCode, String performedBy) {
        completed(jdbc, "loading_progress_detail_bay", "weighed_by", tenantId, instanceId, routeId, orderId, skuCode, performedBy);
    }

    /** After an unload / receipt closed this line's unloading row(s). */
    static void completedUnloading(JdbcTemplate jdbc, long tenantId, Long instanceId, String routeId, String orderId,
                                   String skuCode, String performedBy) {
        completed(jdbc, "unloading_progress_detail_bay", "unloaded_by", tenantId, instanceId, routeId, orderId, skuCode, performedBy);
    }

    /** Tanker weight Save closed ONE tank row (known id). */
    static void weighedLoadingRow(JdbcTemplate jdbc, long tenantId, Long rowId, String performedBy) {
        byRowId(jdbc, "loading_progress_detail_bay", "weighed_by", tenantId, rowId, performedBy);
    }

    /** Tanker unload / receipt touched ONE tank row (known id). */
    static void unloadedRow(JdbcTemplate jdbc, long tenantId, Long rowId, String performedBy) {
        byRowId(jdbc, "unloading_progress_detail_bay", "unloaded_by", tenantId, rowId, performedBy);
    }

    private static void byRowId(JdbcTemplate jdbc, String table, String prefix, long tenantId, Long rowId, String performedBy) {
        Long actor = CurrentUser.id();
        if (actor == null || rowId == null) return;
        List<Object> args = new ArrayList<>();
        args.add(actor);
        String set = prefix + "_id = ?";
        if (performedBy != null) { set += ", " + prefix + "_name = ?"; args.add(performedBy); }
        args.add(tenantId);
        args.add(rowId);
        jdbc.update("UPDATE " + table + " SET " + set + " WHERE tenant_id = ? AND id = ?", args.toArray());
    }

    private static void started(JdbcTemplate jdbc, String table, long tenantId, Long instanceId, String routeId,
                                String orderId, String skuCode, String bayName, String performedBy) {
        Long actor = CurrentUser.id();
        if (actor == null) return;
        List<Object> args = new ArrayList<>();
        args.add(actor);
        String set = "started_by_id = ?";
        if (performedBy != null) { set += ", started_by_name = ?"; args.add(performedBy); }
        args.add(tenantId); args.add(instanceId); args.add(routeId); args.add(orderId); args.add(skuCode); args.add(bayName);
        jdbc.update("UPDATE " + table + " SET " + set + " WHERE tenant_id = ? AND workflow_instance_id = ? "
                        + "AND route_id <=> ? AND order_id <=> ? AND sku_code = ? AND bay_name <=> ? "
                        + "AND status = 'IN_PROGRESS' AND started_by_id IS NULL",
                args.toArray());
    }

    private static void completed(JdbcTemplate jdbc, String table, String prefix, long tenantId, Long instanceId,
                                  String routeId, String orderId, String skuCode, String performedBy) {
        Long actor = CurrentUser.id();
        if (actor == null) return;
        List<Object> args = new ArrayList<>();
        args.add(actor);
        String set = prefix + "_id = ?";
        if (performedBy != null) { set += ", " + prefix + "_name = ?"; args.add(performedBy); }
        args.add(tenantId); args.add(instanceId); args.add(routeId); args.add(orderId); args.add(skuCode);
        jdbc.update("UPDATE " + table + " SET " + set + " WHERE tenant_id = ? AND workflow_instance_id = ? "
                        + "AND route_id <=> ? AND order_id <=> ? AND sku_code = ? "
                        + "AND status = 'DONE' AND " + prefix + "_id IS NULL",
                args.toArray());
    }
}
