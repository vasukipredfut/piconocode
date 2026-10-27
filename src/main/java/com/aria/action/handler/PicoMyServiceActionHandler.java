package com.aria.action.handler;

import com.aria.action.ActionHandler;
import com.aria.action.ActionInvocation;
import com.aria.action.ActionResult;
import com.aria.fieldservice.InstallationReportPdf;
import com.aria.fieldservice.ServiceAccessValidator;
import com.aria.security.CurrentRole;
import com.aria.security.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * pico.my-service -- service reads for the mobile app. Engineers / technicians see their own
 * reports and tickets (plus tickets they created); role Service sees all tickets; Manager /
 * TenantAdmin see everything. ERPNext served all of these with allow_guest=True.
 * <ul>
 *   <li>{@code reports}    -- installation reports, optional {@code status}, {@code customer_id}
 *                             (get_employee_installation_reports, get_all_installation_reports)</li>
 *   <li>{@code report}     -- {@code report_id} (get_installation_report)</li>
 *   <li>{@code report-pdf} -- {@code report_id} as base64 PDF (download_installation_report_pdf)</li>
 *   <li>{@code tickets}    -- optional {@code status}, {@code customer_id} (get_issues)</li>
 *   <li>{@code ticket}     -- {@code ticket_id} with comments (get_issues with issue)</li>
 * </ul>
 */
@Component
public class PicoMyServiceActionHandler implements ActionHandler {

    public static final String CODE = "pico.my-service";

    private static final String REPORT_COLUMNS =
            "r.id, r.report_number, r.doc_no, r.report_date, r.customer_id, c.customer_name, r.ticket_id, r.engineer_emp_id, " +
            "e.full_name AS engineer, r.address, r.contact_person_name, r.contact_number, r.email, r.warranty_kind, r.call_receipt_date, " +
            "r.nature_of_call, r.call_date_from, r.call_date_to, r.machine_type, r.machine_model, r.primary_communication, " +
            "r.pre_work_observation, r.action_summary, r.remarks_by_customer, r.work_done_nature, r.system_operation, " +
            "r.suggestions_for_trouble, r.purpose_reason, r.status, r.otp_sent_at, r.otp_sent_to, r.otp_expires_at, r.otp_verified_at, " +
            "r.cancel_reason, r.created_at";
    private static final String REPORT_FROM =
            " FROM m9_installation_report r JOIN m8_customer c ON c.tenant_id = r.tenant_id AND c.id = r.customer_id " +
            "JOIN m7_employee e ON e.tenant_id = r.tenant_id AND e.id = r.engineer_emp_id ";
    private static final String TICKET_COLUMNS =
            "t.id, t.ticket_no, t.subject, t.description, t.customer_id, t.customer_name, t.contact_name, t.contact_mobile, " +
            "t.contact_email, t.site_address, t.issue_kind, t.priority, t.source, t.reported_at, t.technician_id, " +
            "te.full_name AS technician, t.scheduled_for, t.status, t.resolution, t.closed_at, t.workflow_instance_id";
    private static final String TICKET_FROM =
            " FROM m9_service_ticket t LEFT JOIN m7_employee te ON te.tenant_id = t.tenant_id AND te.id = t.technician_id ";

    @Autowired private JdbcTemplate jdbc;
    @Autowired private InstallationReportPdf reportPdf;

    @Override
    public String code() { return CODE; }

    @Override
    public ActionResult handle(ActionInvocation inv) {
        Map<String, Object> a = inv.getArgs() == null ? Map.of() : inv.getArgs();
        Map<String, Object> emp = PicoSelf.employee(jdbc);
        if (emp == null) return ActionResult.error("Employee not linked to this user");
        long me = ((Number) emp.get("id")).longValue();
        Long tenantId = TenantContext.get();
        boolean admin = CurrentRole.isAny(jdbc, CurrentRole.MANAGER, CurrentRole.TENANT_ADMIN);
        boolean serviceTeam = admin || CurrentRole.isAny(jdbc, ServiceAccessValidator.SERVICE_ROLE);
        String view = PicoSelf.str(a.get("view"));
        String status = PicoLeadCreateActionHandler.text(a, "status", 32, false);
        Long customerId = a.get("customer_id") == null ? null : PicoLeadCreateActionHandler.id(a.get("customer_id"), "customer_id");

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("employee_id", me);
        switch (view == null ? "reports" : view) {
            case "reports" -> {
                List<Object> p = new ArrayList<>(List.of(tenantId));
                StringBuilder w = new StringBuilder("WHERE r.tenant_id=?");
                if (!admin) { w.append(" AND r.engineer_emp_id=?"); p.add(me); }
                if (status != null) { w.append(" AND r.status=?"); p.add(status.toUpperCase()); }
                if (customerId != null) { w.append(" AND r.customer_id=?"); p.add(customerId); }
                List<Map<String, Object>> rows = jdbc.queryForList("SELECT " + REPORT_COLUMNS + REPORT_FROM + w +
                        " ORDER BY r.created_at DESC LIMIT 500", p.toArray());
                data.put("count", rows.size());
                data.put("reports", rows);
            }
            case "report", "report-pdf" -> {
                if (a.get("report_id") == null) throw new IllegalArgumentException("report_id is required");
                long id = PicoLeadCreateActionHandler.id(a.get("report_id"), "report_id");
                List<Map<String, Object>> rows = jdbc.queryForList("SELECT " + REPORT_COLUMNS + REPORT_FROM +
                        "WHERE r.tenant_id=? AND r.id=?", tenantId, id);
                if (rows.isEmpty() || (!admin && ((Number) rows.get(0).get("engineer_emp_id")).longValue() != me)) {
                    return ActionResult.error("Installation Report not found");
                }
                if ("report".equals(view)) {
                    data.put("report", rows.get(0));
                } else {
                    byte[] pdf = reportPdf.pdf(id);
                    if (pdf == null) return ActionResult.error("Could not render the report PDF");
                    data.put("filename", rows.get(0).get("report_number") + ".pdf");
                    data.put("content_type", "application/pdf");
                    data.put("pdf_base64", Base64.getEncoder().encodeToString(pdf));
                }
            }
            case "tickets" -> {
                List<Object> p = new ArrayList<>(List.of(tenantId));
                StringBuilder w = new StringBuilder("WHERE t.tenant_id=?");
                if (!serviceTeam) { w.append(" AND (t.technician_id=? OR t.created_by_emp_id=?)"); p.add(me); p.add(me); }
                if (status != null) { w.append(" AND t.status=?"); p.add(status.toUpperCase()); }
                if (customerId != null) { w.append(" AND t.customer_id=?"); p.add(customerId); }
                List<Map<String, Object>> rows = jdbc.queryForList("SELECT " + TICKET_COLUMNS + TICKET_FROM + w +
                        " ORDER BY t.reported_at DESC LIMIT 500", p.toArray());
                data.put("count", rows.size());
                data.put("tickets", rows);
            }
            case "ticket" -> {
                if (a.get("ticket_id") == null) throw new IllegalArgumentException("ticket_id is required");
                long id = PicoLeadCreateActionHandler.id(a.get("ticket_id"), "ticket_id");
                List<Map<String, Object>> rows = jdbc.queryForList("SELECT " + TICKET_COLUMNS + ", t.created_by_emp_id" + TICKET_FROM +
                        "WHERE t.tenant_id=? AND t.id=?", tenantId, id);
                if (rows.isEmpty()) return ActionResult.error("Ticket not found");
                Map<String, Object> t = new LinkedHashMap<>(rows.get(0));
                boolean mine = same(t.get("technician_id"), me) || same(t.get("created_by_emp_id"), me);
                if (!serviceTeam && !mine) return ActionResult.error("Ticket not found");
                t.put("comments_and_activity", jdbc.queryForList(
                        "SELECT id, author_name, comment, is_internal, created_at FROM m9_ticket_comment WHERE tenant_id=? AND ticket_id=? ORDER BY created_at",
                        tenantId, id));
                t.put("installation_reports", jdbc.queryForList(
                        "SELECT id, report_number, report_date, status FROM m9_installation_report WHERE tenant_id=? AND ticket_id=? ORDER BY id",
                        tenantId, id));
                data.put("ticket", t);
            }
            default -> throw new IllegalArgumentException("view must be reports, report, report-pdf, tickets or ticket");
        }
        return ActionResult.builder().ok(true).data(data).build();
    }

    private static boolean same(Object a, long b) {
        return a instanceof Number n && n.longValue() == b;
    }
}
