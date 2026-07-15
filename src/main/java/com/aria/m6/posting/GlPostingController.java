package com.aria.m6.posting;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 *  POST /api/m6/gl/post/sales-invoice/{id}
 *  POST /api/m6/gl/post/purchase-invoice/{id}
 *  POST /api/m6/gl/post/payroll?period=YYYY-MM
 *  POST /api/m6/gl/post/all-pending
 */
@RestController
@RequestMapping("/api/m6/gl/post")
public class GlPostingController {

    private final GlAutoPostingService svc;

    public GlPostingController(GlAutoPostingService svc) {
        this.svc = svc;
    }

    @PostMapping("/sales-invoice/{id}")
    public Map<String, Object> postSi(@PathVariable long id) {
        return svc.postSalesInvoice(id);
    }

    @PostMapping("/purchase-invoice/{id}")
    public Map<String, Object> postPi(@PathVariable long id) {
        return svc.postPurchaseInvoice(id);
    }

    @PostMapping("/payroll")
    public Map<String, Object> postPayroll(@RequestParam String period) {
        return svc.postPayroll(period);
    }

    @PostMapping("/all-pending")
    public Map<String, Object> postAll() {
        return svc.postAllPending();
    }
}
