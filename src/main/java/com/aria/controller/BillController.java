package com.aria.controller;

import com.aria.dao.UserRepository;
import com.aria.dto.*;
import com.aria.model.HOConfigRESTLog;
import com.aria.model.Status;
import com.aria.model.User;
import com.aria.service.BillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class BillController {

    @Autowired
    BillService billService;

    @Autowired
    UserRepository userRepository;

    @PostMapping("/saveBill")
    public ResponseEntity<?> saveBill(@RequestBody BillInwardDto billInwardDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return billService.saveBill(billInwardDto, user.get());
    }

    @PostMapping("/saveBillInward")
    public ResponseEntity<?> saveBillInward(@RequestBody BillInwardDto billInwardDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return billService.saveBillInward(billInwardDto, user.get());
    }

    @PutMapping("/updateBillInward")
    public ResponseEntity<?> updateBillInward(@RequestBody BillInwardDto billInwardDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return billService.updateBillInward(billInwardDto, user.get());
    }

    @GetMapping("/getBillInwardList")
    public ResponseEntity<?> getBillInwardList(Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return billService.listAllBillInward(pageable);
        } else {
            return new ResponseEntity<List<BillInwardDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/getLorrySerialNo")
    public ResponseEntity<?> getLorrySerialNo(Authentication authentication,
                                              @RequestParam(value = "itemType") String itemType,
                                              @RequestParam(value = "lorryNo", required = false) String lorryNo,
                                              @RequestParam(value = "inwardDate", required = false) String inwardDate) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return billService.getTransactionType(itemType, lorryNo, inwardDate);
        } else {
            return new ResponseEntity<List<BillInwardDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/checkEwayBill")
    public ResponseEntity<?> checkEwayBill(Authentication authentication,
                                           @RequestParam(value = "vendorId", required = false) Long vendorId,
                                           @RequestParam(value = "ewayBillNo") String ewayBillNo) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return billService.checkEwayBill(vendorId, ewayBillNo);
        } else {
            return new ResponseEntity<List<BillInwardDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/filterBillList")
    public ResponseEntity<?> filterBillList(@RequestParam("billFilterType") String billFilterType,

                                            @RequestParam(value = "referenceNumber", required = false) String referenceNumber,
                                            @RequestParam(value = "vendorId", required = false) Long vendorId,
                                            @RequestParam(value = "vendorName", required = false) String vendorName,
                                            @RequestParam(value = "inwardBranchId", required = false) Long inwardBranchId,
                                            @RequestParam(value = "firmId", required = false) Long firmId,
                                            @RequestParam(value = "firmName", required = false) String firmName,

                                            @RequestParam(value = "billNumber", required = false) String billNumber,
                                            @RequestParam(value = "fromBranchId", required = false) Long fromBranchId,
                                            @RequestParam(value = "fromBranchName", required = false) String fromBranchName,
                                            @RequestParam(value = "toBranchId", required = false) Long toBranchId,
                                            @RequestParam(value = "toBranchName", required = false) String toBranchName,

                                            @RequestParam(value = "deliveryBranchId", required = false) Long deliveryBranchId,
                                            @RequestParam(value = "deliveryBranchName", required = false) String deliveryBranchName,
                                            @RequestParam(value = "departmentId", required = false) Long departmentId,
                                            @RequestParam(value = "departmentName", required = false) String departmentName,

                                            @RequestParam(value = "status", required = false) String status,
                                            @RequestParam(value = "inwardBranchName", required = false) String inwardBranchName,
                                            @RequestParam(value = "dateFrom", required = false) String dateFrom,
                                            @RequestParam(value = "dateTo", required = false) String dateTo, Pageable pageable,
                                            Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {

            if (billNumber == null && status == null) {
                status = "Pending";
            }

            if (billFilterType.equals("billInward")) {

                return billService.filterBillInwardList(firmId, firmName, referenceNumber, vendorId, vendorName, inwardBranchId, status, dateFrom, dateTo, inwardBranchName, pageable);

            } else if (billFilterType.equals("billForward")) {

                if (fromBranchId == null) {
                    fromBranchId = user.get().getBranch_id();
                }
                return billService.filterBillForwardList(firmId, firmName, billNumber, fromBranchId, fromBranchName, toBranchId,
                        toBranchName, status, dateFrom, dateTo, pageable);

            } else if (billFilterType.equals("billExchange")) {

                return billService.filterBillExchangeList(firmId, firmName, billNumber, fromBranchId, toBranchId, toBranchName, status,
                        dateFrom, dateTo, deliveryBranchId, deliveryBranchName, pageable);

            } else if (billFilterType.equals("billReceive")) {

                return billService.filterBillReceiveList(firmId, firmName, billNumber, fromBranchId, toBranchId, status, dateFrom, dateTo, pageable);

            } else {
                return ResponseEntity.badRequest().body(new Status("400",
                        "Error: Invalid billFilterType value. The value of billFilterType must be any one of billInward, billForward, billExchange or billReceive"));
            }
        } else {
            throw new RuntimeException("Error: 403 Forbidden");
        }
    }

    @GetMapping("/getBillInwardDetails/{id}")
    public ResponseEntity<?> getBillInwardDetails(@PathVariable String id, Authentication authentication) {

        return billService.getBillInwardDetails(id);
    }

    @DeleteMapping("/deleteBillInwardDetails/{id}")
    public ResponseEntity<?> deleteBillInwardDetails(@PathVariable String id, Authentication authentication) {

        return billService.deleteBillInward(id);
    }

    // Bill Forward

    @PutMapping("/updateBillForward")
    public ResponseEntity<?> updateBillForward(@RequestBody List<BillForwardDto> billForwardDto,
                                               Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return billService.updateForward(billForwardDto, user.get());
    }

    @GetMapping("/getBillForwardList")
    public ResponseEntity<?> getBillForwardList(Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return billService.listAllBillForward(pageable);
        } else {
            return new ResponseEntity<List<BillForwardDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/getBillForwardDetails/{id}")
    public ResponseEntity<?> getBillForwardDetails(@PathVariable String id, Authentication authentication) {

        return billService.getBillForwardDetails(id);
    }

    @DeleteMapping("/deleteBillForwardDetails/{id}")
    public ResponseEntity<?> deleteBillForwardDetails(@PathVariable String id, Authentication authentication) {

        return billService.deleteBillForward(id);
    }

   /* // Bill receive Details

    @PostMapping("/saveBillReceive")
    public ResponseEntity<?> saveBillReceive(@RequestBody BillReceiveDto billReceiveDto,
                                             Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return billService.saveReceive(billReceiveDto, user.get());
    }

    @PutMapping("/updateBillReceive")
    public ResponseEntity<?> updateBillReceive(@RequestBody BillReceiveDto billReceiveDto,
                                               Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return billService.updateReceive(billReceiveDto, user.get());
    }

    @GetMapping("/getBillReceiveList")
    public ResponseEntity<?> getBillReceiveList(Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return billService.listAllBillReceive(pageable);
        } else {
            return new ResponseEntity<List<BillReceiveDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/getBillReceiveDetails/{id}")
    public ResponseEntity<?> getBillReceiveDetails(@PathVariable String id, Authentication authentication) {

        return billService.getBillReceiveDetails(id);
    }

    @DeleteMapping("/deleteBillReceiveDetails/{id}")
    public ResponseEntity<?> deleteBillReceiveDetails(@PathVariable String id, Authentication authentication) {

        return billService.deleteBillReceive(id);
    }*/

    // Bill Exchange

    @PutMapping("/updateBillExchange")
    public ResponseEntity<?> updateBillExchange(@RequestBody List<BillExchangeDto> billExchangeDto,
                                                Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return billService.updateExchange(billExchangeDto, user.get());
    }

    @PostMapping("/verifyBillExchange")
    public ResponseEntity<?> verifyBillExchange(@RequestBody BillVerifyDto billVerifyDto,
                                                Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return billService.verifyExchange(billVerifyDto, user.get());
    }

    @PostMapping("/ApproveBillReceive")
    public ResponseEntity<?> approveBillReceive(@RequestBody BillVerifyDto billVerifyDto,
                                                Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return billService.verifyExchange(billVerifyDto, user.get());
    }

    @GetMapping("/getBillExchangeList")
    public ResponseEntity<?> getBillExchangeList(Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return billService.listAllBillExchange(pageable);
        } else {
            return new ResponseEntity<List<MasterComponentDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/getBillExchangeDetails/{id}")
    public ResponseEntity<?> getBillExchangeDetails(@PathVariable String id, Authentication authentication) {

        return billService.getBillExchangeDetails(id);
    }

    @DeleteMapping("/deleteBillExchangeDetails/{id}")
    public ResponseEntity<?> deleteBillExchangeDetails(@PathVariable String id,
                                                       @RequestParam("type") String type, Authentication authentication) {

        return billService.deleteBillExchange(id, type);
    }

    // Bill Dp Details

    @PostMapping("/saveDirectPurchase")
    public ResponseEntity<?> saveDirectPurchase(@RequestBody DirectPurchaseDto directPurchaseDto,
                                                Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return billService.saveDirectPurchase(directPurchaseDto, user.get());
    }

    @PutMapping("/updateDirectPurchase")
    public ResponseEntity<?> updateDirectPurchase(@RequestBody DirectPurchaseDto directPurchaseDto,
                                                  Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return billService.updateDirectPurchase(directPurchaseDto, user.get());
    }

    @PutMapping("/updateStockStatus")
    public ResponseEntity<?> updateStockStatus(@RequestParam(value = "id", required = false) Long id,
                                               @RequestParam(value = "stockStatus", required = false) String stockStatus, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return billService.updateStockStatus(id, stockStatus, user.get());
    }

    @GetMapping("/getDirectPurchaseList")
    public ResponseEntity<?> getDirectPurchaseList(@RequestParam(value = "billno", required = false) String billno,
                                                   @RequestParam(value = "productCategory", required = false) String productCategory,
                                                   @RequestParam(value = "orgId", required = false) Long orgId,
                                                   @RequestParam(value = "branchId", required = false) Long branchId,
                                                   @RequestParam(value = "deptId", required = false) Long deptId,
                                                   @RequestParam(value = "vendorId", required = false) Long vendorId,
                                                   @RequestParam(value = "vendorName", required = false) String vendorName,
                                                   @RequestParam(value = "dateForm", required = false) String dateForm,
                                                   @RequestParam(value = "dateTo", required = false) String dateTo,
                                                   @RequestParam(value = "status", required = false) String status, Pageable pageable,
                                                   Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            /*if (branchId == null) {
                branchId = user.get().getBranch_id();
            }*/
            return billService.listAllDirectPurchase(productCategory, billno, orgId, branchId, deptId, vendorId, vendorName, dateForm, dateTo, status, pageable);

        } else {
            return new ResponseEntity<List<DirectPurchaseDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/getDirectPurchaseDetails/{id}")
    public ResponseEntity<?> getDirectPurchaseDetails(@PathVariable String id, Authentication authentication) {

        return billService.getDirectPurchaseDetails(id);
    }

    @GetMapping("/getDirectPurchaseListbyId/{id}")
    public ResponseEntity<?> getDirectPurchaseListDetails(@PathVariable Long id, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        return billService.getDirectPurchaseListbyId(id, user.get());
    }

    @DeleteMapping("/deleteDirectPurchaseDetails/{id}")
    public ResponseEntity<?> deleteDirectPurchaseDetails(@PathVariable String id, Authentication authentication) {

        return billService.deleteDirectPurchase(id);
    }

    @GetMapping("/searchBillInwardReference")
    public ResponseEntity<?> searchBillInwardReference(Authentication authentication,
                                                       @RequestParam(value = "vendorName", required = false) String vendorName,
                                                       @RequestParam(value = "vendorId", required = false) Long vendorId,
                                                       @RequestParam(value = "deliveryBranchId", required = false) Long deliveryBranchId,
                                                       @RequestParam(value = "referenceNumber", required = false) String referenceNumber) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return billService.listAllBillReferenceNumberAndVendor(referenceNumber, vendorName, vendorId, deliveryBranchId);
        } else {
            return new ResponseEntity<List<ProductDistruptancesBillInwardDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/searchInwardReferenceAndBatch")
    public ResponseEntity<?> searchBillInwardReference(Authentication authentication,
                                                       @RequestParam(value = "referenceNumber", required = false) String referenceNumber,
                                                       @RequestParam(value = "vendorName", required = false) String vendorName,
                                                       @RequestParam(value = "batchNo", required = false) String batchNo) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return billService.listAllBillReferenceNumber(referenceNumber, batchNo);
        } else {
            return new ResponseEntity<List<BillInwardDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/lorryArrivalStatus")
    public ResponseEntity<?> lorryArrivalStatus(Authentication authentication,
                                                @RequestParam(value = "billNo", required = false) String billNo,
                                                @RequestParam(value = "vendorId", required = false) Long vendorId,
                                                @RequestParam(value = "inwardBranchId", required = false) Long inwardBranchId,
                                                @RequestParam(value = "employeeId", required = false) Long employeeId,
                                                @RequestParam(value = "inwardId", required = false) Long inwardId,
                                                @RequestParam(value = "startDate", required = false) String startDate,
                                                @RequestParam(value = "endDate", required = false) String endDate, Pageable pageable) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return billService.filterBillInwardLorryArrivalList(billNo, vendorId, inwardBranchId, employeeId, inwardId,
                    startDate, endDate, pageable);
        } else {
            return new ResponseEntity<List<BillInwardDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/filterInwardInspectionBatchORBillNo")
    public ResponseEntity<?> filterInwardInspectionBatchList(Authentication authentication,
                                                             @RequestParam(value = "productId", required = false) Long productId,
                                                             @RequestParam(value = "date", required = false) String date, Pageable pageable) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return billService.filterInwardInspectionBatchList(productId, date, pageable);
        } else {
            return new ResponseEntity<List<InwardInspectionBatchListDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @PutMapping("/billVerify")
    public ResponseEntity<?> billVerify(Authentication authentication,
                                        @RequestParam(value = "stockId", required = false) Long stockId,
                                        @RequestParam(value = "billInwardId", required = false) Long billInwardId,
                                        @RequestParam(value = "eTransfer", required = false) String eTransfer,
                                        @RequestParam(value = "creditOrDebit", required = false) String creditOrDebit) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return billService.approvebill(stockId, billInwardId, eTransfer, creditOrDebit, user.get());
        } else {
            return new ResponseEntity<List<InwardInspectionBatchListDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @PutMapping("/billReject")
    public ResponseEntity<?> billReject(Authentication authentication,
                                        @RequestParam(value = "stockId", required = false) Long stockId,
                                        @RequestParam(value = "billInwardId", required = false) Long billInwardId) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return billService.billReject(stockId, billInwardId, user.get());
        } else {
            return new ResponseEntity<List<InwardInspectionBatchListDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    @PutMapping("/billApproveOrAuthorise")
    public ResponseEntity<?> billApproveOrAuthorise(Authentication authentication,
                                                    @RequestParam(value = "type", required = false) String type,
                                                    @RequestParam(value = "stockId", required = false) Long stockId,
                                                    @RequestParam(value = "billInwardId", required = false) Long billInwardId) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return billService.billApproveOrAuthorise(type, stockId, billInwardId, user.get());
        } else {
            return new ResponseEntity<List<InwardInspectionBatchListDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    @PutMapping("/mdBillVerify")
    public ResponseEntity<?> mdBillVerify(Authentication authentication,
                                          @RequestParam(value = "stockId", required = false) Long stockId,
                                          @RequestParam(value = "billInwardId", required = false) Long billInwardId) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return billService.billVerify(stockId, billInwardId, user.get());
        } else {
            return new ResponseEntity<List<InwardInspectionBatchListDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/findByJaggeryBillList")
    public ResponseEntity<?> findByJaggeryBillList(Authentication authentication,
                                                   @RequestParam(value = "vendorId", required = false) Long vendorId,
                                                   @RequestParam(value = "billDate", required = false) String billDate) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return billService.findByJaggeryBillList(vendorId, billDate);
        } else {
            return new ResponseEntity<List<BillInwardDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @PostMapping("/checkRejectBill")
    public ResponseEntity<?> checkRejectBill(Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return billService.getResponseData1(Long.valueOf(415), user.get());
    }

    @GetMapping("/updateLorryWeight/{id}")
    public ResponseEntity<?> updateLorryWeight(@PathVariable String id, @RequestParam("lorryWeight") String lorryWeight,
                                               Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return billService.updateLorryWeight(id, lorryWeight);
    }

    @GetMapping("/checkLorrySerialNo")
    public ResponseEntity<?> checkLorrySerialNo(Authentication authentication,
                                                @RequestParam(value = "itemType", required = false) String itemType,
                                                @RequestParam(value = "lorrySerialNo", required = false) String lorrySerialNo,
                                                @RequestParam(value = "lorryNo", required = false) String lorryNo,
                                                @RequestParam(value = "inwardDate", required = false) String inwardDate) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return billService.checkLorrySerialNo(itemType, lorryNo, inwardDate, lorrySerialNo);
        } else {
            return new ResponseEntity<List<BillInwardDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @PutMapping("/amendmentBill")
    public ResponseEntity<?> amendmentBill(@RequestParam(value = "id") Long id,
                                           @RequestParam(value = "inwardDate") String inwardDate,
                                           @RequestParam(value = "lorrySerialNo", required = false) String lorrySerialNo,
                                           Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return billService.amendmentBill(id, inwardDate, lorrySerialNo);
    }

    @PutMapping("/updateManualBillInward")
    public ResponseEntity<?> updateManualBillInward(@RequestBody BillInwardDto billInwardDto,
                                                    Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return billService.updateManualBillInward(billInwardDto, user.get());
    }

    @PutMapping("/updateConfigHoBillInward")
    public ResponseEntity<?> updateConfigHoBillInward(@RequestParam("inwardId") String inwardId,
                                                      Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return billService.updateConfigHoBillInward(inwardId);
    }


    @PutMapping("/updateConfigHoBillInWardItems")
    public ResponseEntity<?> updateConfigHoBillInWardItems(@RequestParam("inwardId") String inwardId,
                                                           Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return billService.updateConfigHoBillInWardItems(inwardId);
    }


    @PutMapping("/updateConfigHoBillForward")
    public ResponseEntity<?> updateConfigHoBillForward(@RequestParam("inwardId") String inwardId,
                                                       Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return billService.updateConfigHoBillForward(inwardId);
    }

    @PutMapping("/updateConfigPO")
    public ResponseEntity<?> updateConfigPO(Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        billService.updateConfigPO();
        return new ResponseEntity<>("Sucess", HttpStatus.OK);
    }

    // billinward to dp entry data miss
    @PutMapping("/updateConfigDp")
    public ResponseEntity<?> updateConfigDp(@RequestParam("inwardId") Long inwardId,
                                            Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        billService.updateBillConfig(inwardId);
        return new ResponseEntity<>("Sucess", HttpStatus.OK);
    }

    // dpentry to forward data miss
    @PutMapping("/updateDptoForward")
    public ResponseEntity<?> updateDptoForward(@RequestParam("dpId") Long dpId,
                                            Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        billService.updateDPConfig(dpId);
        return new ResponseEntity<>("Sucess", HttpStatus.OK);
    }


    // dpentry to forward data miss
    @PutMapping("/updateBillInwardToBillChange")
    public ResponseEntity<?> updateBillInwardToBillChange(@RequestParam("inwardId") Long inwardId,
                                               Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        billService.updateBillInwardToBillChange(inwardId);

        return new ResponseEntity<>("Sucess", HttpStatus.OK);
    }


    @GetMapping("/searchBillDetails")
    public ResponseEntity<?> searchBillDetails(Authentication authentication,
                                               @RequestParam(value = "vendorId", required = false) Long vendorId,
                                               @RequestParam(value = "deliveryBranchId", required = false) Long deliveryBranchId,
                                               @RequestParam(value = "referenceNumber", required = false) String referenceNumber,
                                               @RequestParam(value = "type") String type) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return billService.listAllBillNumberAndVendor(referenceNumber, vendorId, deliveryBranchId, type);
        } else {
            return new ResponseEntity<List<ProductDistruptancesBillInwardDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    @GetMapping("/excessOrReturnBillDetails/{id}")
    public ResponseEntity<?> excessOrReturnBillDetails(Authentication authentication, @PathVariable("id") Long id) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return billService.excessOrReturnBillDetails(id);
        } else {
            return new ResponseEntity<List<ProductDistruptancesBillInwardDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }
    @GetMapping("/lorryArrivalData")
    public ResponseEntity<?> lorryArrivalData(Authentication authentication,
                                                @RequestParam(value = "lorryNo", required = false) String lorryNo,
                                                @RequestParam(value = "inwardDate", required = false) String inwardDate,
                                                @RequestParam(value = "inwardBranchName", required = false) String inwardBranchName,
                                                @RequestParam(value = "billDate", required = false) String billDate,
                                                @RequestParam(value = "billNo", required = false) String  billNo,
                                                @RequestParam(value = "ewayBillNo", required = false) String ewayBillNo,
                                                @RequestParam(value = "place", required = false) String place,
                                                @RequestParam(value = "vendorName", required = false) String vendorName,
                                                @RequestParam(value = "rate", required = false) String rate,
                                                @RequestParam(value = "lot", required = false) String lot,
                                                @RequestParam(value = "quantity", required = false) String quantity,
                                                @RequestParam(value = "amount", required = false) String amount,
                                                @RequestParam(value = "freightPaid", required = false) String freightPaid,
                                                @RequestParam(value = "deliverBranchId", required = false) Long deliverBranchId,
                                                @RequestParam(value = "inwarBranchId", required = false) Long inwarBranchId,
                                                @RequestParam(value = "month", required = false) Long month,
                                                @RequestParam(value = "year", required = false) Long year,
                                                @RequestParam(value = "deliverBranchName", required = false) String deliverBranchName,
                                                @RequestParam(value = "updatedDate", required = false) String updatedDate , 
                                                @RequestParam(value = "productName", required = false) String productName ,
                                                @RequestParam(value = "verificationStatus", required = false) String verificationStatus ,
                                               Pageable pageable) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return billService.lorryArrivalData(lorryNo, inwardDate, inwardBranchName,deliverBranchId,inwarBranchId,month,year, billDate, billNo,ewayBillNo,place,vendorName,rate,lot,quantity,amount,freightPaid,
            		deliverBranchName,updatedDate, productName,verificationStatus, pageable);
        } else {
            return new ResponseEntity<List<LorryArrivalDataDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }
}


