package com.aria.action.handler;

import com.aria.action.ActionHandler;
import com.aria.action.ActionInvocation;
import com.aria.action.ActionResult;
import com.aria.fieldservice.InstallationOtpService;
import com.aria.security.CurrentRole;
import com.aria.security.CurrentUser;
import com.aria.security.TenantContext;
import com.aria.workflow.engine.WorkflowEngineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * pico.installation-otp -- replaces ERPNext verify_installation_report_otp (which was
 * allow_guest=True with unlimited tries) and adds resend. The report's engineer or a Manager.
 *
 * <p>args: {@code report_id}, {@code op} = verify (with {@code otp}) | resend.
 * verify: InstallationOtpService checks expiry / attempts / hash; on success otp_verified_at is
 * stamped and the report moves to VERIFIED via pico:installation-verify (ServiceAccessValidator
 * refuses that transition without the stamp).
 */
@Component
public class PicoInstallationOtpActionHandler implements ActionHandler {

    public static final String CODE = "pico.installation-otp";

    @Autowired private JdbcTemplate jdbc;
    @Autowired private InstallationOtpService otp;
    @Autowired private WorkflowEngineService workflowEngine;

    @Override
    public String code() { return CODE; }

    @Override
    public ActionResult handle(ActionInvocation inv) {
        Map<String, Object> a = inv.getArgs() == null ? Map.of() : inv.getArgs();
        if (a.get("report_id") == null) throw new IllegalArgumentException("report_id is required");
        long id = PicoLeadCreateActionHandler.id(a.get("report_id"), "report_id");
        String op = PicoSelf.str(a.get("op"));
        Map<String, Object> emp = PicoSelf.employee(jdbc);
        if (emp == null) return ActionResult.error("Employee not linked to this user");
        Long tenantId = TenantContext.get();
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT * FROM m9_installation_report WHERE tenant_id=? AND id=?", tenantId, id);
        if (rows.isEmpty()) return ActionResult.error("Installation Report not found");
        Map<String, Object> report = rows.get(0);
        boolean mine = ((Number) report.get("engineer_emp_id")).longValue() == ((Number) emp.get("id")).longValue();
        if (!mine && !CurrentRole.isAny(jdbc, CurrentRole.MANAGER, CurrentRole.TENANT_ADMIN)) return ActionResult.error("Installation Report not found");
        if (!"OTP_PENDING".equals(report.get("status"))) return ActionResult.error("Report is already " + report.get("status"));

        if ("resend".equals(op)) {
            List<String> sentTo;
            try {
                sentTo = otp.issue(report);
            } catch (IllegalStateException e) {
                return ActionResult.error(e.getMessage());
            }
            if (sentTo.isEmpty()) return ActionResult.error("The code could not be sent (no email / SMS reached the customer)");
            return ActionResult.builder().ok(true).message("OTP sent").recordId(id)
                    .data(Map.of("otp_sent_to", sentTo, "otp_valid_minutes", otp.ttlMinutes())).build();
        }
        if (!"verify".equals(op)) throw new IllegalArgumentException("op must be verify or resend");

        InstallationOtpService.Outcome outcome = otp.verify(report, PicoSelf.str(a.get("otp")));
        switch (outcome) {
            case NO_CODE -> { return reject("No code has been sent for this report; use resend", "NO_CODE"); }
            case EXPIRED -> { return reject("The code has expired; use resend", "EXPIRED"); }
            case LOCKED -> { return reject("Too many wrong attempts; use resend for a new code", "LOCKED"); }
            case WRONG -> { return reject("OTP is not valid", "WRONG"); }
            default -> { }
        }
        jdbc.update("UPDATE m9_installation_report SET otp_verified_at=NOW() WHERE tenant_id=? AND id=?", tenantId, id);
        try {
            workflowEngine.handlePageSubmission(((Number) report.get("workflow_instance_id")).longValue(), "pico:installation-verify",
                    Map.of("note", "Customer code verified"), CurrentUser.id());
        } catch (RuntimeException e) {
            jdbc.update("UPDATE m9_installation_report SET otp_verified_at=NULL WHERE tenant_id=? AND id=?", tenantId, id);
            return ActionResult.error("Code was correct but the report could not be closed: " + e.getMessage());
        }
        return ActionResult.builder().ok(true).message("OTP verified successfully and document submitted").recordId(id)
                .data(Map.of("status", "VERIFIED", "report_number", report.get("report_number"))).build();
    }

    private static ActionResult reject(String message, String reason) {
        return ActionResult.builder().ok(false).message(message).data(Map.of("reason", reason)).build();
    }
}
