package com.aria.service;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.*;
import com.aria.utils.*;

@Service
public class CWagesSalaryService {

    private static final DecimalFormat decfor = new DecimalFormat("0.00");

    private static final Logger logger = LoggerFactory.getLogger(CWagesSalaryService.class);

    @Autowired
    CwagesSUMRepository cwagesSUMRepository;

    @Autowired
    CwagesTRNRepository cwagesTRNRepository;

    @Autowired
    CwagesDetailsRepository cwagesDetailsRepository;

    @Autowired
    DutyAllotmentRepository dutyAllotmentRepository;

    @Autowired
    DateUtil dateUtil;

    @Autowired
    EmastDailyRepository emastDailyRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    LuZoneRepository luZoneRepository;

    @Autowired
    AssignDutyRepository assignDutyRepository;

    @Autowired
    DaySalaryRepo daySalaryRepo;

    @Autowired
    SalaryStructureRepo salaryStructureRepo;

    @Autowired
    EpayMasterNewRepository epayMasterNewRepository;

    @Autowired
    TRNIndexRepo trnIndexRepo;

    @Autowired
    MaterialIssueRepository materialIssueRepository;

    @Autowired
    OtherBranchReceiptRepository otherBranchReceiptRepository;

    @Autowired
    BinstockAuditLogRepository binstockAuditLogRepository;

    @Autowired
    WcShiftsRepository wcShiftsRepository;

    @Autowired
    BillInwardRepository billInwardRepository;

    @Autowired
    JournalEntryRepository journalEntryRepository;

    @Autowired
    WithInBranchReceiveRepository withInBranchReceiveRepository;

    @Autowired
    QcInProcessRepository qcInProcessRepository;

    @Autowired
    JournalRepository journalRepository;

    @Autowired
    JournalMachineMappingRepository journalMachineMappingRepository;

    @Autowired
    BinStockLogRepository binStockLogRepository;

    @Autowired
    WithInBranchReceiveItemsRepository withInBranchReceiveItemsRepository;


    @Autowired
    GoDownExternalRepository goDownExternalRepository;


    @Autowired
    RequestGodownItemRepository godownItemRepository;
    
    @Autowired
    private DailyWagesAttendanceRepository wagesAttendanceRepository;
    

	@Autowired
	protected AttendanceRepository attendanceRepository;


    public ResponseEntity<?> getTransactionListByType(String trnDate, TransactionType transactionType,
                                                      Long branch, Long dept) {
        String type = transactionType.name();
        Date trnDates = null;
        if (trnDate != null) {
            trnDates = dateUtil.getStringTODateWk(trnDate);
        }
        List<TransactionList> transactionList = executeRepository(trnDates, type, branch, dept);
        List<transactionDTO> list = transactionList.stream().map(transactionDTO::new).collect(Collectors.toList());
        return new ResponseEntity<List<transactionDTO>>(list, HttpStatus.OK);
    }

    public List<TransactionList> executeRepository(Date trnDates, String type, Long branch, Long dept) {
        switch (type) {
            case "INTRA":
                return withInBranchReceiveRepository.getTransactionListFilterByINTRA(trnDates, type, branch, dept);
            case "INTER_IN":
                return otherBranchReceiptRepository.getTransactionListFilterByINTER_IN(trnDates, type, branch, dept);
            case "INTER_OUT":
                return materialIssueRepository.getTransactionListFilterByINTER_OUT(trnDates, type, branch, dept);
            case "STK_IN":
                return billInwardRepository.getTransactionListFilterBySTK_IN(trnDates, type, branch, dept);
            case "STK_OUT":
                return materialIssueRepository.getTransactionListFilterBySTK_OUT(trnDates, type, branch, dept);
            case "JOURNAL":
                return wcShiftsRepository.getTransactionListFilterByJOURNAL(trnDates, type, branch, dept);
            case "EXT_IN":
                return goDownExternalRepository.getTransactionListFilterByEXTIN(trnDates, type, branch, dept);
            case "EXT_OUT":
                return goDownExternalRepository.getTransactionListFilterByEXTOUT(trnDates, type, branch, dept);
            /*case "QC_PROCESS":
                return qcInProcessRepository.getTransactionListFilterByQCInProcess(shiftDate, type, branch, dept);*/
            default:
                throw new IllegalArgumentException("Invalid selectedTransaction value: " + type);
        }
    }


    public ResponseEntity<?> getJournalDetail(String trnNo) {
        List<journalTypeList> journalList = wcShiftsRepository.getJournalDetails(trnNo);
        List<journalTypeDTO> list = journalList.stream().map(data ->
                {
                    journalTypeDTO journalTypeDTO = new journalTypeDTO(data);
                    List<WagesJournalWork> journalEntries = journalEntryRepository.journalTransActionList(trnNo);
                    journalTypeDTO.setJournalEntryProducts(journalEntries.stream().map(entrydata ->
                            {
                                WagesJournalWorkDTO workDTO = new WagesJournalWorkDTO(entrydata);
                                workDTO.setFinishedQty(cwagesSUMRepository.sumOfAllocatedQty(workDTO.getTrnNo(), workDTO.getProductId(), workDTO.getLot(), "journal"));
                                return workDTO;
                            }
                    ).collect(Collectors.toList()));

                    return journalTypeDTO;
                })
                .collect(Collectors.toList());
        return new ResponseEntity<List<journalTypeDTO>>(list, HttpStatus.OK);
    }


    public ResponseEntity<List<dwagesDTO>> getDWagesEmpList(User user, String shiftType, Long shiftId,
                                                            String shiftDate) {
        Date shiftdate = null;
        if (shiftDate != null) {
            shiftdate = dateUtil.getStringTODateWk(shiftDate);
        }
        List<dwagesList> DWList = assignDutyRepository.getDWagesEmpList(user.getBranch_id(), user.getDepartment().getId(),
                shiftType, shiftId, shiftdate);
        List<dwagesDTO> list = DWList.stream().map(data -> new dwagesDTO(data)).collect(Collectors.toList());
        return new ResponseEntity<List<dwagesDTO>>(list, HttpStatus.OK);
    }


    public void updateSalStatus(CwagesSUM result, CWagesSalaryReqDTO cWagesSalaryReqDTO) {
        String trnNo = result.getReftrnno();
        Long productId = result.getRefid();
        String productType = result.getType();
        String type = cWagesSalaryReqDTO.getTransactionType().name();

        switch (type) {

            case "INTRA":
                Optional<WithInBranchReceive> withInBranchReceiveOptional = withInBranchReceiveRepository.findByTrnNo(trnNo);
                WithInBranchReceive mwr = withInBranchReceiveOptional.get();
                List<WithInBranchReceiveItems> materialItemList = withInBranchReceiveItemsRepository.findMaterialItemList(mwr.getId(), result.getRefid());
                if (!materialItemList.isEmpty()) {
                    for (WithInBranchReceiveItems withInBranchReceiveItems : materialItemList) {

                        Double totalQty = cwagesSUMRepository.findBy(result.getRefid(), result.getReftrnno(), result.getLot(), "intra");
                        Double qty = Double.valueOf(withInBranchReceiveItems.getQuantity());
                        Double rsqty = qty - totalQty;
                        if (rsqty > 0) {
                            withInBranchReceiveItems.setSalStatus(null);
                            withInBranchReceiveItemsRepository.save(withInBranchReceiveItems);
                        }
                        if ((rsqty < 0) || (rsqty == 0)) {
                            withInBranchReceiveItems.setSalStatus("Completed");
                            withInBranchReceiveItemsRepository.save(withInBranchReceiveItems);
                        }
                    }
                }
                break;

            case "INTER_IN":
                Optional<OtherBranchReceipt> otherBranchReceipt = otherBranchReceiptRepository.findByTrnNo(trnNo);
                OtherBranchReceipt obr = otherBranchReceipt.get();
                List<BinStockLog> binStockLogList = binStockLogRepository.getMRNTransactionList(productId, trnNo, "R", result.getLot(), "MATERIAL_TRANSACTION");
                if (!binStockLogList.isEmpty()) {
                    for (BinStockLog binStockLog : binStockLogList) {

                        Double totalQty = cwagesSUMRepository.findBy(result.getRefid(), result.getReftrnno(), result.getLot(), "interin");
                        Double qty = binStockLog.getQty();
                        Double rsqty = qty - totalQty;
                        if (rsqty > 0) {
                            binStockLog.setSalStatus(null);
                            binStockLogRepository.save(binStockLog);
                        }
                        if ((rsqty < 0) || (rsqty == 0)) {
                            binStockLog.setSalStatus("Completed");
                            binStockLogRepository.save(binStockLog);
                        }
                    }
                }
                break;
            case "INTER_OUT":
                Optional<MaterialIssue> materialIssue = materialIssueRepository.findByIssue(trnNo);
                MaterialIssue mi = materialIssue.get();
                List<BinStockLog> issueInterbinStockLogList = binStockLogRepository.getMRNTransactionList(productId, trnNo, "I", result.getLot(), "MATERIAL_TRANSACTION");
                if (!issueInterbinStockLogList.isEmpty()) {
                    for (BinStockLog binStockLog : issueInterbinStockLogList) {

                        Double totalQty = cwagesSUMRepository.findBy(result.getRefid(), result.getReftrnno(), result.getLot(), "interout");
                        Double qty = binStockLog.getQty();
                        Double rsqty = qty - totalQty;
                        if (rsqty > 0) {
                            binStockLog.setSalStatus(null);
                            binStockLogRepository.save(binStockLog);
                        }
                        if ((rsqty < 0) || (rsqty == 0)) {
                            binStockLog.setSalStatus("Completed");
                            binStockLogRepository.save(binStockLog);
                        }
                    }
                }

                break;
            case "STK_IN":
                List<BinStockLog> binstock = binStockLogRepository.getTransactionList(productId, trnNo, result.getLot());
                if (!binstock.isEmpty()) {
                    for (BinStockLog binStockLog : binstock) {

                        Double totalQty = cwagesSUMRepository.findBy(result.getRefid(), result.getReftrnno(), result.getLot(), "stkin");
                        Double qty = binStockLog.getQty();
                        Double rsqty = qty - totalQty;
                        if (rsqty > 0) {
                            binStockLog.setSalStatus(null);
                            binStockLogRepository.save(binStockLog);
                        }
                        if ((rsqty < 0) || (rsqty == 0)) {
                            binStockLog.setSalStatus("Completed");
                            binStockLogRepository.save(binStockLog);
                        }
                    }
                }
                break;
            case "STK_OUT":
                Optional<MaterialIssue> stockOut = materialIssueRepository.findByIssue(trnNo);
                MaterialIssue stockut = stockOut.get();
                List<BinStockLog> issuebinStockLogList = binStockLogRepository.getMRNTransactionList(productId, trnNo, "I", result.getLot(), "MATERIAL_TRANSACTION");
                if (!issuebinStockLogList.isEmpty()) {
                    for (BinStockLog binStockLog : issuebinStockLogList) {

                        Double totalQty = cwagesSUMRepository.findBy(result.getRefid(), result.getReftrnno(), result.getLot(), "stkout");
                        Double qty = binStockLog.getQty();
                        Double rsqty = qty - totalQty;
                        if (rsqty > 0) {
                            binStockLog.setSalStatus(null);
                            binStockLogRepository.save(binStockLog);
                        }
                        if ((rsqty < 0) || (rsqty == 0)) {
                            binStockLog.setSalStatus("Completed");
                            binStockLogRepository.save(binStockLog);
                        }
                    }
                }
                break;
            case "JOURNAL":
                Optional<Wcshift> wcShift = wcShiftsRepository.findByTrnNo(trnNo);
                Wcshift wc = wcShift.get();
                List<JournalEntry> journalEntryList = journalEntryRepository.getJournalEntryTransaction(wc.getJournalId(), productId, trnNo, productType);
                if (!journalEntryList.isEmpty()) {
                    for (JournalEntry journalEntry : journalEntryList) {

                        Double totalQty = cwagesSUMRepository.findBy(result.getRefid(), result.getReftrnno(), result.getLot(), "journal");
                        Double qty = journalEntry.getStkqty();
                        Double rsqty = qty - totalQty;
                        if (rsqty > 0) {
                            journalEntry.setSalStatus(null);
                            journalEntryRepository.save(journalEntry);
                        }
                        if ((rsqty < 0) || (rsqty == 0)) {
                            journalEntry.setSalStatus("Completed");
                            journalEntryRepository.save(journalEntry);
                        }
                    }
                }
                break;

            case "EXT_IN":
                Optional<GoDownExternal> goDownExternalIn = goDownExternalRepository.findByTrnNo(trnNo);
                if (goDownExternalIn.isPresent()) {
                    List<BinStockLog> binStockLogs = binStockLogRepository.getMRNTransactionList(productId, trnNo, "R", result.getLot(), "EXTERNAL_GODOWN");
                    if (!binStockLogs.isEmpty()) {
                        for (BinStockLog binStockLog : binStockLogs) {

                            Double totalQty = cwagesSUMRepository.findBy(result.getRefid(), result.getReftrnno(), result.getLot(), "extin");
                            Double qty = binStockLog.getQty();
                            Double rsqty = qty - totalQty;
                            if (rsqty > 0) {
                                binStockLog.setSalStatus(null);
                                binStockLogRepository.save(binStockLog);
                            }
                            if ((rsqty < 0) || (rsqty == 0)) {
                                binStockLog.setSalStatus("Completed");
                                binStockLogRepository.save(binStockLog);
                            }
                        }
                    }
                }
                break;
            case "EXT_OUT":
                Optional<GoDownExternal> goDownExternal = goDownExternalRepository.findByTrnNo(trnNo);
                if (goDownExternal.isPresent()) {

                    System.out.println("godownId == " + goDownExternal.get().getId() +
                            " productId == " + productId + " lot == " + String.valueOf(result.getLot().intValue()));

                    List<RequestGodownItem> requestGodownItems = godownItemRepository.findBy(goDownExternal.get().getId(), productId, String.valueOf(result.getLot().intValue()));
                    if (!requestGodownItems.isEmpty()) {
                        for (RequestGodownItem requestGodownItem : requestGodownItems) {

                            Double totalQty = cwagesSUMRepository.findBy(result.getRefid(), result.getReftrnno(), result.getLot(), "extout");
                            Double qty = Double.valueOf(requestGodownItem.getAllocatedQuantity());
                            Double rsqty = qty - totalQty;
                            if (rsqty > 0) {
                                requestGodownItem.setSalStatus(null);
                                godownItemRepository.save(requestGodownItem);
                            }
                            if ((rsqty < 0) || (rsqty == 0)) {
                                requestGodownItem.setSalStatus("Completed");
                                godownItemRepository.save(requestGodownItem);
                            }
                        }
                    }
                }
                break;

            default:
                throw new IllegalArgumentException("Invalid selectedTransaction value: " + type);
        }
    }


    public ResponseEntity<?> changeSalStatus(String trnNo, TransactionType transactionType, Long productId, String productType) {
        String type = transactionType.name();
        switch (type) {
            case "INTRA":
                Optional<WithInBranchReceive> materialWithInBranchReceipt = withInBranchReceiveRepository.findByTrnNo(trnNo);
                WithInBranchReceive mwr = materialWithInBranchReceipt.get();
                //mwr.setSalStatus("Completed");
                withInBranchReceiveRepository.save(mwr);
                return new ResponseEntity<>(new Status("200", "Salary Status Updated Sucessfully!!!"), HttpStatus.OK);

            case "INTER_IN":
                Optional<OtherBranchReceipt> otherBranchReceipt = otherBranchReceiptRepository.findByTrnNo(trnNo);
                OtherBranchReceipt obr = otherBranchReceipt.get();
                // obr.setSalStatus("Completed");
                otherBranchReceiptRepository.save(obr);
                return new ResponseEntity<>(new Status("200", "Salary Status Updated Sucessfully!!!"), HttpStatus.OK);

            case "INTER_OUT":
                Optional<MaterialIssue> materialIssue = materialIssueRepository.findByIssue(trnNo);
                MaterialIssue mi = materialIssue.get();
                mi.setSalStatus("Completed");
                materialIssueRepository.save(mi);
                return new ResponseEntity<>(new Status("200", "Salary Status Updated Sucessfully!!!"), HttpStatus.OK);

            case "STK_IN":
                Optional<BinstockAuditLog> binstock = binstockAuditLogRepository.findByTrnno(trnNo);
                BinstockAuditLog bin = binstock.get();
                bin.setSalStatus("Completed");
                binstockAuditLogRepository.save(bin);
                return new ResponseEntity<>(new Status("200", "Salary Status Updated Sucessfully!!!"), HttpStatus.OK);

            case "STK_OUT":
                Optional<MaterialIssue> stockOut = materialIssueRepository.findByIssue(trnNo);
                MaterialIssue stockut = stockOut.get();
                stockut.setSalStatus("Completed");
                materialIssueRepository.save(stockut);
                return new ResponseEntity<>(new Status("200", "Salary Status Updated Sucessfully!!!"), HttpStatus.OK);

            case "JOURNAL":
                Optional<Wcshift> wcShift = wcShiftsRepository.findByTrnNo(trnNo);
                Wcshift wc = wcShift.get();
                if (type != null) {

                }
                List<JournalEntry> journalEntryList = journalEntryRepository.getJournalEntryTransaction(wc.getJournalId(), productId, trnNo, productType);
                if (!journalEntryList.isEmpty()) {
                    for (JournalEntry journalEntry : journalEntryList) {
                        journalEntry.setSalStatus("Completed");
                        journalEntryRepository.save(journalEntry);
                    }
                }
                return new ResponseEntity<>(new Status("200", "Salary Status Updated Sucessfully!!!"), HttpStatus.OK);

            case "QC_PROCESS":
                return null;
            default:
                throw new IllegalArgumentException("Invalid selectedTransaction value: " + type);
        }
    }


    public ResponseEntity<?> saveCWagesSalary(CWagesSalaryReqDTO cWagesSalaryReqDTO, User loggedUser) {

        String trnNo = null;
        Optional<TRNIndex> trnIndex = trnIndexRepo.findByPrefix("CWAGES");
        if (trnIndex.isPresent()) {
            TRNIndex trnIndex1 = trnIndex.get();
            trnNo = trnIndex1.getPrefix() + "-" + trnIndex1.getVouno();
            Long vouno = trnIndex1.getVouno() + 1;
            trnIndex1.setVouno(vouno);
            trnIndexRepo.save(trnIndex1);
        }

        CwagesSUM cwagesSUM = new CwagesSUM(cWagesSalaryReqDTO);
        if (cwagesSUM.getDept() == null) {
            cwagesSUM.setDept(loggedUser.getDepartment().getId());
        }
        if (cwagesSUM.getBranch() == null) {
            cwagesSUM.setBranch(loggedUser.getBranch_id());
        }
        cwagesSUM.setTrnno(trnNo);
        cwagesSUM.setCby(loggedUser.getId());
        cwagesSUM.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        CwagesSUM result = cwagesSUMRepository.save(cwagesSUM);

        System.out.println("CwagesSUM = " + result.getId());

        if (!cWagesSalaryReqDTO.getCwagesDetailsReqDTOList().isEmpty()) {
            for (CwagesDetailsReqDTO cwagesDetailsReqDTO : cWagesSalaryReqDTO.getCwagesDetailsReqDTOList()) {

                CwagesDetails cwagesDetails = new CwagesDetails(result, cwagesDetailsReqDTO);

                CwagesDetails rs = cwagesDetailsRepository.save(cwagesDetails);
                System.out.println("CwagesDetails = " + rs.getId());
            }
        }
        if (!cWagesSalaryReqDTO.getEmployeeDTOList().isEmpty()) {
            for (CWagesSalaryEmployeeDTO employeeDTO : cWagesSalaryReqDTO.getEmployeeDTOList()) {

                CwagesTRN cwagesTRN = new CwagesTRN(result, cWagesSalaryReqDTO, employeeDTO);

                CwagesTRN rs = cwagesTRNRepository.save(cwagesTRN);
                System.out.println("CwagesTRN = " + rs.getId());
                updatesDutyAllotment(rs);
            }
        }
        updateSalStatus(result, cWagesSalaryReqDTO);
        return new ResponseEntity<Status>(new Status("200", "cwages salary saved successfully!!!"), HttpStatus.OK);
    }

    public void updatesDutyAllotment(CwagesTRN rs) {

        Optional<AssignDuty> assignDuty = assignDutyRepository.findByAssignDuty(rs.getEmpcode(), 1, rs.getShiftdate());
        if (assignDuty.isPresent()) {
            System.out.println("EMPLOYEE  " + assignDuty.get().getStaff_id());
            AssignDuty assignDuty1 = assignDuty.get();
            assignDuty1.setContract("Y");
            assignDuty1.setStatus("C");
            assignDutyRepository.save(assignDuty1);
        }
    }

    public ResponseEntity<?> cWagesSalaryList(User loggedUser, Long branchId, Long dept, String shiftName, Long shiftId,
                                              String shiftDate, Pageable pageable, String trnType, String trnName, String trnNo) {

        Date shiftDateCon = null;

        if (shiftDate != null) {
            shiftDateCon = dateUtil.getStringTODateWk(shiftDate);
        }

        Page<DWagesSalaryList> list = cwagesSUMRepository.findBy(branchId, dept, shiftName, shiftId, shiftDateCon,
                trnNo, trnName, trnType, pageable);
        if (!list.isEmpty()) {
            List<DWagesSalaryListDto> result = list.toList().stream().map(res -> {
                DWagesSalaryListDto dWagesSalaryListDto = new DWagesSalaryListDto(res);
                dWagesSalaryListDto
                        .setStatus(getStatus(dWagesSalaryListDto.getBranchId(), dWagesSalaryListDto.getDeptId(),
                                dWagesSalaryListDto.getPshiftId(), dWagesSalaryListDto.getShiftDate()));
                return dWagesSalaryListDto;
            }).collect(Collectors.toList());
            PageDataDto<DWagesSalaryListDto> pageData = new PageDataDto(result, list);
            return new ResponseEntity<PageDataDto<DWagesSalaryListDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<DWagesSalarySummaryDTO>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public String getStatus(Long branchId, Long deptId, Long pShiftId, String shiftDate) {
        String status = "NotVerfied";
        Optional<DaySalary> daySalary = daySalaryRepo.findBy(branchId, deptId, pShiftId,
                dateUtil.getStringTODateWk(shiftDate));
        if (daySalary.isPresent()) {
            status = "Verified";
        }
        return status;
    }

    public ResponseEntity<?> rejectCWagesSalaryAllocation(User loggedUser, Long branchId, Long dept, String trnNo,
                                                          String refTrnNo, TransactionType transactionType) {

        Optional<CwagesSUM> cwagesSUMOptional = cwagesSUMRepository.findBy(branchId, dept, trnNo);
        if (cwagesSUMOptional.isPresent()) {
            CwagesSUM cwagesSUM = cwagesSUMOptional.get();
            refTrnNo = cwagesSUM.getReftrnno();
            List<CwagesDetails> cwagesDetailsList = cwagesDetailsRepository.findBy(cwagesSUM.getId(), cwagesSUM.getTrnno());
            if (!cwagesDetailsList.isEmpty()) {
                cwagesDetailsRepository.deleteAll(cwagesDetailsList);
            }
            System.out.println("trnId = " + cwagesSUM.getId() + " trnNo = " + cwagesSUM.getTrnno());
            List<CwagesTRN> trnList = cwagesTRNRepository.findBy(cwagesSUM.getId(), cwagesSUM.getTrnno());
            if (!trnList.isEmpty()) {
                for (CwagesTRN cwagesTRN : trnList) {
                    System.out.println("empcode = " + cwagesTRN.getEmpcode() + " shiftDate = " + cwagesSUM.getShiftdate());
                    Optional<AssignDuty> optional = assignDutyRepository.findByAssignDuty(cwagesTRN.getEmpcode(), 1, cwagesSUM.getShiftdate());
                    if (optional.isPresent()) {
                        AssignDuty assignDuty = optional.get();
                        assignDuty.setContract(null);
                        assignDuty.setStatus(null);
                        assignDutyRepository.save(assignDuty);
                    }
                }
                cwagesTRNRepository.deleteAll(trnList);
            }
            cwagesSUMRepository.delete(cwagesSUM);
            changeNullSalStatus(refTrnNo, cwagesSUM, transactionType);
            return new ResponseEntity<Status>(new Status("200", "CWages Salary Allocation Rejected Successfully!!!"), HttpStatus.OK);
        } else {
            return new ResponseEntity<Status>(
                    new Status("400", "CWages Salary Allocation Rejected Not Successfully!!!"), HttpStatus.OK);
        }
    }

    public void changeNullSalStatus(String trnNo, CwagesSUM cwagesSUM, TransactionType transactionType) {
        Long productId = cwagesSUM.getRefid();
        String type = transactionType.name();
        switch (type) {
            case "INTRA":
                Optional<WithInBranchReceive> withInBranchReceive = withInBranchReceiveRepository.findByTrnNo(trnNo);
                WithInBranchReceive mwr = withInBranchReceive.get();
                List<WithInBranchReceiveItems> materialItemList = withInBranchReceiveItemsRepository.findMaterialItemList(mwr.getId(), cwagesSUM.getRefid());
                if (!materialItemList.isEmpty()) {
                    for (WithInBranchReceiveItems materialWithInBranchReceiptItem : materialItemList) {
                        materialWithInBranchReceiptItem.setSalStatus(null);
                        withInBranchReceiveItemsRepository.save(materialWithInBranchReceiptItem);
                    }
                }
                break;
            case "INTER_IN":
                Optional<OtherBranchReceipt> otherBranchReceipt = otherBranchReceiptRepository.findByTrnNo(trnNo);
                OtherBranchReceipt obr = otherBranchReceipt.get();
                List<BinStockLog> binStockLogList = binStockLogRepository.getMRNTransactionList(productId, trnNo, "R", cwagesSUM.getLot(), "MATERIAL_TRANSACTION");
                if (!binStockLogList.isEmpty()) {
                    for (BinStockLog binStockLog : binStockLogList) {
                        binStockLog.setSalStatus(null);
                        binStockLogRepository.save(binStockLog);
                    }
                }
                break;

            case "INTER_OUT":
                Optional<MaterialIssue> materialIssue = materialIssueRepository.findByIssue(trnNo);
                MaterialIssue mi = materialIssue.get();
                List<BinStockLog> issueInterbinStockLogList = binStockLogRepository.getMRNTransactionList(productId, trnNo, "I", cwagesSUM.getLot(), "MATERIAL_TRANSACTION");
                if (!issueInterbinStockLogList.isEmpty()) {
                    for (BinStockLog binStockLog : issueInterbinStockLogList) {
                        binStockLog.setSalStatus(null);
                        binStockLogRepository.save(binStockLog);
                    }
                }
                break;

            case "STK_IN":
                List<BinStockLog> binstock = binStockLogRepository.getTransactionList(productId, trnNo, cwagesSUM.getLot());
                if (binstock.isEmpty()) {
                    for (BinStockLog binStockLog : binstock) {
                        binStockLog.setSalStatus(null);
                        binStockLogRepository.save(binStockLog);
                    }
                }
                break;

            case "STK_OUT":
                Optional<MaterialIssue> stockOut = materialIssueRepository.findByIssue(trnNo);
                MaterialIssue stock = stockOut.get();
                List<BinStockLog> issuebinStockLogList = binStockLogRepository.getMRNTransactionList(productId, trnNo, "I", cwagesSUM.getLot(), "MATERIAL_TRANSACTION");
                if (!issuebinStockLogList.isEmpty()) {
                    for (BinStockLog binStockLog : issuebinStockLogList) {
                        binStockLog.setSalStatus(null);
                        binStockLogRepository.save(binStockLog);
                    }
                }
                break;
            case "JOURNAL":
                Optional<Wcshift> wcShift = wcShiftsRepository.findByTrnNo(trnNo);
                Wcshift wc = wcShift.get();
                List<JournalEntry> journalEntryList = journalEntryRepository.getJournalEntryTransaction(wc.getJournalId(), productId, trnNo, cwagesSUM.getType());
                if (!journalEntryList.isEmpty()) {
                    for (JournalEntry journalEntry : journalEntryList) {
                        journalEntry.setSalStatus(null);
                        journalEntryRepository.save(journalEntry);
                    }
                }
                break;

            case "EXT_IN":
                Optional<GoDownExternal> goDownExternalIn = goDownExternalRepository.findByTrnNo(trnNo);
                if (goDownExternalIn.isPresent()) {
                    List<BinStockLog> binStockLogs = binStockLogRepository.getMRNTransactionList(productId, trnNo, "R", cwagesSUM.getLot(), "EXTERNAL_GODOWN");
                    if (binStockLogs.isEmpty()) {
                        for (BinStockLog binStockLog : binStockLogs) {
                            binStockLog.setSalStatus(null);
                            binStockLogRepository.save(binStockLog);
                        }
                    }
                }
                break;
            case "EXT_OUT":
                Optional<GoDownExternal> goDownExternal = goDownExternalRepository.findByTrnNo(trnNo);
                if (goDownExternal.isPresent()) {
                    List<RequestGodownItem> requestGodownItems = godownItemRepository.findBy(goDownExternal.get().getId(), productId, String.valueOf(cwagesSUM.getLot().intValue()));
                    if (requestGodownItems.isEmpty()) {
                        for (RequestGodownItem requestGodownItem : requestGodownItems) {
                            requestGodownItem.setSalStatus(null);
                            godownItemRepository.save(requestGodownItem);
                        }
                    }
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid selectedTransaction value: " + type);
        }
    }


    public ResponseEntity<?> getWagesSummary(User loggedUser, Long branchId, Long dept, String shiftName, Long shiftId,
                                             String shiftDate, String shiftType) {

        Date shiftDateCon = null;

        if (shiftDate != null) {
            shiftDateCon = dateUtil.getStringTODateWk(shiftDate);
        }

        System.out.println("branchId ==" + branchId + " Dept == " + dept + " ShiftName == " + shiftName + "shiftDate == " + shiftDate);

        List<DWagesSalarySummary> list = dutyAllotmentRepository.findBy(branchId, dept, shiftName, shiftId, shiftDateCon, shiftType);
        if (!list.isEmpty()) {
            List<DWagesSalarySummaryDTO> result = new ArrayList<>();

            for (DWagesSalarySummary sal : list) {

                DWagesSalarySummaryDTO salarySummaryDTO = new DWagesSalarySummaryDTO(sal);

                if (salarySummaryDTO.getEmp_type() == 1) {
                    Optional<EmastDaily> emastDaily = emastDailyRepository.findById(salarySummaryDTO.getEmpId());
                    if (emastDaily.isPresent()) {
                        salarySummaryDTO.setEmployeeName(emastDaily.get().getName());
                        salarySummaryDTO.setDaCatId(emastDaily.get().getDacatId());
                        if (emastDaily.get().getJoiningDate() != null) {
                            salarySummaryDTO
                                    .setJoiningDate(dateUtil.getDateTOStringWk(emastDaily.get().getJoiningDate()));
                            Long expYears = dateUtil.getDiffYears(emastDaily.get().getJoiningDate(),
                                    dateUtil.getISTDate());
                            salarySummaryDTO.setExperience(expYears.toString());
                        } else {
                            EpayMasterNew epayMasterNew = epayMasterNewRepository
                                    .findByWages(salarySummaryDTO.getEmpId());
                            if (epayMasterNew != null) {
                                salarySummaryDTO
                                        .setJoiningDate(dateUtil.getDateTOStringWk(emastDaily.get().getJoiningDate()));
                                Long expYears = dateUtil.getDiffYears(emastDaily.get().getJoiningDate(),
                                        dateUtil.getISTDate());
                                salarySummaryDTO.setExperience(expYears.toString());
                            }
                        }

                        if (emastDaily.get().getGender() != null) {
                            if (emastDaily.get().getGender().equalsIgnoreCase("FEMALE")) {
                                salarySummaryDTO.setSex("F");
                            } else if (emastDaily.get().getGender().equalsIgnoreCase("MALE")) {
                                salarySummaryDTO.setSex("M");
                            }
                        }
                        Optional<AttendanceTimelineList> findAttendanceDetail = wagesAttendanceRepository.findAttendanceDetail(salarySummaryDTO.getEmpId(),shiftDateCon);
                        if(findAttendanceDetail.isPresent()) {
                        	salarySummaryDTO.setCheckIn(findAttendanceDetail.get().getCheckIn());
                        	salarySummaryDTO.setCheckOut(findAttendanceDetail.get().getCheckOut());
                        	salarySummaryDTO.setTotalworkingdays(findAttendanceDetail.get().getTotalworkingdays());
                        }
                    }
                } else {
                    Optional<User> employee = userRepository.findById(salarySummaryDTO.getEmpId());
                    if (employee.isPresent()) {
                        salarySummaryDTO.setEmployeeName(employee.get().getEmployeename());
                        salarySummaryDTO.setDaCatId(employee.get().getDacatid());
                        if (employee.get().getJoiningDate() != null) {
                            salarySummaryDTO
                                    .setJoiningDate(dateUtil.getDateTOStringWk(employee.get().getJoiningDate()));
                            Long expYears = dateUtil.getDiffYears(employee.get().getJoiningDate(),
                                    dateUtil.getISTDate());
                            salarySummaryDTO.setExperience(expYears.toString());
                        }

                        if (employee.get().getGender() != null) {
                            if (employee.get().getGender().equalsIgnoreCase("FEMALE")) {
                                salarySummaryDTO.setSex("F");
                            } else if (employee.get().getGender().equalsIgnoreCase("MALE")) {
                                salarySummaryDTO.setSex("M");
                            }
                        }
                        Optional<AttendanceTimelineList> findAttendanceDetail = attendanceRepository.findAttendanceDetail(salarySummaryDTO.getEmpId(),shiftDateCon);
                        if(findAttendanceDetail.isPresent()) {
                        	salarySummaryDTO.setCheckIn(findAttendanceDetail.get().getCheckIn());
                        	salarySummaryDTO.setCheckOut(findAttendanceDetail.get().getCheckOut());
                        	salarySummaryDTO.setTotalworkingdays(findAttendanceDetail.get().getTotalworkingdays());
                        }
                    }
                }

                WagesSal wagesSal = null;
                System.out.println("luzone == " + salarySummaryDTO.getLuzone());
                if (salarySummaryDTO.getSex().equalsIgnoreCase("M")) {
                    wagesSal = luZoneRepository.findByMale(salarySummaryDTO.getLuzone());
                } else if (salarySummaryDTO.getSex().equalsIgnoreCase("F")) {
                    wagesSal = luZoneRepository.findByFeMale(salarySummaryDTO.getLuzone());
                }
                if (wagesSal != null) {
                    salarySummaryDTO.setDesignationId(wagesSal.getDesignationId());
                    salarySummaryDTO.setDesignationName(wagesSal.getDesignation());
                }

                double basic = 0.0;
                double da = 0.0;
                double allowance = 0.0;

                if (salarySummaryDTO.getContract() != null && salarySummaryDTO.getContract().equalsIgnoreCase("Y")) {

                    Date conshiftDate = dateUtil.getStringTODateWk(salarySummaryDTO.getShiftDate());

                    System.out.println("employee ID = " + salarySummaryDTO.getEmpId() + "conshiftDate ID = " + conshiftDate + "ShiftId ID = " + salarySummaryDTO.getPShiftId());

                    CwagesTRNSumList cwagesTRN = cwagesTRNRepository.findBy(salarySummaryDTO.getEmpId(), salarySummaryDTO.getPShiftId(), conshiftDate);
                    if (cwagesTRN != null) 
                    { 	wagesSal  = luZoneRepository.findByCName("Expeller_Attender");
                    if (wagesSal!=null) {                    	
                         basic = wagesSal.getBasic();
                         da = wagesSal.getDa();
                        if (salarySummaryDTO.getExperience() != null) {
                            basic = basic + Double.valueOf(salarySummaryDTO.getExperience());
                        }
                        double total = cwagesTRN.getAmount();
                         allowance = total - (basic + da);
                        salarySummaryDTO.setBasic(decfor.format(basic));
                        salarySummaryDTO.setDa(decfor.format(da));
                        if (allowance > 0) {
                            salarySummaryDTO.setAllowance(decfor.format(allowance));
                        } else {
                            salarySummaryDTO.setAllowance(decfor.format(0.0));
                        }                        
                        salarySummaryDTO.setTotal(decfor.format(total));
                    }
                    } else {
                        System.out.println("cwagesTRN  is NOT presented ");
                    }

                } else {

                    if (wagesSal != null) {
                        basic = wagesSal.getBasic();
                        da = wagesSal.getDa();
                        allowance = wagesSal.getAllowance();
                        logger.info("basic == " + basic + " da == " + da + " allowance == " + allowance);
                        if (salarySummaryDTO.getExperience() != null) {

                            double fBasic = basic + Double.valueOf(salarySummaryDTO.getExperience());
                            salarySummaryDTO.setBasic(decfor.format(fBasic));
                            double fAllowance = allowance - Double.valueOf(salarySummaryDTO.getExperience());
                            if (fAllowance > 0) {
                                salarySummaryDTO.setAllowance(decfor.format(fAllowance));
                            } else {
                                salarySummaryDTO.setAllowance(decfor.format(0.0));
                            }
                        } else {
                            salarySummaryDTO.setBasic(decfor.format(basic));
                            salarySummaryDTO.setAllowance(decfor.format(allowance));
                        }
                        //salarySummaryDTO.setBasic(decfor.format(basic));
                        //salarySummaryDTO.setAllowance(decfor.format(allowance));

                        salarySummaryDTO.setDa(decfor.format(wagesSal.getDa()));

                    }
                    double total = basic + da + allowance;

                    salarySummaryDTO.setTotal(decfor.format(total));
                }
                salarySummaryDTO.setWages(salarySummaryDTO.getTotal());


                result.add(salarySummaryDTO);
            }

            return new ResponseEntity<List<DWagesSalarySummaryDTO>>(result, HttpStatus.OK);
        } else {
            System.out.println("empty");
            return new ResponseEntity<List<DWagesSalarySummaryDTO>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> checkVerifyWagesSummary(User loggedUser, String category, Long branchId, Long dept, Long shiftId, Long pShiftId, String shiftDate) {

        Optional<DaySalary> daySalaryOptional = null;
        if (category != null && category.equalsIgnoreCase("Wages")) {
            daySalaryOptional = daySalaryRepo.findByWage(branchId, dept, shiftId, pShiftId, dateUtil.getStringTODateWk(shiftDate));
        } else {
            daySalaryOptional = daySalaryRepo.findByWage(branchId, dept, shiftId, pShiftId, dateUtil.getStringTODateWk(shiftDate));
        }
        if (daySalaryOptional.isPresent()) {
            return new ResponseEntity<Status>(new Status("200", "DaySalary Verified successfully!!!"), HttpStatus.OK);
        } else {
            return new ResponseEntity<Status>(new Status("400", "DaySalary Not Verified!!!"), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> verifyWagesSummary(User loggedUser, List<DWagesSalarySummaryDTO> dWagesSalarySummaryDTOList) {

        if (!dWagesSalarySummaryDTOList.isEmpty()) {

            for (DWagesSalarySummaryDTO dWagesSalarySummaryDTO : dWagesSalarySummaryDTOList) {

                Optional<DaySalary> daySalaryOptional = daySalaryRepo.findBy(dWagesSalarySummaryDTO.getEmpId(),
                        dWagesSalarySummaryDTO.getFromBranch(), dWagesSalarySummaryDTO.getFromDept(),
                        dWagesSalarySummaryDTO.getShiftId(), dWagesSalarySummaryDTO.getPShiftId(),
                        dateUtil.getStringTODateWk(dWagesSalarySummaryDTO.getShiftDate()));

                DaySalary daySalary = null;

                if (daySalaryOptional.isPresent()) {
                    daySalary = new DaySalary(daySalaryOptional.get(), dWagesSalarySummaryDTO);
                } else {
                    daySalary = new DaySalary(dWagesSalarySummaryDTO);
                    daySalary.setCreatedBy(loggedUser.getId());
                    daySalary.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                }

                if (daySalary.getBasic() == 0.0) {
                    Optional<SalaryStructure> salaryStructure = salaryStructureRepo.findByDefult();
                    double basic = 0.0;
                    if (daySalary.getExperience() != null) {
                        //basic = salaryStructure.get().getMinbasic() + Double.valueOf(daySalary.getExperience());
                        basic = salaryStructure.get().getMinbasic();
                    }
                    double da = salaryStructure.get().getDa();
                    if (daySalary.getPer_day_total() > basic) {
                        double totba = daySalary.getPer_day_total() - basic;
                        daySalary.setBasic(Math.floor(basic));
                        double totDa = totba - da;
                        if (totDa > 0) {
                            daySalary.setDa(Math.floor(da));
                            daySalary.setAllowance(Math.floor(totDa));
                        } else {
                            daySalary.setDa(Math.floor(totba));
                        }
                    }
                }

                DaySalary rs = daySalaryRepo.save(daySalary);
                updatesAssignDutyStatus(rs);
            }
            return new ResponseEntity<Status>(new Status("200", "Wages DaySalary updated successfully!!!"),
                    HttpStatus.OK);
        } else {
            return new ResponseEntity<Status>(new Status("400", "Wages DaySalary Not updated!!!"), HttpStatus.OK);
        }
    }

    // Wages SalaryList

    public ResponseEntity<?> wagesSalaryList(String fromDate, String toDate, Long company, Long branch, Long dept,
                                             String pageFlag, Pageable pageable) throws UsernameNotFoundException {

        Date start = null;
        Date end = null;
        if (fromDate != null) {
            start = dateUtil.getStringTODateWk(fromDate);
        }

        if (toDate != null) {
            end = dateUtil.getStringTODateWk(toDate);
        }
        System.out.println("start == " + start);
        System.out.println("end == " + end);
        if (pageFlag != null && pageFlag.equalsIgnoreCase("Y")) {

            Page<WagesSalary> list = daySalaryRepo.findBy(start, end, branch, dept, pageable);
            if (!list.isEmpty()) {
                List<WagesSalaryDto> result = list.toList().stream().map(data -> {
                    WagesSalaryDto salaryDto = new WagesSalaryDto(data);
                    return salaryDto;
                }).collect(Collectors.toList());
                PageDataDto<WagesSalaryDto> pageData = new PageDataDto<>(result, list);
                return new ResponseEntity<PageDataDto<WagesSalaryDto>>(pageData, HttpStatus.OK);
            } else {
                return new ResponseEntity<PageDataDto<WagesSalaryDto>>(new PageDataDto(), HttpStatus.OK);
            }
        } else {
            List<WagesSalary> list = daySalaryRepo.findBy(start, end, branch, dept);
            if (!list.isEmpty()) {
                List<WagesSalaryDto> result = list.stream().map(data -> {
                    WagesSalaryDto salaryDto = new WagesSalaryDto(data);
                    return salaryDto;
                }).collect(Collectors.toList());
                return new ResponseEntity<List<WagesSalaryDto>>(result, HttpStatus.OK);
            } else {
                return new ResponseEntity<List<WagesSalaryDto>>(new ArrayList<>(), HttpStatus.OK);
            }
        }
    }

    // Wages Salary Transaction List

    public ResponseEntity<?> wagesSalaryTransactionList(Long company, Long branch, Long dept, String fromDate,
                                                        String toDate, Pageable pageable) throws UsernameNotFoundException {

        Date start = null;
        Date end = null;
        if (fromDate != null) {
            start = dateUtil.getStringTODateWk(fromDate);
        }

        if (toDate != null) {
            end = dateUtil.getStringTODateWk(toDate);
        }
        System.out.println("start == " + start);
        System.out.println("end == " + end);

        Page<WagesTransactionList> list = daySalaryRepo.findByList(branch, dept, start, end, pageable);
        if (!list.isEmpty()) {
            List<WagesTransactionListDTO> result = list.toList().stream().map(data -> {
                WagesTransactionListDTO salaryDto = new WagesTransactionListDTO(data);
                return salaryDto;
            }).collect(Collectors.toList());
            PageDataDto<WagesTransactionListDTO> pageData = new PageDataDto<>(result, list);
            return new ResponseEntity<PageDataDto<WagesTransactionListDTO>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<PageDataDto<WagesTransactionListDTO>>(new PageDataDto(), HttpStatus.OK);
        }

    }

    // Wages Salary Transaction Print List

    public ResponseEntity<?> wagesSalaryTransactionPrintList(Long company, Long branch, Long dept, String fromDate,
                                                             String toDate, Pageable pageable) throws UsernameNotFoundException {

        Date start = null;
        Date end = null;
        if (fromDate != null) {
            start = dateUtil.getStringTODateWk(fromDate);
        }

        if (toDate != null) {
            end = dateUtil.getStringTODateWk(toDate);
        }
        System.out.println("start == " + start);
        System.out.println("end == " + end);

        List<WagesSalary> list = daySalaryRepo.findByList(start, end, branch, dept);
        if (!list.isEmpty()) {
            List<WagesSalaryDto> result = list.stream().map(data -> {
                WagesSalaryDto salaryDto = new WagesSalaryDto(data);
                return salaryDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<WagesSalaryDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<WagesSalaryDto>>(new ArrayList<>(), HttpStatus.OK);
        }

    }

    // Wages Salary Transaction Print List

    public ResponseEntity<?> printWagesSalaryTransactionDetailsList(String trnNo, Long branch, Long dept, String shiftDate) throws UsernameNotFoundException {

        Date shiftDateCon = null;
        if (shiftDate != null) {
            shiftDateCon = dateUtil.getStringTODateWk(shiftDate);
        }
        PrintWagesSalaryResultDTO printWagesSalaryResult = new PrintWagesSalaryResultDTO();
        System.out.println("shiftDate == " + shiftDateCon);

        PrintWagesSalaryVoucher printWagesSalaryVoucher = cwagesSUMRepository.findBy(trnNo, shiftDateCon, branch, dept);
        if (printWagesSalaryVoucher != null) {
            PrintWagesSalaryVoucherDTO salaryVoucher = new PrintWagesSalaryVoucherDTO(printWagesSalaryVoucher);
            printWagesSalaryResult.setCwagesTransactionDetails(salaryVoucher);
            CwagesDet cwagesDet = cwagesDetailsRepository.findByDetail(printWagesSalaryVoucher.getId(), printWagesSalaryVoucher.getTrnNo());
            CwagesDetDTO cwagesDetDTO = new CwagesDetDTO(cwagesDet);

            if (cwagesDetDTO.getRefTable().equalsIgnoreCase("bills")) {

                List<String> list = Arrays.asList(cwagesDetDTO.getRefTrnNo().split("\\-"));
                System.out.println("bill no = " + list.get(1));
                Optional<BillInward> billInward = billInwardRepository.findById(Long.valueOf(list.get(1)));

                if (billInward.isPresent()) {
                    cwagesDetDTO.setBillDate(dateUtil.getDateTOStringWk(billInward.get().getReceivedDate()));
                    cwagesDetDTO.setLorryDate(dateUtil.getDateTOStringWk(billInward.get().getLorryDate()));
                    cwagesDetDTO.setLorrySerialNo(billInward.get().getLorrySerialNo());
                    cwagesDetDTO.setBillNo(billInward.get().getBillNo());
                    cwagesDetDTO.setVendorName(billInward.get().getVendor().getName());
                    cwagesDetDTO.setVoucherType("DP");
                }

            } else if (cwagesDetDTO.getRefTable().equalsIgnoreCase("wcshift")) {


                Optional<Wcshift> wcshift = wcShiftsRepository.findByTrnNo(cwagesDetDTO.getRefTrnNo());

                if (wcshift.isPresent()) {
                    Optional<Journal> journal = journalRepository.findById(wcshift.get().getJournalId());
                    if (journal.isPresent()) {
                        cwagesDetDTO.setJournalName(journal.get().getJournalName());
                        Optional<JournalMachineMapping> journalMachineMapping = journalMachineMappingRepository.findByJournalMachineMapping(journal.get().getId(), wcshift.get().getWcId());
                        if (journalMachineMapping.isPresent()) {
                            cwagesDetDTO.setWorkCenterName(journalMachineMapping.get().getMachineName());
                        }
                        List<WagesJournalWork> entry = journalEntryRepository.journalEntryList(cwagesDetDTO.getRefTrnNo());
                        if (!entry.isEmpty()) {
                            cwagesDetDTO.setBatchNo(entry.get(0).getBatchNo());
                        }
                    }
                    cwagesDetDTO.setVoucherType("PRODUCTION");
                }
            } else if (cwagesDetDTO.getRefTable().equalsIgnoreCase("mrntrn")) {
                cwagesDetDTO.setVoucherType("MATERIAL_TRANSACTION");
            } else if (cwagesDetDTO.getRefTable().equalsIgnoreCase("exttrn")) {
                cwagesDetDTO.setVoucherType("EXTERNAL_TRANSACTION");
            }

            List<CwagesDetailsList> cwagesDetailsLists = cwagesDetailsRepository.findDetailList(printWagesSalaryVoucher.getId(), printWagesSalaryVoucher.getTrnNo());
            if (!cwagesDetailsLists.isEmpty()) {
                List<CwagesDetailsListDTO> cwagesDetailsListDTOS = cwagesDetailsLists.stream().map(data -> new CwagesDetailsListDTO(data)).collect(Collectors.toList());
                cwagesDetDTO.setCwagesDetailsList(cwagesDetailsListDTOS);
            }
            printWagesSalaryResult.setCwagesWorkDetails(cwagesDetDTO);
            List<CwagesEmployees> cwagesEmployees = cwagesTRNRepository.findByCwagesEmployees(printWagesSalaryVoucher.getId(), printWagesSalaryVoucher.getTrnNo(), shiftDateCon);
            if (!cwagesEmployees.isEmpty()) {
                List<CwagesEmployeesDTO> cwagesEmployeesDTOList = cwagesEmployees.stream().map(data -> new CwagesEmployeesDTO(data)).collect(Collectors.toList());
                printWagesSalaryResult.setCwagesEmployeesList(cwagesEmployeesDTOList);
            }
            return new ResponseEntity<PrintWagesSalaryResultDTO>(printWagesSalaryResult, HttpStatus.OK);
        }

        return new ResponseEntity<PrintWagesSalaryResultDTO>(HttpStatus.NOT_FOUND);
    }


    public ResponseEntity<?> rejectWagesSummary(Long company, Long branch, Long dept, String shiftDate, Long shiftId,
                                                Long pShiftId, String shiftType, String category) {

        Date shiftConDate = null;
        if (shiftDate != null) {
            shiftConDate = dateUtil.getStringTODateWk(shiftDate);
        }
        Long daCatId = null;

        List<DaySalary> daySalaryList = null;

        if (category != null && category.equalsIgnoreCase("Wages")) {
            daySalaryList = daySalaryRepo.findByWages(branch, dept, shiftConDate, shiftId, pShiftId, shiftType);
        } else {
            daySalaryList = daySalaryRepo.findByWageApp(branch, dept, shiftConDate, shiftId, pShiftId, shiftType);
        }

        if (!daySalaryList.isEmpty()) {

            daySalaryRepo.deleteAll(daySalaryList);

            return new ResponseEntity<Status>(new Status("200", "Wages DaySalary Rejected successfully!!!"), HttpStatus.OK);
        } else {
            return new ResponseEntity<Status>(new Status("400", "Wages DaySalary Not Rejected!!!"), HttpStatus.OK);
        }
    }


    public void updatesAssignDutyStatus(DaySalary ds) {

        Optional<AssignDuty> assignDuty = assignDutyRepository.findByAssignDuty(ds.getEmpId(), 1, ds.getShift_date());
        if (assignDuty.isPresent()) {
            System.out.println("EMPLOYEE  " + assignDuty.get().getStaff_id());
            AssignDuty assignDuty1 = assignDuty.get();
            assignDuty1.setStatus("C");
            assignDutyRepository.save(assignDuty1);
        }
    }


    public ResponseEntity<?> getTrnProductDetails(String trnNo, TransactionType transactionType) {

        String type = transactionType.name();

        switch (type) {
            case "INTRA":
                return getwithInBranchItem(trnNo);
            case "INTER_IN":
                return getOtherBranchReceipt(trnNo);
            case "INTER_OUT":
                return getMrnStockOut(trnNo, "interout");
            case "STK_IN":
                return getBillInwardProduct(trnNo);
            case "STK_OUT":
                return getMrnStockOut(trnNo, "stkout");
            case "JOURNAL":
                return getJournalDetail(trnNo);
            case "EXT_IN":
                return getGodownReceiveItem(trnNo);
            case "EXT_OUT":
                return getGodownOUTItem(trnNo);
            default:
                throw new IllegalArgumentException("Invalid selectedTransaction value: " + type);
        }

    }


    public ResponseEntity<?> getBillInwardProduct(String trnNo) {
        List<WagesJournalWork> journalEntries = binStockLogRepository.inwardProductList(trnNo);
        List<WagesJournalWorkDTO> list = journalEntries.stream().map(entrydata ->
                {
                    WagesJournalWorkDTO workDTO = new WagesJournalWorkDTO(entrydata);
                    workDTO.setFinishedQty(cwagesSUMRepository.sumOfAllocatedQty(trnNo, workDTO.getProductId(), workDTO.getLot(), "stkin"));
                    return workDTO;
                }
        ).collect(Collectors.toList());
        return new ResponseEntity<List<WagesJournalWorkDTO>>(list, HttpStatus.OK);
    }


    public ResponseEntity<?> getMrnStockOut(String trnNo, String type) {
        List<WagesJournalWork> journalEntries = binStockLogRepository.mrnIssueProductList(trnNo);
        List<WagesJournalWorkDTO> list = journalEntries.stream().map(entrydata ->
                {
                    WagesJournalWorkDTO workDTO = new WagesJournalWorkDTO(entrydata);
                    workDTO.setFinishedQty(cwagesSUMRepository.sumOfAllocatedQty(workDTO.getTrnNo(), workDTO.getProductId(), workDTO.getLot(), type));
                    return workDTO;
                }
        ).collect(Collectors.toList());
        return new ResponseEntity<List<WagesJournalWorkDTO>>(list, HttpStatus.OK);
    }

    public ResponseEntity<?> getwithInBranchItem(String trnNo) {
        List<WagesJournalWork> journalEntries = withInBranchReceiveItemsRepository.mrnReceiptProductList(trnNo);
        List<WagesJournalWorkDTO> list = journalEntries.stream().map(entrydata ->
        {
            WagesJournalWorkDTO workDTO = new WagesJournalWorkDTO(entrydata);
            workDTO.setFinishedQty(cwagesSUMRepository.sumOfAllocatedQty(workDTO.getTrnNo(), workDTO.getProductId(), workDTO.getLot(), "intra"));
            return workDTO;
        }).collect(Collectors.toList());
        return new ResponseEntity<List<WagesJournalWorkDTO>>(list, HttpStatus.OK);
    }


    public ResponseEntity<?> getOtherBranchReceipt(String trnNo) {
        List<WagesJournalWork> journalEntries = binStockLogRepository.mrnOtherBranchReceiptProductList(trnNo);
        List<WagesJournalWorkDTO> list = journalEntries.stream().map(entrydata -> {
            WagesJournalWorkDTO workDTO = new WagesJournalWorkDTO(entrydata);
            workDTO.setFinishedQty(cwagesSUMRepository.sumOfAllocatedQty(workDTO.getTrnNo(), workDTO.getProductId(), workDTO.getLot(), "interin"));
            return workDTO;
        }).collect(Collectors.toList());
        return new ResponseEntity<List<WagesJournalWorkDTO>>(list, HttpStatus.OK);
    }

    public ResponseEntity<?> getGodownReceiveItem(String trnNo) {
        List<WagesJournalWork> journalEntries = binStockLogRepository.getGodownReceiveItem(trnNo);
        List<WagesJournalWorkDTO> list = journalEntries.stream().map(entrydata -> {
            WagesJournalWorkDTO workDTO = new WagesJournalWorkDTO(entrydata);
            workDTO.setFinishedQty(cwagesSUMRepository.sumOfAllocatedQty(workDTO.getTrnNo(), workDTO.getProductId(), workDTO.getLot(), "extin"));
            return workDTO;
        }).collect(Collectors.toList());
        return new ResponseEntity<List<WagesJournalWorkDTO>>(list, HttpStatus.OK);
    }


    public ResponseEntity<?> getGodownOUTItem(String trnNo) {
        List<WagesJournalWork> journalEntries = godownItemRepository.getGodownItem(trnNo);
        List<WagesJournalWorkDTO> list = journalEntries.stream().map(entrydata -> {
            WagesJournalWorkDTO workDTO = new WagesJournalWorkDTO(entrydata);
            workDTO.setFinishedQty(cwagesSUMRepository.sumOfAllocatedQty(workDTO.getTrnNo(), workDTO.getProductId(), workDTO.getLot(), "extout"));
            return workDTO;
        }).collect(Collectors.toList());
        return new ResponseEntity<List<WagesJournalWorkDTO>>(list, HttpStatus.OK);
    }
}