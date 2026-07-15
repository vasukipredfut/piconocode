package com.aria.service;

import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.*;
import com.aria.utils.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StockLedgerService {

    public static final Logger logger = LoggerFactory.getLogger(StockLedgerService.class);

    private static final DecimalFormat decfor = new DecimalFormat("0.0000");
    @Autowired
    DateUtil dateUtil;

    @Autowired
    BinstockAuditLogRepository binstockAuditLogRepository;

    @Autowired
    BillInwardRepository billInwardRepository;

    @Autowired
    LotRepository lotRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProductExcessStorageRepository productExcessStorageRepository;

    @Autowired
    PurchaseReturnRepository purchaseReturnRepository;

    @Autowired
    DirectPurchaseRepository directPurchaseRepository;

    @Autowired
    BillForwardRepository billForwardRepository;

    @Autowired
    BillInwardItemReceivedRepository billInwardItemReceivedRepository;

    @Autowired
    JournalEntryRepository journalEntryRepository;

    @Autowired
    GoDownRepository goDownRepository;

    @Autowired
    MachineRepository machineRepository;

    @Autowired
    DwClusterRepository dwClusterRepository;

    @Autowired
    ProductMasterRepository productMasterRepository;

    @Autowired
    JournalProductMappingRepository journalProductMappingRepository;

    @Autowired
    MaterialRequestRepository materialRequestRepository;

    @Autowired
    MaterialIssueRepository materialIssueRepository;

    @Autowired
    IssueItemsRepository issueItemsRepository;

    @Autowired
    SalesReturnRepository salesReturnRepository;

    @Autowired
    SplitTransactionRepository splitTransactionRepository;

    @Autowired
    SplitProductDetailsRepository splitProductDetailsRepository;

    @Autowired
    DepartmentDetailsRepository departmentDetailsRepository;

    @Autowired
    BranchRepository branchRepository;

    public ResponseEntity<?> pCateProductAndLots(String pCategory) throws UsernameNotFoundException {
        List<PCateProduct> list = lotRepository.findBy(pCategory);
        List<PCateProductDto> result = list.stream().map(data -> {
            PCateProductDto pCateProductDto = new PCateProductDto(data);
            return pCateProductDto;
        }).collect(Collectors.toList());
        return new ResponseEntity<List<PCateProductDto>>(result, HttpStatus.OK);
    }

   /* public ResponseEntity<?> filterStockLedger(Long companyId, Long branchId, Long deptId, String group, String
            stockCategory, Long productId, Double lot, String fromDate, String toDate, Pageable pageable) throws
            UsernameNotFoundException {

        String logCheck = "br = " + branchId + " # " + "dt = " + deptId + " # " + "pro = " + productId + " # ";
        System.out.println(logCheck);
        Date startDate = null;
        Date endDate = null;

        if (fromDate != null && toDate != null) {
            startDate = dateUtil.getStringTODateWk(fromDate);
            endDate = dateUtil.getStringTODateWk(toDate);
        } else if (fromDate != null && toDate == null) {
            startDate = dateUtil.getStringTODateWk(fromDate);
        } else if (fromDate == null && toDate != null) {
            endDate = dateUtil.getStringTODateWk(toDate);
        }

        System.out.println("startDate = " + startDate);
        System.out.println("endDate = " + endDate);

        List<StockLedger> list = new ArrayList<>();
        List<StockLedgerDto> result = new ArrayList<>();
        *//* if (pageable.getPageNumber() == 0) {
         *//**//*StockLedger stockLedger = binstockAuditLogRepository.getStockLedgerOpening(companyId, branchId, productId, lot, startDate);
            if (stockLedger != null) {
                StockLedgerDto stockLedgerDto = new StockLedgerDto(stockLedger, "OPENING");
                result.add(stockLedgerDto);
            }*//**//*
            System.out.println("startDate = " + startDate);
            System.out.println("endDate = " + endDate);*//*
        // list = binstockAuditLogRepository.getStockLedger(companyId, branchId, productId, lot, startDate, endDate);

        *//*} else {
            System.out.println("startDate = " + startDate);
            System.out.println("endDate = " + endDate);
            list = binstockAuditLogRepository.getStockLedgerNotInOpening(companyId, branchId, productId, lot, startDate, endDate, pageable);
        }*//*
        // commented by alagu need only all list not in pagedata
        *//*if ((pageable.getPageNumber() == 0 || pageable.getPageNumber() != 0) && !list.isEmpty()) {
            List<StockLedgerDto> result1 = list.toList().stream().map(data -> {
                StockLedgerDto stockLedgerDto = new StockLedgerDto(data);
                return stockLedgerDto;
            }).collect(Collectors.toList());
            result.addAll(result1);
        } else if (pageable.getPageNumber() != 0 && list.isEmpty()) {
            return new ResponseEntity<PageDataDto<StockLedgerDto>>(new PageDataDto<>(), HttpStatus.OK);
        }
        PageDataDto<StockLedgerDto> pageData = new PageDataDto(result, list);
        return new ResponseEntity<PageDataDto<StockLedgerDto>>(pageData, HttpStatus.OK);*//*

        StockLedger listOpening = binstockAuditLogRepository.getStockLedger(companyId, branchId, productId, lot, startDate, "OPENING");
        if (listOpening != null) {
            list.add(listOpening);
        }
        List<StockLedger> listAll = binstockAuditLogRepository.getStockLedger(companyId, branchId, productId, lot, startDate, endDate);
        if (!listAll.isEmpty()) {
            list.addAll(listAll);
        }
        StockLedger listClosing = binstockAuditLogRepository.getStockLedger(companyId, branchId, productId, lot, endDate, "CLOSING");
        if (listClosing != null) {
            list.add(listClosing);
        }

        result = list.stream().map(data -> {
            StockLedgerDto stockLedgerDto = new StockLedgerDto(data);
            if (stockLedgerDto.getBalance() != null && stockLedgerDto.getBalance() != "")
                stockLedgerDto.setBalance(decfor.format(Double.valueOf(stockLedgerDto.getBalance())));
            if (stockLedgerDto.getReceipt() != null && stockLedgerDto.getReceipt() != "")
                stockLedgerDto.setReceipt(decfor.format(Double.valueOf(stockLedgerDto.getReceipt())));
            if (stockLedgerDto.getIssue() != null && stockLedgerDto.getIssue() != "")
                stockLedgerDto.setIssue(decfor.format(Double.valueOf(stockLedgerDto.getIssue())));
            return stockLedgerDto;
        }).collect(Collectors.toList());

        return new ResponseEntity<List<StockLedgerDto>>(result, HttpStatus.OK);
    }*/




    public ResponseEntity<List<StockLedgerDto>> filterStockLedger(
            Long companyId,
            Long branchId,
            Long deptId,
            String group,
            String stockCategory,
            Long productId,
            Double lot,
            String fromDate,
            String toDate, Pageable pageable) throws UsernameNotFoundException {

        Date startDate = null;
        Date endDate = null;

        if (fromDate != null) {
            startDate = dateUtil.getStringTODateWk(fromDate);
        }
        if (toDate != null) {
            endDate = dateUtil.getStringTODateWk(toDate);
        }

        List<StockLedger> ledgerList = new ArrayList<>();

        // 1️⃣ Opening
        if (startDate != null) {
            StockLedger opening = binstockAuditLogRepository
                    .getStockLedger(companyId, branchId, productId, lot, startDate, "OPENING");

            if (opening != null) {
                ledgerList.add(opening);
            }
        }

        // 2️⃣ Transactions
        List<StockLedger> transactions = binstockAuditLogRepository
                .getStockLedger(companyId, branchId, productId, lot, startDate, endDate);

        if (transactions != null && !transactions.isEmpty()) {
            ledgerList.addAll(transactions);
        }

        // 3️⃣ Closing
        if (endDate != null) {
            StockLedger closing = binstockAuditLogRepository
                    .getStockLedger(companyId, branchId, productId, lot, endDate, "CLOSING");

            if (closing != null) {
                ledgerList.add(closing);
            }
        }

        // 4️⃣ Sort by date
        ledgerList.sort(Comparator.comparing(StockLedger::getId));

        // 5️⃣ Running Balance Calculation
        List<StockLedgerDto> result = new ArrayList<>();
        double runningBalance = 0.0;

        for (StockLedger ledger : ledgerList) {

            StockLedgerDto dto = new StockLedgerDto(ledger);

            double receipt = dto.getReceipt() != null ? dto.getReceipt() : 0.0;
            double issue = dto.getIssue() != null ? dto.getIssue() : 0.0;

            if(!dto.getEntryType().equalsIgnoreCase("CLOSING") ) {
                runningBalance += receipt - issue;
            }

            dto.setBalance(String.valueOf(runningBalance));

            if(dto.getEntryType().equalsIgnoreCase("OPENING") || dto.getEntryType().equalsIgnoreCase("CLOSING")){
                dto.setReceipt(null);
            }


            result.add(dto);
        }

        return new ResponseEntity<>(result, HttpStatus.OK);
    }




    public ResponseEntity<?> stockLedgerView(Long id) throws UsernameNotFoundException {
        Optional<BinstockAuditLog> optional = binstockAuditLogRepository.findById(id);
        if (optional.isPresent()) {
            BinstockAuditLog rs = optional.get();
            if (rs.getEntry_type().equalsIgnoreCase("Stock Inward")) {

                Optional<BillInward> optionalBillInward = billInwardRepository.findById(rs.getBill_id());
                if (optionalBillInward.isPresent()) {
                    Optional<DirectPurchase> optionalDirectPurchase = directPurchaseRepository.findBy(optionalBillInward.get().getBillNo(), optionalBillInward.get().getId());
                    if (optionalDirectPurchase.isPresent()) {
                        BinstockAuditResponse response = new BinstockAuditResponse();
                        DirectPurchaseDto directPurchaseDto = new DirectPurchaseDto(optionalDirectPurchase.get());

                        if (directPurchaseDto.getUpdatedBy() != null) {
                            Optional<User> user = userRepository.findById(directPurchaseDto.getUpdatedBy());
                            if (user.isPresent()) {
                                directPurchaseDto.setStockInwardBy(user.get().getEmployeename());
                            }
                        }
                        if (directPurchaseDto.getApprovedBy() != null) {
                            Optional<User> user = userRepository.findById(directPurchaseDto.getApprovedBy());
                            if (user.isPresent()) {
                                directPurchaseDto.setApprovedByName(user.get().getEmployeename());
                            }
                        }

                        Optional<BillForward> optionalBillForward = billForwardRepository.findByBillInwardId(optionalBillInward.get().getId());
                        if (optionalBillForward.isPresent()) {
                            if (optionalBillForward.get().getStatus() != null && optionalBillForward.get().getStatus().equalsIgnoreCase("Pending") && optionalBillForward.get().getStatus().equalsIgnoreCase("Verify")) {
                                directPurchaseDto.setBillVerified("Success");
                            } else if (optionalBillForward.get().getStatus() != null && optionalBillForward.get().getStatus().equalsIgnoreCase("Pending")) {
                                directPurchaseDto.setBillVerified("Success");
                            }
                        }

                        response.setDirectPurchaseDto(directPurchaseDto);

                        DirectPurchaseList optionalDirectPurchase1 = directPurchaseRepository.getDirectPurchaseListbyId(directPurchaseDto.getId());
                        if (optionalDirectPurchase1 != null) {
                            DirectPurchaseListDto directPurchaseDto1 = new DirectPurchaseListDto(optionalDirectPurchase1);

                            BillInward billInward = optionalBillInward.get();
                            directPurchaseDto1.setBillFrightAmount(billInward.getBillFreightAmount());
                            directPurchaseDto1.setEwayBill(billInward.getEwayBill());
                            directPurchaseDto1.setLorryWithoutWeight(billInward.getLorry_without_Weight());
                            directPurchaseDto1.setLorryWithWeight(billInward.getLorry_with_Weight());
                            directPurchaseDto1.setLorrySerialNo(billInward.getLorrySerialNo());

                            directPurchaseDto1.setLorryDetails(billInward.getBillInwardLorryDetails() != null ? billInward.getBillInwardLorryDetails().stream().map(field -> new BillInwardLorryDto(field)).collect(Collectors.toList()) : null);

                            if (billInward.getCreatedBy() != null) {
                                Optional<User> userOptional = userRepository.findById(billInward.getCreatedBy());
                                if (userOptional.isPresent())
                                    directPurchaseDto1.setBillInwardperson(userOptional.get().getEmployeename());
                            } else {
                                Optional<User> userOptional = userRepository.findById(billInward.getUpdatedBy());
                                if (userOptional.isPresent())
                                    directPurchaseDto1.setBillInwardperson(userOptional.get().getEmployeename());
                            }

                            if (optionalDirectPurchase.get().getApproved_by() != null) {
                                Optional<User> userOptional = userRepository.findById(optionalDirectPurchase.get().getApproved_by());
                                if (userOptional.isPresent())
                                    directPurchaseDto1.setBillApprovePerson(userOptional.get().getEmployeename());
                            }

                            if (optionalDirectPurchase.get().getAuthorised_by() != null) {
                                Optional<User> userOptional = userRepository.findById(optionalDirectPurchase.get().getAuthorised_by());
                                if (userOptional.isPresent())
                                    directPurchaseDto1.setBillAuthorizedPerson(userOptional.get().getEmployeename());
                            }

                            directPurchaseDto1.setInsurance(billInward.getInsurance());
                            directPurchaseDto1.setLorryno(billInward.getLorryno());
                            directPurchaseDto1.setRoundOfType(billInward.getRound_of_type());
                            directPurchaseDto1.setRoundOfValue(billInward.getRound_of_value());
                            directPurchaseDto1.setCessTax(billInward.getCessTax());

                            if (directPurchaseDto.getUpdatedBy() != null) {
                                Optional<User> optionalUser = userRepository.findById(directPurchaseDto.getUpdatedBy());
                                if (optionalUser.isPresent())
                                    directPurchaseDto1.setStockInwardperson(optionalUser.get().getEmployeename());
                            }

                            if (billInward.getProductCategory().equalsIgnoreCase("RawMaterial")) {
                                directPurchaseDto1.setCgst(billInward.getCgst());
                                directPurchaseDto1.setIgst(billInward.getIgst());
                                directPurchaseDto1.setSgst(billInward.getSgst());
                                directPurchaseDto1.setCgstper(billInward.getCgstper());
                                directPurchaseDto1.setSgstper(billInward.getSgstper());
                                directPurchaseDto1.setIgstper(billInward.getIgstper());
                            } else {
                                List<BillInwardItemReceived> list = billInwardItemReceivedRepository.listBillInwardProducts(billInward.getId());
                                if (list != null && list.size() == 1) {
                                    directPurchaseDto1.setCgst(list.get(0).getCgst());
                                    directPurchaseDto1.setIgst(list.get(0).getIgst());
                                    directPurchaseDto1.setSgst(list.get(0).getSgst());
                                }
                            }
                            response.setDirectPurchaseListDto(directPurchaseDto1);
                        }
                        return new ResponseEntity<BinstockAuditResponse>(response, HttpStatus.OK);
                    } else {
                        return new ResponseEntity<DirectPurchaseListDto>(HttpStatus.NOT_FOUND);
                    }
                } else {
                    return new ResponseEntity<DirectPurchaseListDto>(HttpStatus.NOT_FOUND);
                }

            } else if (rs.getEntry_type().equalsIgnoreCase("Excess")) {

                Optional<BillInward> optionalBillInward = billInwardRepository.findById(rs.getBill_id());
                if (optionalBillInward.isPresent()) {
                    Optional<ProductExcessStorage> optionalProductExcessStorage = productExcessStorageRepository
                            .findBy(optionalBillInward.get().getVendor().getId(), optionalBillInward.get().getBillNo(), optionalBillInward.get().getBillDate(), optionalBillInward.get().getReceivedDate());
                    ProductExcessStorageDto productExcessStorageDto = new ProductExcessStorageDto(optionalProductExcessStorage.get());
                    productExcessStorageDto.setBillInwardDate(dateUtil.getDateTOStringWk(optionalProductExcessStorage.get().getBillInwardDate()));
                    productExcessStorageDto.setBillDate(dateUtil.getDateTOStringWk(optionalProductExcessStorage.get().getBillDate()));

                    if (productExcessStorageDto.getCreatedBy() != null) {
                        Optional<User> userOptional = userRepository.findById(productExcessStorageDto.getCreatedBy());
                        if (userOptional.isPresent()) {
                            productExcessStorageDto.setCreatedByName(userOptional.get().getEmployeename());
                        }
                    }

                    if (productExcessStorageDto.getUpdatedBy() != null) {
                        Optional<User> userOptional = userRepository.findById(productExcessStorageDto.getUpdatedBy());
                        if (userOptional.isPresent()) {
                            productExcessStorageDto.setUpdatedByName(userOptional.get().getEmployeename());
                        }
                    }

                    if (productExcessStorageDto.getRejectedBy() != null) {
                        Optional<User> userOptional = userRepository.findById(productExcessStorageDto.getRejectedBy());
                        if (userOptional.isPresent()) {
                            productExcessStorageDto.setRejectedByName(userOptional.get().getEmployeename());
                        }
                    }
                    if (productExcessStorageDto.getVerifiedBy() != null) {
                        Optional<User> userOptional = userRepository.findById(productExcessStorageDto.getVerifiedBy());
                        if (userOptional.isPresent()) {
                            productExcessStorageDto.setVerifiedByName(userOptional.get().getEmployeename());
                        }
                    }

                    return new ResponseEntity<ProductExcessStorageDto>(productExcessStorageDto, HttpStatus.OK);
                } else {
                    return new ResponseEntity<ProductExcessStorageDto>(HttpStatus.NOT_FOUND);
                }

            } else if (rs.getEntry_type().equalsIgnoreCase("Shortage")) {

                Optional<BillInward> optionalBillInward = billInwardRepository.findById(rs.getBill_id());
                if (optionalBillInward.isPresent()) {
                    Optional<ProductExcessStorage> optionalProductExcessStorage = productExcessStorageRepository
                            .findBy(optionalBillInward.get().getVendor().getId(), optionalBillInward.get().getBillNo(), optionalBillInward.get().getBillDate(), optionalBillInward.get().getReceivedDate());
                    ProductExcessStorageDto productExcessStorageDto = new ProductExcessStorageDto(optionalProductExcessStorage.get());
                    productExcessStorageDto.setBillInwardDate(dateUtil.getDateTOStringWk(optionalProductExcessStorage.get().getBillInwardDate()));
                    productExcessStorageDto.setBillDate(dateUtil.getDateTOStringWk(optionalProductExcessStorage.get().getBillDate()));
                    if (productExcessStorageDto.getCreatedBy() != null) {
                        Optional<User> userOptional = userRepository.findById(productExcessStorageDto.getCreatedBy());
                        if (userOptional.isPresent()) {
                            productExcessStorageDto.setCreatedByName(userOptional.get().getEmployeename());
                        }
                    }

                    if (productExcessStorageDto.getUpdatedBy() != null) {
                        Optional<User> userOptional = userRepository.findById(productExcessStorageDto.getUpdatedBy());
                        if (userOptional.isPresent()) {
                            productExcessStorageDto.setUpdatedByName(userOptional.get().getEmployeename());
                        }
                    }

                    if (productExcessStorageDto.getRejectedBy() != null) {
                        Optional<User> userOptional = userRepository.findById(productExcessStorageDto.getRejectedBy());
                        if (userOptional.isPresent()) {
                            productExcessStorageDto.setRejectedByName(userOptional.get().getEmployeename());
                        }
                    }
                    if (productExcessStorageDto.getVerifiedBy() != null) {
                        Optional<User> userOptional = userRepository.findById(productExcessStorageDto.getVerifiedBy());
                        if (userOptional.isPresent()) {
                            productExcessStorageDto.setVerifiedByName(userOptional.get().getEmployeename());
                        }
                    }

                    return new ResponseEntity<ProductExcessStorageDto>(productExcessStorageDto, HttpStatus.OK);
                } else {
                    return new ResponseEntity<ProductExcessStorageDto>(HttpStatus.NOT_FOUND);
                }

            } else if (rs.getEntry_type().equalsIgnoreCase("PRODN")) {


                Optional<JournalEntry> journalEntry = journalEntryRepository.getJournalEntry(rs.getBatchno(), rs.getTrnno(), rs.getBatch_id());

                if (journalEntry.isPresent()) {
                    JournalEntryView journalEntryView = journalEntryRepository.getJournalEntryView(journalEntry.get().getSjid(),
                            journalEntry.get().getAtrndate(), journalEntry.get().getBatchno(), journalEntry.get().getTrnno(), journalEntry.get().getPshiftid());
                    if (journalEntryView != null) {
                        JournalEntryViewDto result = new JournalEntryViewDto(journalEntryView);
                        if (result.getAby() != null) {
                            Optional<User> optionalUser = userRepository.findById(result.getAby());
                            if (optionalUser.isPresent()) {
                                result.setAccountingByName(optionalUser.get().getEmployeename());
                            }
                        }

                        if (result.getEby() != null) {
                            Optional<User> optionalUser = userRepository.findById(result.getEby());
                            if (optionalUser.isPresent()) {
                                result.setEntryByName(optionalUser.get().getEmployeename());
                            }
                        }

                        if (result.getWcid() != null) {

                            if (result.getWcTable() != null && result.getWcTable().equalsIgnoreCase("machine")) {
                                Optional<Machine> optionalMachine = machineRepository.findById(result.getWcid());
                                if (optionalMachine.isPresent()) {
                                    result.setWorkCenterName(optionalMachine.get().getName());
                                } else {
                                    System.out.println("no Machine");
                                }
                            } else {
                                Optional<DwCluster> optionalMachine = dwClusterRepository.findById(result.getWcid());
                                if (optionalMachine.isPresent()) {
                                    result.setWorkCenterName(optionalMachine.get().getName());
                                } else {
                                    System.out.println("no dwcluster");
                                }
                            }
                        }

                        if (result.getGodownId() != null) {
                            Optional<GoDown> goDownOptional = goDownRepository.findById(result.getGodownId());
                            if (goDownOptional.isPresent()) {
                                result.setGodownName(goDownOptional.get().getName());
                            }
                        }

                        updateJournalEntryView(result, journalEntry.get().getSjid(), journalEntry.get().getAtrndate(), journalEntry.get().getBatchno(), journalEntry.get().getTrnno(), journalEntry.get().getPshiftid());
                        return new ResponseEntity<JournalEntryViewDto>(result, HttpStatus.OK);
                    } else {
                        return new ResponseEntity<JournalEntryViewDto>(HttpStatus.NOT_FOUND);
                    }
                }
                return new ResponseEntity<BillInwardDto>(HttpStatus.NOT_FOUND);

            } else if (rs.getEntry_type().equalsIgnoreCase("PurchaseReturn")) {

                Optional<BillInward> optionalBillInward = billInwardRepository.findById(rs.getBill_id());
                if (optionalBillInward.isPresent()) {
                    Optional<PurchaseReturn> optionalPurchaseReturn = purchaseReturnRepository.findBy(optionalBillInward.get().getVendor().getId(), optionalBillInward.get().getBillNo(), optionalBillInward.get().getBillDate(), optionalBillInward.get().getReceivedDate());
                    PurchaseReturnDto purchaseReturnDto = new PurchaseReturnDto(optionalPurchaseReturn.get());
                    purchaseReturnDto.setBillInwardDate(dateUtil.getDateTOStringWk(optionalPurchaseReturn.get().getBillInwardDate()));
                    purchaseReturnDto.setBillDate(dateUtil.getDateTOStringWk(optionalPurchaseReturn.get().getBillDate()));
                    Optional<User> user = userRepository.findById(purchaseReturnDto.getCreatedBy());
                    if (user.isPresent()) {
                        purchaseReturnDto.setCreatedByName(user.get().getEmployeename());
                    }
                    if (purchaseReturnDto.getUpdatedBy() != null) {
                        Optional<User> user1 = userRepository.findById(purchaseReturnDto.getUpdatedBy());
                        if (user1.isPresent()) {
                            purchaseReturnDto.setUpdatedByName(user.get().getEmployeename());
                        }
                    }
                    if (purchaseReturnDto.getRejectedBy() != null) {
                        Optional<User> userOptional = userRepository.findById(purchaseReturnDto.getRejectedBy());
                        if (userOptional.isPresent()) {
                            purchaseReturnDto.setRejectedByName(userOptional.get().getEmployeename());
                        }
                    }
                    if (purchaseReturnDto.getVerifiedBy() != null) {
                        Optional<User> userOptional = userRepository.findById(purchaseReturnDto.getVerifiedBy());
                        if (userOptional.isPresent()) {
                            purchaseReturnDto.setVerifiedByName(userOptional.get().getEmployeename());
                        }
                    }

                    return new ResponseEntity<PurchaseReturnDto>(purchaseReturnDto, HttpStatus.OK);
                } else {
                    return new ResponseEntity<ProductExcessStorageDto>(HttpStatus.NOT_FOUND);
                }

            } else if (rs.getEntry_type().equalsIgnoreCase("MaterialIssue") || rs.getEntry_type().equalsIgnoreCase("MaterialReceive")) {

                Optional<MaterialIssue> optionalMaterialIssue = materialIssueRepository.findByIssue(rs.getTrnno());
                if (optionalMaterialIssue.isPresent()) {
                    MaterialIssueDto materialIssueDto = new MaterialIssueDto(optionalMaterialIssue.get());
                    if (optionalMaterialIssue.get().getRequestDate() != null) {
                        materialIssueDto.setRequestDate(dateUtil.getDateTOStringWk(optionalMaterialIssue.get().getRequestDate()));
                        materialIssueDto
                                .setRequestDateString(dateUtil.getDateTOStringWk(optionalMaterialIssue.get().getRequestDate()));
                    }
                    if (optionalMaterialIssue.get().getIssueDate() != null) {
                        materialIssueDto.setIssueDate(dateUtil.getDateTOStringWk(optionalMaterialIssue.get().getIssueDate()));
                        materialIssueDto.setIssueDateString(dateUtil.getDateTOStringWk(optionalMaterialIssue.get().getIssueDate()));
                    }
                    Optional<MaterialRequest> opMaterialIssue = materialRequestRepository.findById(materialIssueDto.getRequestId());
                    if (opMaterialIssue.isPresent()) {
                        MaterialRequest materialRequest = opMaterialIssue.get();
                        materialIssueDto.setRequestFrom(materialRequest.getRequestBy().getEmployeename());
                        materialIssueDto.setIssueBriefItems(getIssueItemBrief(materialRequest, optionalMaterialIssue.get().getId()));
                    }
                    return new ResponseEntity<MaterialIssueDto>(materialIssueDto, HttpStatus.OK);
                } else {
                    return new ResponseEntity<Status>(HttpStatus.NOT_FOUND);
                }

            } else if (rs.getEntry_type().equalsIgnoreCase("SALESRETURN") || rs.getEntry_type().equalsIgnoreCase("SELFTRANSFER")) {

                Optional<SalesReturn> aReturn = salesReturnRepository.findBy(rs.getTrnno());
                if (aReturn.isPresent()) {
                    SalesReturn salesReturn = aReturn.get();
                    SalesReturnDto salesReturnDto = new SalesReturnDto(salesReturn);
                    return new ResponseEntity<SalesReturnDto>(salesReturnDto, HttpStatus.OK);
                } else {
                    return new ResponseEntity<ProductExcessStorageDto>(HttpStatus.NOT_FOUND);
                }

            } else if (rs.getEntry_type().equalsIgnoreCase("SPLIT")) {

                Optional<SplitTransaction> splitTransaction = splitTransactionRepository.getSplitTransaction(null, rs.getTrnno());

                if (splitTransaction.isPresent()) {
                    SplitTransactionDto result = new SplitTransactionDto(splitTransaction.get());

                    Optional<ProductMaster> productMaster = productMasterRepository.findById(result.getRefid());
                    if (productMaster.isPresent()) {
                        result.setProductName(productMaster.get().getProductName());
                    }

                    Optional<DepartmentDetails> departmentDetails = departmentDetailsRepository.findById(result.getDept());
                    if (departmentDetails.isPresent()) {
                        result.setDeptName(departmentDetails.get().getName());
                    }

                    Optional<Branch> branch1 = branchRepository.findById(result.getBranch());
                    if (branch1.isPresent()) {
                        result.setBranchName(branch1.get().getBranchName());
                    }

                    Optional<User> optionalUser = userRepository.findById(result.getCreatedBy());
                    if (optionalUser.isPresent()) {
                        result.setCreatedByName(optionalUser.get().getEmployeename());
                    }

                    List<SplitProductDetails> list = splitProductDetailsRepository.viewSplitProductDetails(result.getId());
                    result.setProductDetailsDTOList(list.stream().map(user -> {
                        SplitProductDetailsDTO splitListDto = new SplitProductDetailsDTO(user);
                        return splitListDto;
                    }).collect(Collectors.toList()));

                    return new ResponseEntity<SplitTransactionDto>(result, HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                }
            }

        } else {
            return new ResponseEntity<Status>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<Status>(HttpStatus.NOT_FOUND);
    }


    public void updateJournalEntryView(JournalEntryViewDto journalEntryViewDto, Long journalId, Date shiftDate, String batchNo, String trnNo, Long shiftId) {

        List<JournalEntry> list = journalEntryRepository.getJournalEntryViewList(journalId, shiftDate, batchNo, trnNo, shiftId);
        if (!list.isEmpty()) {
            List<JournalEntryDto> result = list.stream().map(data -> {
                JournalEntryDto j = new JournalEntryDto(data);
                if (j.getProid() != null) {
                    Optional<ProductMaster> optional = productMasterRepository.findById(j.getProid());
                    if (optional.isPresent()) {
                        j.setProductName(optional.get().getProductName());
                    }
                   /* Optional<JournalProductMapping> journalProductMappingOptional = journalProductMappingRepository.findByJournalProductMapping(journalId, j.getProid(), j.getType());
                    if (journalProductMappingOptional.isPresent()) {
                        j.setBomTakeType(journalProductMappingOptional.get().getBomTakeType());
                    }*/
                }
                return j;
            }).collect(Collectors.toList());
            journalEntryViewDto.setOutputProducts(result);
        } else {
            journalEntryViewDto.setOutputProducts(new ArrayList<>());
        }
    }


    public List<IssueItemsBriefDto> getIssueItemBrief(MaterialRequest materialRequest, Long issueId) {
        List<RequestItem> requestItems = materialRequest.getRequestItems();
        List<IssueItemsBriefDto> dtoList = new ArrayList<>();

        Map<Long, Map<String, Double>> allocatedQuantitiesMap = new HashMap<>();

        for (RequestItem requestItem : requestItems) {
            IssueItemsBriefDto issueItemsBriefDto = new IssueItemsBriefDto();
            issueItemsBriefDto.setItemName(requestItem.getItemName());
            issueItemsBriefDto.setItemId(requestItem.getItemId());
            issueItemsBriefDto.setIssueItemId(issueId);
            List<IssueItems> issueItems = issueItemsRepository.findAllByIssueItemIdAndItemAndLot(issueId,
                    requestItem.getItemId(), requestItem.getSelectLot());
            Double pendingQuantity = 0.00;
            Double allocatedQuantity = 0.00;
            Double requestedQuantity = Double.valueOf(requestItem.getStockRequired());
            if (!issueItems.isEmpty()) {
                for (IssueItems items : issueItems) {
                    try {
                        if (items.getAllocatedQuantity() != null) {
                            Double value = Double.valueOf(items.getAllocatedQuantity());
                            allocatedQuantity += value;
                            allocatedQuantitiesMap.computeIfAbsent(requestItem.getItemId(), k -> new HashMap<>()).merge(items.getSelectLot(), value, Double::sum);
                        }
                    } catch (NumberFormatException e) {

                    }

                }
            }
            Double allocatedQuantityForLot = allocatedQuantitiesMap
                    .getOrDefault(requestItem.getItemId(), Collections.emptyMap())
                    .getOrDefault(requestItem.getSelectLot(), 0.0);
            issueItemsBriefDto.setLot(requestItem.getSelectLot());
            issueItemsBriefDto.setAllocatedQuantity(allocatedQuantityForLot);
            issueItemsBriefDto.setRequestedQuantity(requestedQuantity);
            issueItemsBriefDto.setPendingQuantity(pendingQuantity);
            dtoList.add(issueItemsBriefDto);
        }
        return dtoList;
    }

}
