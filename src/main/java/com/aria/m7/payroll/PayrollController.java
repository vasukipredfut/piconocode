package com.aria.m7.payroll;

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

    public PayrollController(PayrollCalcService calc) {
        this.calc = calc;
    }

    @GetMapping("/preview")
    public Map<String, Object> preview(@RequestParam long empId,
                                        @RequestParam String period) {
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
