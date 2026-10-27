package com.aria.hr.payroll;

import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * Trigger payroll calc + return the structured result.
 *
 *  GET  /api/m7/payroll/preview?empId=1&period=2026-05
 *      Computes the payroll for that employee/period and returns the line
 *      list (earnings + deductions + employer side) + gross + net pay.
 *      Does NOT persist anything — purely a preview.
 */
@RestController
@RequestMapping("/api/m7/payroll")
public class PayrollController {

    private final PayrollCalcService calc;
    private final org.springframework.jdbc.core.JdbcTemplate jdbc;

    public PayrollController(PayrollCalcService calc, org.springframework.jdbc.core.JdbcTemplate jdbc) {
        this.calc = calc;
        this.jdbc = jdbc;
    }

    /** Payroll HR only: any empId is accepted, so any logged-in user could preview anyone's salary. */
    @GetMapping("/preview")
    public Map<String, Object> preview(@RequestParam long empId,
                                        @RequestParam String period) {
        if (!com.aria.security.CurrentRole.isAny(jdbc, com.aria.security.CurrentRole.MANAGER, com.aria.security.CurrentRole.TENANT_ADMIN)) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.FORBIDDEN, "Payroll access requires Manager or TenantAdmin");
        }
        PayrollCalcService.PayrollResult r = calc.compute(empId, period);
        return Map.of(
            "empId", r.empId,
            "period", r.period,
            "payableDays", r.payableDays,
            "lopDays", r.lopDays,
            "gross", r.gross == null ? "0" : r.gross.toPlainString(),
            "totalDeduct", r.totalDeduct == null ? "0" : r.totalDeduct.toPlainString(),
            "netPay", r.netPay == null ? "0" : r.netPay.toPlainString(),
            "lines", r.lines,
            "error", r.error == null ? "" : r.error
        );
    }
}
