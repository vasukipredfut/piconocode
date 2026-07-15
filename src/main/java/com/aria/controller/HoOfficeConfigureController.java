package com.aria.controller;

import com.aria.dto.*;
import com.aria.service.AttendanceService;
import com.aria.service.DwlogService;
import com.aria.service.HoOfficeConfigureService;
import com.aria.service.HoSalesService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class HoOfficeConfigureController {


    public static final Logger logger = org.slf4j.LoggerFactory.getLogger(HoOfficeConfigureController.class);


    @Autowired
    HoOfficeConfigureService hoOfficeConfigureService;

    @Autowired
    HoSalesService hoSalesService;

    @Autowired
    AttendanceService attendanceService;

    @Autowired
    DwlogService dwlogService;

    // GRN
    @PostMapping("/auth/grnBillInward")
    public ResponseEntity<?> grnBillInward(@RequestBody GRNDto billInwardDto, HttpServletRequest request) {

        return hoOfficeConfigureService.grnBillInward(billInwardDto, request);
    }

    // Save Ho Po
    @PostMapping("/auth/saveHoPurchaseOrder")
    public ResponseEntity<?> saveHoPurchaseOrder(@RequestBody HoPurchaseOrderDto purchaseOrderDto,
                                                 HttpServletRequest request) {
        return hoOfficeConfigureService.saveHoPurchaseOrder(request, purchaseOrderDto);
    }

    // Save Ho Sales Invoice
    @PostMapping("/auth/saveHoSalesInvoice")
    public ResponseEntity<?> saveHoSalesInvoice(@RequestBody HoSalesInvoiceDTO hoSalesInvoiceDTO,
                                                HttpServletRequest request) {
        return hoSalesService.saveHoSalesInvoice(hoSalesInvoiceDTO, request);
    }

    // Save Ho Sales Order
    @PostMapping("/auth/saveHoSalesOrder")
    public ResponseEntity<?> saveHoSalesOrder(@RequestBody HoSalesOrderDTO hoSalesOrderDTO,
                                              HttpServletRequest request) {
        return hoSalesService.saveHoSalesOrder(hoSalesOrderDTO, request);
    }

    @GetMapping("/auth/updateGst")
    public void checkGSt() {
        hoSalesService.updateGst();
    }


    // Save Instru Log 

    @PostMapping("/auth/attendance/instruLog")
    public ResponseEntity<?> saveInstruLog(
            @Valid @RequestBody List<InstruLogDto> instruLogList,
            HttpServletRequest request) {

        if (instruLogList == null || instruLogList.isEmpty()) {
            logger.warn("Bulk InstruLog request rejected - Empty payload | IP={}",
                    request.getRemoteAddr());
            Map<String, Object> response = new HashMap<String, Object>();
            response.put("message", "Request body cannot be empty");
            response.put("status", 400);
            response.put("success", false);
            return ResponseEntity
                    .badRequest()
                    .body(response);
        }

        logger.info("Bulk InstruLog request received | Count={} | IP={}",
                instruLogList.size(),
                request.getRemoteAddr());

        return attendanceService.saveInstruLogList(instruLogList);
    }



    // Save DwLog Log

    @PostMapping("/auth/attendance/dwLog")
    public ResponseEntity<?> saveDwLog(@Valid @RequestBody List<DwlogDto> instruLogList,
                                   HttpServletRequest request) {
        if (instruLogList == null || instruLogList.isEmpty()) {
            logger.warn("Bulk DwLog request rejected - Empty payload | IP={}",
                    request.getRemoteAddr());
            Map<String, Object> response = new HashMap<String, Object>();
            response.put("message", "Request body cannot be empty");
            response.put("status", 400);
            response.put("success", false);
            return ResponseEntity
                    .badRequest()
                    .body(response);
        }

        logger.info("Bulk DwLog request received | Count={} | IP={}",
                instruLogList.size(),
                request.getRemoteAddr());

        return dwlogService.saveDwLogList(instruLogList);
    }

}
