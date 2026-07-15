package com.aria.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.utils.DateUtil;
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

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;


@Service
public class DayEndClosingService {

    public static final Logger logger = LoggerFactory.getLogger(DayEndClosingService.class);

    private static final DecimalFormat decfor = new DecimalFormat("0.00");

    @Autowired
    DateUtil dateUtil;

    @Autowired
    UserRepository userRepository;

    @Autowired
    public BinStockRepository binStockRepository;

    @Autowired
    public BinStatusRepository binStatusRepository;

    @Autowired
    public PShiftsRepository pShiftsRepository;

    @Autowired
    public BranchRepository branchRepository;

    @Autowired
    public DepartmentDetailsRepository departmentRepository;

    @Autowired
    public BRMProductsRepository brmProductsRepository;

    @Autowired
    public PhysicalStockRepository physicalStockRepository;

    @Autowired
    public JournalEntryRepository journalEntryRepository;

    @Autowired
    public DayEndStockClosingSummaryRepository dayEndStockClosingSummaryRepository;

    @Autowired
    public ProductionEntryRepository productionEntryRepository;

    @Autowired
    public MaterialRequestRepository materialRequestRepository;

    @Autowired
    public BinstockAuditLogRepository binstockAuditLogRepository;

    @Autowired
    MachineRepository machineRepository;

    @Autowired
    DwClusterRepository dwClusterRepository;

    @Autowired
    GoDownRepository goDownRepository;

    @Autowired
    ProductMasterRepository productMasterRepository;

    @Autowired
    JournalProductMappingRepository journalProductMappingRepository;

    @Autowired
    BinStockLogRepository binStockLogRepository;

    @Autowired
    BillInwardRepository billInwardRepository;

    @Autowired
    DayEndClosingBranchGroupRepository dayEndClosingBranchGroupRepository;


    public ResponseEntity<?> physicalStockList(User loggedUser) throws UsernameNotFoundException {

        // updatePhysicalStock(dateUtil.getISTDate(), loggedUser);
        List<PhysicalStockList> list = physicalStockRepository.findAll(loggedUser.getBranch_id(), dateUtil.getISTDate());
        if (!list.isEmpty()) {
            List<PhysicalStockDto> result = list.stream().map(data -> {
                PhysicalStockDto stockPaperDto = new PhysicalStockDto(data);
                return stockPaperDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<PhysicalStockDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<PhysicalStockDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

/*
    private void updatePhysicalStock(Date accountingDate, List<Long> branchList, User loggedUser) {


        List<PhysicalStockItems> physicalStockItems = binStockRepository.getPhysicalStockItems(branchList);

        if (!physicalStockItems.isEmpty()) {

            List<PhysicalStock> physicalStockList = physicalStockItems.stream().map(data -> {

                Optional<PhysicalStock> optional = physicalStockRepository.findBY(data.getRefid(), data.getBranch(), data.getDept(), data.getBinid(), data.getBinname(), data.getLot(), accountingDate);

                PhysicalStock physicalStock = null;
                if (optional.isPresent()) {
                    physicalStock = optional.get();
                    physicalStock.setQty(data.getQty());
                    physicalStock.setAdate(accountingDate);
                } else {
                    physicalStock = new PhysicalStock(data);
                    physicalStock.setAdate(accountingDate);
                }
                physicalStock.setAby(loggedUser.getId());
                return physicalStock;
            }).collect(Collectors.toList());
            physicalStockRepository.saveAll(physicalStockList);
        }

    }*/

    @Transactional
    private void updatePhysicalStock(Date accountingDate, List<Long> branchList, User loggedUser) {

        List<PhysicalStockItems> stockItems = binStockRepository.getPhysicalStockItems(branchList);

        if (stockItems.isEmpty()) {
            return;
        }

        Long userId = loggedUser.getId();

        // 🔹 Fetch existing physical stock in ONE query
        List<PhysicalStock> existingStocks =
                physicalStockRepository.findByBranchInAndAdate(branchList, accountingDate);

        Map<String, PhysicalStock> stockMap = existingStocks.stream()
                .collect(Collectors.toMap(
                        this::buildKey,
                        ps -> ps
                ));

        List<PhysicalStock> toSave = new ArrayList<>(stockItems.size());

        for (PhysicalStockItems item : stockItems) {

            String key = buildKey(item);

            PhysicalStock stock = stockMap.get(key);

            if (stock == null) {
                stock = new PhysicalStock(item);
                stock.setAdate(accountingDate);
            } else {
                stock.setQty(item.getQty());
            }

            stock.setAby(userId);
            toSave.add(stock);
        }

        physicalStockRepository.saveAll(toSave);
    }


    private String buildKey(PhysicalStock ps) {
        return ps.getRefid() + "|" +
                ps.getBid() + "|" +
                ps.getDid() + "|" +
                ps.getBinid() + "|" +
                ps.getBinname() + "|" +
                ps.getLot();
    }

    private String buildKey(PhysicalStockItems item) {
        return item.getRefid() + "|" +
                item.getBranch() + "|" +
                item.getDept() + "|" +
                item.getBinid() + "|" +
                item.getBinname() + "|" +
                item.getLot();
    }


    public ResponseEntity<?> savePhysicalStock(List<PhysicalStockDto> physicalStockDtoList, User loggedUser) throws UsernameNotFoundException {

        if (!physicalStockDtoList.isEmpty()) {
            List<PhysicalStock> physicalStockList = physicalStockDtoList.stream().map(data -> {
                PhysicalStock ps = new PhysicalStock(data);
                ps.setAby(loggedUser.getId());
                return ps;
            }).collect(Collectors.toList());
            physicalStockRepository.saveAll(physicalStockList);
            return new ResponseEntity<Status>(new Status("200", "PhysicalStock saved successfully!!!"), HttpStatus.OK);
        } else {
            return new ResponseEntity<Status>(new Status("400", "PhysicalStock  not saved!!!"), HttpStatus.OK);
        }
    }


    public ResponseEntity<?> closingStockDetails(Long branch) throws UsernameNotFoundException {

        Date currentDate = dateUtil.getISTDate();

        Optional<DayEndStockClosingSummary> optional = dayEndStockClosingSummaryRepository.findByDateAndBranch(branch, currentDate);
        if (optional.isPresent()) {
            return new ResponseEntity<Status>(new Status("201", "DayEndClosing already Updated!!!"), HttpStatus.OK);
        }

        List<DayEndStockList> list = brmProductsRepository.findBY(branch);

        System.out.println(branch);

        List<ProductStockList> actualStock = binStockRepository.getActualStock(branch);
        if (actualStock.isEmpty()) {
            return new ResponseEntity<Status>(new Status("400", "PhysicalStock Not Updated!!!"), HttpStatus.OK);
        }
        System.out.println(actualStock);
        Map<Long, Double> actualStockMap = actualStock.stream().collect(Collectors.toMap(tuple -> tuple.getItemId(), tuple -> tuple.getQty()));
        System.out.println("actualStockMap == " + actualStockMap);
        List<ProductStockList> physicalStock = physicalStockRepository.getPhysicalStock(branch, dateUtil.getISTDate());
        if (physicalStock.isEmpty()) {
            return new ResponseEntity<Status>(new Status("400", "PhysicalStock Not Updated!!!"), HttpStatus.OK);
        }
        Map<Long, Double> physicalStockMap = physicalStock.stream().collect(Collectors.toMap(tuple -> tuple.getItemId(), tuple -> tuple.getQty()));

        if (!list.isEmpty()) {
            List<DayEndStockClosingSummaryDto> result = list.stream().map(data -> {
                DayEndStockClosingSummaryDto endStockDto = new DayEndStockClosingSummaryDto(data);
                Long itemId = endStockDto.getItemId();
                if (actualStockMap.containsKey(itemId)) {
                    endStockDto.setBinStock(actualStockMap.get(itemId));
                }
                if (physicalStockMap.containsKey(itemId)) {
                    endStockDto.setPhysicalStock(physicalStockMap.get(itemId));
                }
                return endStockDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<DayEndStockClosingSummaryDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<DayEndStockClosingSummaryDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> updateDayEndClosingStock(List<DayEndStockClosingSummaryDto> closingSummaryDtoList, User loggedUser) throws UsernameNotFoundException {

        if (closingSummaryDtoList == null || closingSummaryDtoList.isEmpty()) {
            return ResponseEntity.ok(new Status("400", "Day end not closed!!!"));
        }
     
        try {
            DayEndStockClosingSummaryDto firstDto = closingSummaryDtoList.get(0);

            Long branch = firstDto.getBranchId() != null
                    ? firstDto.getBranchId()
                    : loggedUser.getBranch_id();
            LocalDate accDate = DateUtil.stringToLocalDate(firstDto.getAccountingDate());
            LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata")); 
           
            if (accDate.isAfter(today))
            {  return new ResponseEntity<>(
	                new Status("400", "Accounting date cannot be future date"),
	                HttpStatus.BAD_REQUEST);
 }
            Date accountingDate = resolveAccountingDate(firstDto.getAccountingDate());
           

            ObjectMapper objectMapper = new ObjectMapper();

            for (DayEndStockClosingSummaryDto dto : closingSummaryDtoList) {

                DayEndStockClosingSummary entity =
                        dayEndStockClosingSummaryRepository
                                .findByDateAndBranchAndItem(
                                        dto.getBranchId(),
                                        dto.getItemId(),
                                        accountingDate
                                )
                                .orElseGet(() -> new DayEndStockClosingSummary(dto));

                entity.setTodayClosing(dto.getTodayClosing());
                entity.setYieldSummaryValue(objectMapper.writeValueAsString(dto.getYieldSummaryValue()));
                entity.setCreatedBy(loggedUser.getId());
                entity.setAdate(accountingDate);
                entity.setCreated_date(dateUtil.getDateToLocalDateTime(accountingDate));
                dayEndStockClosingSummaryRepository.save(entity);
            }

            List<Long> branchList = new ArrayList<>();
            branchList.add(branch);

            List<Long> childBranchList = dayEndClosingBranchGroupRepository.findChildBranchIdByBranchId(branch);
            if (childBranchList != null && !childBranchList.isEmpty()) {
                logger.info("Child Branch List for Day End Closing: {}", childBranchList);
                branchList.addAll(childBranchList);
            }
            logger.info("Branch List for Day End Closing: {}", branchList);

            updateAllProductClosingStock(loggedUser, branchList, accountingDate);
            updateLedger(loggedUser, branchList, accountingDate);
            updateStatus(branchList, accountingDate);
            updatePhysicalStock(accountingDate, branchList, loggedUser);

            return ResponseEntity.ok(
                    new Status("200", "Day end closed successfully!!!")
            );

        } catch (Exception e) {
            logger.error("Error in updateDayEndClosingStock", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Status("500", "Internal server error occurred."));
        }
    }

    private Date resolveAccountingDate(String accountingDateStr) {

        try {
            Date accountingDate = dateUtil.getStringTODateWk(accountingDateStr);

            Date today = dateUtil.getISTDate();

            if (today.equals(accountingDate)) {
                return today;
            }
         

            Calendar calendar = GregorianCalendar.getInstance();
            calendar.setTime(accountingDate);
            calendar.add(Calendar.HOUR, 23);
            calendar.add(Calendar.MINUTE, 59);

            return calendar.getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }


    public void updateStatus(List<Long> branchList, Date accountingDate) {

        if (branchList == null || branchList.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Kolkata"));

        dayEndStockClosingSummaryRepository
                .closeDayEnd(branchList, accountingDate, now);
    }



    /*public void updateAllProductClosingStock(User loggerUser, Long branch, Date accountingDateCon) throws UsernameNotFoundException {

        List<DayEndStockList> list = binStockRepository.findByClosingStockDetails(branch);
        List<DayEndStockClosingSummary> dayEndStockClosingSummaries = list.stream().map(data -> {
            DayEndStockClosingSummary endStockClosingSummary = new DayEndStockClosingSummary(data);
            endStockClosingSummary.setCreatedBy(loggerUser.getId());
            endStockClosingSummary.setAdate(accountingDateCon);
            endStockClosingSummary.setStatus("Pending");
            endStockClosingSummary.setCreated_date(dateUtil.getDateToLocalDateTime(accountingDateCon));
            return endStockClosingSummary;
        }).collect(Collectors.toList());
        dayEndStockClosingSummaryRepository.saveAll(dayEndStockClosingSummaries);
    }*/


    @Transactional
    public void updateAllProductClosingStock(User loggedUser, List<Long> branchList, Date accountingDate) throws UsernameNotFoundException {

        List<DayEndStockList> stockList = binStockRepository.findByClosingStockDetails(branchList);

        if (stockList.isEmpty()) {
            return;
        }
        LocalDateTime createdDate = dateUtil.getDateToLocalDateTime(accountingDate);
        Long userId = loggedUser.getId();
        List<DayEndStockClosingSummary> summaries = new ArrayList<>(stockList.size());
        for (DayEndStockList stock : stockList) {
            summaries.add(buildClosingSummary(
                    stock, userId, accountingDate, createdDate));
        }
        dayEndStockClosingSummaryRepository.saveAll(summaries);
    }


    private DayEndStockClosingSummary buildClosingSummary(DayEndStockList stock, Long userId, Date accountingDate, LocalDateTime createdDate) {
        DayEndStockClosingSummary summary = new DayEndStockClosingSummary(stock);
        summary.setCreatedBy(userId);
        summary.setAdate(accountingDate);
        summary.setStatus(StockStatus.PENDING.name());
        summary.setCreated_date(createdDate);
        return summary;
    }


    @Transactional
    public void updateLedger(
            User loggedUser,
            List<Long> branchList,
            Date accountingDate
    ) throws UsernameNotFoundException {

        List<DayEndClosingStockList> closingStocks =
                binStockRepository.getDayEndClosingStockItems(branchList);

        if (closingStocks.isEmpty()) {
            return;
        }

        Date nextDay = addDays(accountingDate, 1);

        List<BinstockAuditLog> auditLogs = new ArrayList<>(
                closingStocks.size() * 2
        );

        for (DayEndClosingStockList stock : closingStocks) {
            auditLogs.add(buildAuditLog(
                    stock, loggedUser, accountingDate,
                    LedgerNarration.CLOSING_STOCK,
                    LedgerEntryType.CLOSING
            ));

            auditLogs.add(buildAuditLog(
                    stock, loggedUser, nextDay,
                    LedgerNarration.OPENING_STOCK,
                    LedgerEntryType.OPENING
            ));
        }

        binstockAuditLogRepository.saveAll(auditLogs);
    }


    private BinstockAuditLog buildAuditLog(
            DayEndClosingStockList stock,
            User user,
            Date accountingDate,
            LedgerNarration narration,
            LedgerEntryType entryType
    ) {
        BinstockAuditLog log = new BinstockAuditLog();
        log.setItem_id(stock.getRefid());
        log.setQty(stock.getQty());
        log.setLot(stock.getLot());
        log.setNaration(narration.getText());
        log.setEntry_type(entryType.name());
        log.setTrn_type(TrnType.REVERSAL.code());
        log.setAccouting_date(accountingDate);
        log.setUpdate_on(accountingDate);
        log.setBranch_id(stock.getBranch());
        log.setFirm_id(user.getOrg_id());
        log.setBalance(stock.getQty());
        log.setBinNo(stock.getBinname());
        log.setGodownId(stock.getGid());
        return log;
    }


    private Date addDays(Date date, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, days);
        return cal.getTime();
    }













/*
    public void updateLedger(User loggedUser, List<Long> branchList, Date accountingDateCon) throws UsernameNotFoundException {
        Date currentDate = accountingDateCon;
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(accountingDateCon);
        calendar.add(Calendar.DATE, 1);
        Date tomorrowDayDate = calendar.getTime();
        List<DayEndClosingStockList> list = binStockRepository.getDayEndClosingStockItems(branchList);
        for (DayEndClosingStockList closingSummary : list) {
            updateLedger(closingSummary,  loggedUser, currentDate, "CLOSING STOCK", "CLOSING");
            updateLedger(closingSummary,  loggedUser, tomorrowDayDate, "OPENING STOCK", "OPENING");
        }
    }

    public void updateLedger(DayEndClosingStockList closingSummary, User loggedUser, Date currentDate, String narration, String type) {
        BinstockAuditLog binStockLog = new BinstockAuditLog();
        binStockLog.setItem_id(closingSummary.getRefid());
        binStockLog.setQty(closingSummary.getQty());
        binStockLog.setLot(closingSummary.getLot());
        binStockLog.setNaration(narration);
        binStockLog.setEntry_type(type);
        binStockLog.setTrn_type("R");
        binStockLog.setAccouting_date(currentDate);
        binStockLog.setUpdate_on(currentDate);
        binStockLog.setBranch_id(closingSummary.getBranch());
        binStockLog.setFirm_id(loggedUser.getOrg_id());
        binStockLog.setBalance(closingSummary.getQty());
        binStockLog.setUpdate_on(currentDate);
        binStockLog.setBinNo(closingSummary.getBinname());
        binStockLog.setGodownId(closingSummary.getGid());
        binstockAuditLogRepository.save(binStockLog);
    }*/


    public ResponseEntity<?> yieldSummary(Long branch, String accountingDate, User loggedUser) throws UsernameNotFoundException {

        //newly changes added
        Date accountingDateCon = null;
        Date lastAccountingDate = null;
        Date accountingDatePlus = null;

        LocalDateTime yesterDayAccountingCloseDateTime = null;
        if (branch != null) {
            branch = loggedUser.getBranch_id();
        }
        if (accountingDate != null) {
            accountingDateCon = dateUtil.getStringTODateWk(accountingDate);
        }

        Optional<DayEndStockClosingSummary> closingSummary = dayEndStockClosingSummaryRepository.checkLastDate(branch);
        if (closingSummary.isPresent()) {
            lastAccountingDate = dateUtil.getStringTODateWk(dateUtil.getDateTOStringWk(closingSummary.get().getAdate()));
            yesterDayAccountingCloseDateTime = closingSummary.get().getCreated_date();
        }
        System.out.println("accountingDateCon == " + accountingDateCon);
        System.out.println("lastAccountingDate == " + lastAccountingDate);

        System.out.println("yesterDayAccountingCloseDateTime == " + yesterDayAccountingCloseDateTime);

        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(lastAccountingDate);
        calendar.add(Calendar.DATE, 1);
        accountingDatePlus = calendar.getTime();

        if (lastAccountingDate.equals(accountingDateCon)) {

            return new ResponseEntity<Status>(new Status("201", "Already Updated This Date,  We Need Account Closing this Date " + dateUtil.getDateTOStringWk(calendar.getTime())), HttpStatus.OK);

        } else if (accountingDateCon.before(lastAccountingDate)) {

            return new ResponseEntity<Status>(new Status("201", "Already Updated This Date,  We Need Account Closing this Date " + dateUtil.getDateTOStringWk(calendar.getTime())), HttpStatus.OK);

        } else if (accountingDateCon.after(accountingDatePlus)) {

            return new ResponseEntity<Status>(new Status("201", "We Need Account Closing the Previous Date  " + dateUtil.getDateTOStringWk(calendar.getTime())), HttpStatus.OK);

        }

        Optional<DayEndStockClosingSummary> optional = dayEndStockClosingSummaryRepository.checkCloseOrNot(branch, accountingDateCon);
        if (optional.isPresent()) {
            return new ResponseEntity<Status>(new Status("201", "DayEndClosing already Updated!!!"), HttpStatus.OK);
        }

        updateDayEndClosingStockDetails(loggedUser, branch, accountingDateCon);

        List<YieldSummary> list = null;
        Date startDate = dateUtil.getLocalDateTimeToDate(yesterDayAccountingCloseDateTime);
        Date endDate = dateUtil.getISTDate();
        if (endDate.equals(accountingDateCon)) {
            accountingDateCon = endDate;
        } else {
            Calendar calendar1 = GregorianCalendar.getInstance();
            calendar1.setTime(accountingDateCon);
            calendar1.add(Calendar.HOUR, 23);
            calendar1.add(Calendar.MINUTE, 59);
            accountingDateCon = calendar1.getTime();
        }

        if (loggedUser.getBranch_id() == 3) {
            list = journalEntryRepository.getYieldSummaryListVaraloti();
        } else {
            list = journalEntryRepository.getYieldSummaryList();
        }

        if (!list.isEmpty()) {
            List<YieldSummaryDto> result = new ArrayList<>();
            for (YieldSummary yieldSummary : list) {

                YieldSummaryDto summaryDto = new YieldSummaryDto(yieldSummary);
                Double todayProduction = 0.0;
                if (summaryDto.getItemId() == 1040) {
                    todayProduction = journalEntryRepository.getYieldSummaryTodayProductionIssue(branch, 8L, summaryDto.getItemId(), accountingDateCon);
                    summaryDto.setTodayProduction(todayProduction);
                } else {
                    todayProduction = journalEntryRepository.getYieldSummaryTodayProduction(branch, summaryDto.getItemId(), accountingDateCon);
                    summaryDto.setTodayProduction(todayProduction);
                }

                if ((summaryDto.getItemId() == 777) || (summaryDto.getItemId() == 778)) {

                    Double consumption = journalEntryRepository.getConsumptionSummaryValues(summaryDto.getItemId(), branch, accountingDateCon);
                    if (consumption != null) {
                        summaryDto.setConsumption(decfor.format(consumption));
                    }
                }

                Calendar yesterDayCal = GregorianCalendar.getInstance();
                yesterDayCal.setTime(accountingDateCon);
                yesterDayCal.add(Calendar.DATE, -1);

                Date yesterDay = yesterDayCal.getTime();

                System.out.println("branch == " + branch + "itemId = " + summaryDto.getItemId() + "  date" + yesterDay);

                Double yesterDayClosing = dayEndStockClosingSummaryRepository.getCosingStock(branch, summaryDto.getItemId(), yesterDay);
                if (yesterDayClosing != null) {
                    summaryDto.setYesterdayClosing(decfor.format(yesterDayClosing));
                }

                if (summaryDto.getItemId() == 777) {

                    todayProduction = journalEntryRepository.getYieldSummaryTodayProductionIssue(
                            branch, 8L, 1040L, accountingDateCon);
                    Double oilProduction = 0.0;
                    if (summaryDto.getTodayProduction() != null) {
                        oilProduction = Double.valueOf(summaryDto.getTodayProduction());
                    }

                    if (todayProduction != null && todayProduction > 0 && oilProduction != null) {

                        double convertBag = todayProduction / 74.0;

                        if (convertBag > 0) {
                            double todayYieldPercent = oilProduction / convertBag;

                            System.out.println("todayYield % == " + todayYieldPercent);
                            summaryDto.setTodayYield(decfor.format(todayYieldPercent));
                        } else {
                            summaryDto.setTodayYield("0");
                        }

                    } else {
                        summaryDto.setTodayYield("0");
                    }

                } else if (summaryDto.getItemId() == 778) {

                    todayProduction = journalEntryRepository.getYieldSummaryTodayProductionIssue(
                            branch, 8L, 770L, accountingDateCon);
                    Double oilProduction = 0.0;
                    if (summaryDto.getTodayProduction() != null) {
                        oilProduction = Double.valueOf(summaryDto.getTodayProduction());
                    }

                    if (todayProduction != null && todayProduction > 0 && oilProduction != null) {

                        double convertBag = todayProduction / 74.0;

                        if (convertBag > 0) {
                            double todayYieldPercent = oilProduction / convertBag;

                            System.out.println("todayYield % == " + todayYieldPercent);
                            summaryDto.setTodayYield(decfor.format(todayYieldPercent));
                        } else {
                            summaryDto.setTodayYield("0");
                        }

                    } else {
                        summaryDto.setTodayYield("0");
                    }
                } else {
                    updateYield(summaryDto, branch, accountingDateCon);
                }

                result.add(summaryDto);
            }
            return new ResponseEntity<List<YieldSummaryDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<YieldSummaryDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    public void updateYield(YieldSummaryDto summaryDto, Long branch, Date accountingDateCon) {


        List<YieldSummaryList> lists = journalEntryRepository.getYieldSummaryValues(summaryDto.getItemId(), branch, accountingDateCon);

        List<YieldSummaryListDto> yieldSummaryLists = lists.stream().map(data -> new YieldSummaryListDto(data)).collect(Collectors.toList());

        List<Double> yieldSum = new ArrayList<>();


        for (YieldSummaryListDto yieldSummary : yieldSummaryLists) {

            if (yieldSummary.getItemId() == 1040) {

                List<YieldSummaryList> journalEntries = journalEntryRepository.findBy(branch, Long.valueOf(773), yieldSummary.getBatchNo());

                Double issueQty = journalEntries.stream().collect(Collectors.summingDouble(YieldSummaryList::getQty));

                Double issueQtyYield = issueQty / 74;

                Double singleBatchYield = yieldSummary.getQty() / issueQtyYield;

                yieldSum.add(singleBatchYield);

            } else if (yieldSummary.getItemId() == 777) {

                List<YieldSummaryList> journalEntries = journalEntryRepository.findBy(branch, Long.valueOf(1040), yieldSummary.getBatchNo());

                Double issueQty = journalEntries.stream().collect(Collectors.summingDouble(YieldSummaryList::getQty));

                Double issueQtyYield = issueQty / 74;

                Double singleBatchYield = yieldSummary.getQty() / issueQtyYield;

                yieldSum.add(singleBatchYield);

            } else if (yieldSummary.getItemId() == 770) {

                List<YieldSummaryList> journalEntries = journalEntryRepository.findBy(branch, Long.valueOf(774), yieldSummary.getBatchNo());

                Double issueQty = journalEntries.stream().collect(Collectors.summingDouble(YieldSummaryList::getQty));

                Double issueQtyYield = issueQty / 74;

                Double singleBatchYield = yieldSummary.getQty() / issueQtyYield;

                yieldSum.add(singleBatchYield);

            } else if (yieldSummary.getItemId() == 778) {

                List<YieldSummaryList> journalEntries = journalEntryRepository.findBy(branch, Long.valueOf(770), yieldSummary.getBatchNo());

                Double issueQty = journalEntries.stream().collect(Collectors.summingDouble(YieldSummaryList::getQty));

                Double issueQtyYield = issueQty / 74;

                Double singleBatchYield = yieldSummary.getQty() / issueQtyYield;

                yieldSum.add(singleBatchYield);
            }

        }

        int size = yieldSum.size();

        Double total = yieldSum.stream().collect(Collectors.summingDouble(Double::doubleValue));

        System.out.println("total == " + total);

        Double todayYield = total / size;

        System.out.println("todayYield == " + todayYield);

        if (todayYield > 0) {

            Double todayYie = todayYield;
            todayYie *= 100;

            System.out.println("todayYield1 == " + todayYie);

            summaryDto.setTodayYield(decfor.format(todayYield));

        }

        System.out.println("yeterDay == " + dateUtil.getISTYesterDayDate());


    }

    public void updateDayEndClosingStockDetails(User loggedUser, Long branch, Date accountingDateCon) {

        Date date = dateUtil.getISTDate();

        if (date.equals(accountingDateCon)) {
            accountingDateCon = date;
        } else {
            Calendar calendar = GregorianCalendar.getInstance();
            calendar.setTime(accountingDateCon);
            calendar.add(Calendar.HOUR, 23);
            calendar.add(Calendar.MINUTE, 59);
            accountingDateCon = calendar.getTime();
        }

        List<DayEndStockList> list = binStockRepository.findByMainDayEndClosingStockDetails(branch);
        if (!list.isEmpty()) {
            for (DayEndStockList data : list) {
                Optional<DayEndStockClosingSummary> dayEndStockClosingSummary = dayEndStockClosingSummaryRepository.findByDateAndBranchAndItem(data.getBranchId(), data.getItemId(), accountingDateCon);
                DayEndStockClosingSummary endStockClosingSummary = null;
                if (dayEndStockClosingSummary.isPresent()) {
                    endStockClosingSummary = dayEndStockClosingSummary.get();
                    endStockClosingSummary.setBinStock(data.getQty());
                } else {
                    endStockClosingSummary = new DayEndStockClosingSummary(data);
                }
                endStockClosingSummary.setCreatedBy(loggedUser.getId());
                endStockClosingSummary.setAdate(accountingDateCon);
                endStockClosingSummary.setStatus("Pending");
                endStockClosingSummary.setCreated_date(dateUtil.getDateToLocalDateTime(accountingDateCon));
                dayEndStockClosingSummaryRepository.save(endStockClosingSummary);
            }
        }
    }


    public ResponseEntity<?> filterPhysicalStockList(Long branch, Long dept, Double lot, Long binName, User
            loggedUser) throws UsernameNotFoundException {

        List<FilterPhysicalStockList> list = physicalStockRepository.findBy(branch, dept, lot, binName, dateUtil.getISTDate());
        if (!list.isEmpty()) {
            List<PhysicalStockDto> result = list.stream().map(data -> {

                PhysicalStockDto physicalStockDto = new PhysicalStockDto(data);
                if (physicalStockDto.getAby() != null) {
                    Optional<User> user = userRepository.findById(physicalStockDto.getAby());
                    if (user.isPresent()) {
                        physicalStockDto.setAbyName(user.get().getEmployeename());
                    }
                }
                Double actualQty = binStockRepository.getActualStockQty(physicalStockDto.getBid(), physicalStockDto.getBinname(), physicalStockDto.getBinid(), physicalStockDto.getRefid(), physicalStockDto.getGid(), physicalStockDto.getLot(), physicalStockDto.getDid());

                physicalStockDto.setActualStkQty(actualQty);
                return physicalStockDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<PhysicalStockDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<PhysicalStockDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    /*public ResponseEntity<?> journalOrMrnNotClosing(Long branch, Long dept, User loggedUser, String accountingDate) throws UsernameNotFoundException {


        Date accountingDateCon = null;

        Date lastAccountingDate = null;

        Date accountingDatePlus = null;

        LocalDateTime yesterDayAccountingCloseDateTime = null;

        if (accountingDate != null) {
            accountingDateCon = dateUtil.getStringTODateWk(accountingDate);
        }

        if (branch != null) {
            branch = loggedUser.getBranch_id();
        }

        Optional<DayEndStockClosingSummary> closingSummary = dayEndStockClosingSummaryRepository.checkLastDate(branch);
        if (closingSummary.isPresent()) {
            lastAccountingDate = dateUtil.getStringTODateWk(dateUtil.getDateTOStringWk(closingSummary.get().getAdate()));
            yesterDayAccountingCloseDateTime = closingSummary.get().getCreated_date();
        }

        Date startDate = dateUtil.getLocalDateTimeToDate(yesterDayAccountingCloseDateTime);
        Date endDate = dateUtil.getISTDate();


        if (endDate.equals(accountingDateCon)) {
            accountingDateCon = endDate;
        } else {
            Calendar calendar = GregorianCalendar.getInstance();
            calendar.setTime(accountingDateCon);
            calendar.add(Calendar.HOUR, 23);
            calendar.add(Calendar.MINUTE, 59);

            accountingDateCon = calendar.getTime();
        }

        System.out.println(" accountingDateCon " + accountingDateCon);


        List<JournalOrMrnNotClosingListDto> result = new ArrayList<>();
        List<JournalOrMrnNotClosingList> journalClosingLists = productionEntryRepository.findBy(branch, startDate, accountingDateCon);
        if (!journalClosingLists.isEmpty()) {
            result.addAll(journalClosingLists.stream().map(data -> new JournalOrMrnNotClosingListDto(data)).collect(Collectors.toList()));
        }
        System.out.println("endDate : " + accountingDateCon);
        List<JournalOrMrnNotClosingList> mrnNotClosingLists = materialRequestRepository.findBy(branch, accountingDateCon);
        System.out.println("mrnNotClosingLists : " + mrnNotClosingLists);
        if (!mrnNotClosingLists.isEmpty()) {
            result.addAll(mrnNotClosingLists.stream().map(data -> new JournalOrMrnNotClosingListDto(data, true)).collect(Collectors.toList()));
        }

        List<JournalOrMrnNotClosingList> mrnNotClosingLists1 = billInwardRepository.findByPending(accountingDateCon, branch);
        System.out.println("mrnNotClosingLists1 : " + mrnNotClosingLists1);
        if (!mrnNotClosingLists1.isEmpty()) {
            result.addAll(mrnNotClosingLists1.stream().map(data -> new JournalOrMrnNotClosingListDto(data, true)).collect(Collectors.toList()));
        }

        if (!result.isEmpty()) {
            return new ResponseEntity<List<JournalOrMrnNotClosingListDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<JournalOrMrnNotClosingListDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }*/
    @Transactional(readOnly = true)
    public ResponseEntity<List<JournalOrMrnNotClosingListDto>> journalOrMrnNotClosing(
            Long branch,
            Long dept,
            User loggedUser,
            String accountingDate
    ) throws UsernameNotFoundException {

        /* =========================
           1️⃣ Resolve branch list
           ========================= */
        List<Long> branchList = new ArrayList<>();

        Long effectiveBranch = (branch != null)
                ? branch
                : loggedUser.getBranch_id();

        if (effectiveBranch == null) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
        }

        branchList.add(effectiveBranch);

        List<Long> childBranches =
                dayEndClosingBranchGroupRepository.findChildBranchIdByBranchId(effectiveBranch);

        if (childBranches != null && !childBranches.isEmpty()) {
            branchList.addAll(childBranches);
        }

        /* =========================
           2️⃣ Resolve accounting date
           ========================= */
        Date endDate = parseEndOfDay(accountingDate);

        logger.info(
                "Journal/MRN Not Closing | branches={} accountingDate={}",
                branchList,
                endDate
        );

        /* =========================
           3️⃣ Find last day-end close
           ========================= */
        Optional<DayEndStockClosingSummary> lastCloseOpt =
                dayEndStockClosingSummaryRepository.checkLastDate(effectiveBranch);

        Date startDate = lastCloseOpt
                .map(s -> dateUtil.getLocalDateTimeToDate(s.getCreated_date()))
                .orElse(null);

        List<JournalOrMrnNotClosingListDto> result = new ArrayList<>();

        /* =========================
           4️⃣ Journal not closed
           ========================= */
        List<JournalOrMrnNotClosingList> journalList =
                productionEntryRepository.findByBranchList(
                        branchList,
                        startDate,
                        endDate
                );

        for (JournalOrMrnNotClosingList j : journalList) {
            result.add(new JournalOrMrnNotClosingListDto(j));
        }

        /* =========================
           5️⃣ MRN not closed
           ========================= */
        List<JournalOrMrnNotClosingList> mrnList =
                materialRequestRepository.findBy(branchList, endDate);

        for (JournalOrMrnNotClosingList m : mrnList) {
            result.add(new JournalOrMrnNotClosingListDto(m, true));
        }

        /* =========================
           6️⃣ Bill inward pending
           ========================= */
        List<JournalOrMrnNotClosingList> billPendingList =
                billInwardRepository.findByPending(endDate, branchList);

        for (JournalOrMrnNotClosingList b : billPendingList) {
            result.add(new JournalOrMrnNotClosingListDto(b, true));
        }

        logger.info(
                "Journal/MRN Not Closing completed | count={}",
                result.size()
        );

        return new ResponseEntity<>(result, HttpStatus.OK);
    }


    private Date parseEndOfDay(String dateStr) {
        if (dateStr == null) {
            return dateUtil.getISTDate();
        }
        Date date = dateUtil.getStringTODateWk(dateStr);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        return cal.getTime();
    }


    public ResponseEntity<?> checkDateServer() throws UsernameNotFoundException {
        Date startDate1 = dateUtil.getISTClosingStartDateTime();
        System.out.println(" startDate1 " + startDate1);
        Date startDate = dateUtil.getISTPreviousDate();
        System.out.println(" startDate " + startDate);
        return new ResponseEntity<Status>(new Status("200", "check date"), HttpStatus.OK);
    }


    public ResponseEntity<?> journalFilterDayClosing(Long branchId, Long deptId, Long journalId, String
            journalName, String status, String fromDate, String toDate, Pageable pageable) throws UsernameNotFoundException {
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

        if (status == null) {
            status = "Y";
        }


        Page<ProductionEntryList> list = productionEntryRepository.getDayEndPageProductionEntryFilterList(branchId, deptId, journalId, journalName, status, startDate, endDate, pageable);

        if (!list.toList().isEmpty()) {
            List<ProductionEntryListDto> result = list.toList().stream().map(data -> {
                ProductionEntryListDto pEntry = new ProductionEntryListDto(data);
                if (pEntry.getMachineId() != null) {

                    if (pEntry.getMmctable() != null && pEntry.getMmctable().equalsIgnoreCase("machine")) {
                        Optional<Machine> optionalMachine = machineRepository.findById(pEntry.getMachineId());
                        if (optionalMachine.isPresent()) {
                            pEntry.setMachineName(optionalMachine.get().getName());
                        } else {
                            System.out.println("no Machine");
                        }
                    } else {
                        Optional<DwCluster> optionalMachine = dwClusterRepository.findById(pEntry.getMachineId());
                        if (optionalMachine.isPresent()) {
                            pEntry.setMachineName(optionalMachine.get().getName());
                        } else {
                            System.out.println("no dwCluster");
                        }
                    }
                }
                return pEntry;
            }).collect(Collectors.toList());

            PageDataDto<ProductionEntryListDto> pageData = new PageDataDto(result, list);
            return new ResponseEntity<PageDataDto<ProductionEntryListDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<PageDataDto<ProductionEntryListDto>>(new PageDataDto(), HttpStatus.OK);
        }
    }


    public ResponseEntity<?> productionLedger(Long companyId, Long branchId, Long deptId, Long productId, Double
            lot, String fromDate, String toDate, Pageable pageable) throws
            UsernameNotFoundException {

        String logCheck = "br = " + branchId + " # " + "dt = " + deptId + " # " + "pro = " + productId + " # ";
        System.out.println(logCheck);

        Date startDate = dateUtil.getStringTODateWk(fromDate);
        Date endDate = dateUtil.getStringTODateWk(toDate);

        System.out.println("startDate = " + startDate);
        System.out.println("endDate = " + endDate);
        Map<String, Object> stringObjectMap = new HashMap<>();

        List<StockLedgerDto> result = new ArrayList<>();

        List<StockLedger> openingList = binstockAuditLogRepository.getProductionLedgerOpeningOrClosing(companyId, branchId, productId, lot, "OPENING", startDate);

        result.addAll(openingList.stream().map(StockLedgerDto::new).collect(Collectors.toList()));

        List<StockLedger> list = binstockAuditLogRepository.getProductionLedger(companyId, branchId, productId, lot, startDate, endDate);

        result.addAll(list.stream().map(StockLedgerDto::new).collect(Collectors.toList()));

        List<StockLedger> closingList = binstockAuditLogRepository.getProductionLedgerOpeningOrClosing(companyId, branchId, productId, lot, "CLOSING", endDate);

        result.addAll(closingList.stream().map(StockLedgerDto::new).collect(Collectors.toList()));

        stringObjectMap.put("ledgerList", result);
        List<DayEndStockList> dayEndStockLists = binStockRepository.findByProductWiseClosingStockDetails(productId, branchId);
        stringObjectMap.put("closingDetails", dayEndStockLists);

        return new ResponseEntity<Map<String, Object>>(stringObjectMap, HttpStatus.OK);
    }


    public ResponseEntity<?> productionLedgerView(String trnNo) throws UsernameNotFoundException {


        JournalEntryView journalEntryView = journalEntryRepository.getJournalEntryView(null, null, null, trnNo, null);

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
                Optional<GoDown> optional = goDownRepository.findById(result.getGodownId());
                if (optional.isPresent()) {
                    result.setGodownName(optional.get().getName());
                }
            }

            updateJournalEntryView(result, trnNo);
            return new ResponseEntity<JournalEntryViewDto>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<JournalEntryViewDto>(HttpStatus.NOT_FOUND);
        }
    }

    public void updateJournalEntryView(JournalEntryViewDto journalEntryViewDto, String trnNo) {

        List<JournalEntryDto> outputProducts = new ArrayList<>();

        List<JournalEntry> list = journalEntryRepository.getJournalEntryViewByeList(journalEntryViewDto.getJournalId(), dateUtil.getStringTODateWk(journalEntryViewDto.getTrnDate()), null, trnNo, null);
        if (!list.isEmpty()) {

            List<JournalEntryDto> result = list.stream().map(data -> {
                JournalEntryDto j = new JournalEntryDto(data);
                if (j.getProid() != null) {

                    Optional<ProductMaster> optional = productMasterRepository.findById(j.getProid());
                    if (optional.isPresent()) {
                        j.setProductName(optional.get().getProductName());
                    }

                    /*Optional<JournalProductMapping> journalProductMappingOptional = journalProductMappingRepository.findByJournalProductMapping(journalEntryViewDto.getJournalId(), j.getProid(), j.getType());
                    if (journalProductMappingOptional.isPresent()) {
                        j.setBomTakeType(journalProductMappingOptional.get().getBomTakeType());
                    }*/

                    Optional<GoDown> goDownOptional = goDownRepository.findByGoName(j.getBinname());
                    if (goDownOptional.isPresent()) {
                        j.setGodownName(goDownOptional.get().getName());
                        j.setGodownId(goDownOptional.get().getId());
                    }

                }
                return j;
            }).collect(Collectors.toList());

            outputProducts.addAll(result);
        }

        List<JournalEntryDataList> bomlist = journalEntryRepository.getJournalEntryViewBomList(journalEntryViewDto.getJournalId(), dateUtil.getStringTODateWk(journalEntryViewDto.getTrnDate()), null, trnNo, null);
        if (!bomlist.isEmpty()) {
            System.out.println("bom == " + bomlist);
            for (JournalEntryDataList data : bomlist) {
                System.out.println("productId  == " + data.getProid() + "journalId  == " + data.getSjid() + "trnNo  == " + data.getTrnno() + "tpe  == I");
                List<BinStockLog> logList = binStockLogRepository.getLogList(data.getSjid(), data.getProid(), "I", data.getTrnno());

                if (!logList.isEmpty()) {

                    System.out.println("logList == " + logList);

                    for (BinStockLog binStockLog : logList) {

                        JournalEntryDto j = new JournalEntryDto(data);

                        if (j.getProid() != null) {
                            Optional<ProductMaster> optional = productMasterRepository.findById(j.getProid());
                            if (optional.isPresent()) {
                                j.setProductName(optional.get().getProductName());
                            }

                            /*Optional<JournalProductMapping> journalProductMappingOptional = journalProductMappingRepository.findByJournalProductMapping(journalEntryViewDto.getJournalId(), j.getProid(), j.getType());
                            if (journalProductMappingOptional.isPresent()) {
                                j.setBomTakeType(journalProductMappingOptional.get().getBomTakeType());
                            }*/
                        }

                        Optional<GoDown> goDownOptional = goDownRepository.findByGoName(binStockLog.getGname());
                        if (goDownOptional.isPresent()) {
                            j.setGodownName(goDownOptional.get().getName());
                            j.setGodownId(goDownOptional.get().getId());
                        }

                        // stbin details

                        j.setBatchid(binStockLog.getBatchid());
                        j.setBatchno(binStockLog.getBatchno());
                        j.setBinid(binStockLog.getGcode());
                        j.setBinname(binStockLog.getGname());
                        j.setStkqty(binStockLog.getQty());
                        j.setCost(binStockLog.getCost());
                        j.setAvgcost(binStockLog.getAvgcost());
                        j.setGodown(binStockLog.getGid());
                        j.setLot(binStockLog.getLot());
                        outputProducts.add(j);
                    }

                }

            }
        }


        journalEntryViewDto.setOutputProducts(outputProducts);
    }


    public ResponseEntity<?> rawMaterialMovementSummary(String trnDate, User loggedUser) throws UsernameNotFoundException {


        Date date = null;
        if (trnDate != null) {
            date = dateUtil.getStringTODateWk(trnDate);
        }

        System.out.println("Date == " + date + " Branch == " + loggedUser.getBranch_id());

        List<RawMaterialMovementSummary> result = new ArrayList<>();

        List<ProductMaster> productMasterList = productMasterRepository.rawMaterialProductList(loggedUser.getBranch_id(), date);

        if (!productMasterList.isEmpty()) {

            for (ProductMaster productMaster : productMasterList) {

                System.out.println("ProductId == " + productMaster.getId() + "   " + " Name  ==  " + productMaster.getProductName() + "  Date = " + date + " branch == " + loggedUser.getBranch_id());

                RawMaterialMovementSummary movementSummary = new RawMaterialMovementSummary();

                movementSummary.setItemId(productMaster.getId());

                movementSummary.setItemName(productMaster.getProductName());

                List<Double> lotList = binStockLogRepository.findByLotList(date);

                movementSummary.setLotList(lotList);

                movementSummary.setOpeningStock(openingStock(lotList, productMaster.getId(), loggedUser.getBranch_id(), date));

                movementSummary.setPurchases(getPurchase(lotList, productMaster.getId(), loggedUser.getBranch_id(), date));

                movementSummary.setReceivedFromBranchWise(getBranchWiseReceive(lotList, productMaster.getId(), loggedUser.getBranch_id(), date));

                movementSummary.setIssueFrom(getBranchWiseIssue(lotList, productMaster.getId(), loggedUser.getBranch_id(), date));

                movementSummary.setIssueFromProcess(getIssueFromProcess(lotList, loggedUser.getBranch_id(), productMaster.getId(), date));

                movementSummary.setReceiveFromProcess(getReceiveFromProcess(lotList, loggedUser.getBranch_id(), productMaster.getId(), date));

                movementSummary.setClosingStock(closingStock(lotList, productMaster.getId(), loggedUser.getBranch_id(), date));

                result.add(movementSummary);
            }
        }
        if (!result.isEmpty()) {
            return new ResponseEntity<List<RawMaterialMovementSummary>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<RawMaterialMovementSummary>>(HttpStatus.NOT_FOUND);
        }
    }

    public Map<String, Object> getPurchase(List<Double> lotList, Long productId, Long branch, Date accountDate) {


        Map<String, Object> stringMap = new HashMap<>();

        for (Double lot : lotList) {
            Object count = binStockLogRepository.rawMaterialMovementSummary(productId, branch, lot, accountDate);
            stringMap.put(lot.toString(), count);
        }

        return stringMap;

    }

    public Map<String, Object> getBranchWiseReceive(List<Double> lotList, Long productId, Long reqBranch, Date accountDate) {

        Map<String, Object> stringMap = new HashMap<>();

        List<Branch> branchList = branchRepository.findBy(reqBranch);
        if (!branchList.isEmpty()) {
            for (Branch branchDetail : branchList) {
                // System.out.println("Branch == " + branchDetail.getBranchName() + "   reqBranch == " + reqBranch);
                stringMap.put(branchDetail.getBranchName(), getBranchWiseReceiveDetails(lotList, reqBranch, branchDetail.getId(), productId, accountDate));
            }
        }

        return stringMap;

    }


    public Map<String, Object> getBranchWiseReceiveDetails(List<Double> lotList, Long reqBranch, Long issueBranch, Long productId, Date accountDate) {

        //System.out.println("productId == " + productId + "issueBranch == " + issueBranch + "   reqBranch == " + reqBranch);
        Map<String, Object> stringMap = new HashMap<>();
        for (Double lot : lotList) {
            Object count = binStockLogRepository.rawMaterialMovementSummaryMRNReceive(productId, reqBranch, issueBranch, lot, accountDate);
            stringMap.put(lot.toString(), count);
        }

        return stringMap;

    }


    public Map<String, Object> getBranchWiseIssue(List<Double> lotList, Long productId, Long issueBranch, Date accountDate) {

        Map<String, Object> stringMap = new HashMap<>();

        List<Branch> branchList = branchRepository.findBy(issueBranch);
        if (!branchList.isEmpty()) {
            for (Branch branchDetail : branchList) {
                stringMap.put(branchDetail.getBranchName(), getBranchWiseIssueDetails(lotList, branchDetail.getId(), issueBranch, productId, accountDate));
            }
        }

        return stringMap;
    }


    public Map<String, Object> getBranchWiseIssueDetails(List<Double> lotList, Long requestBranch, Long issueBranch, Long productId, Date accountDate) {

        Map<String, Object> stringMap = new HashMap<>();
        for (Double lot : lotList) {
            Object count = binStockLogRepository.rawMaterialMovementSummaryMRNIssue(productId, requestBranch, issueBranch, lot, accountDate);
            stringMap.put(lot.toString(), count);
        }
        return stringMap;

    }


    public Map<String, Object> getIssueFromProcess(List<Double> lotList, Long branch, Long productId, Date accountDate) {

        //System.out.println("ProductId == " + productId + "   " + " Name  ==  " + productId + "  Date = " + accountDate + " branch == " + branch);

        Map<String, Object> stringMap = new HashMap<>();
        for (Double lot : lotList) {
            Object count = binStockLogRepository.rawMaterialMovementSummaryProductionIssue(productId, branch, lot, accountDate);
            stringMap.put(lot.toString(), count);
        }
        return stringMap;

    }


    public Map<String, Object> getReceiveFromProcess(List<Double> lotList, Long branch, Long productId, Date accountDate) {

        Map<String, Object> stringMap = new HashMap<>();
        for (Double lot : lotList) {
            Object count = binStockLogRepository.rawMaterialMovementSummaryProductionReceive(productId, branch, lot, accountDate);
            stringMap.put(lot.toString(), count);
        }
        return stringMap;

    }


    public ResponseEntity<?> accountClosingValidation(String trnDate, Long branch, User loggedUser) throws UsernameNotFoundException {

        Map<String, Object> stringObjectMap = new HashMap<>();

        Date accountingDateCon = null;
        Date lastAccountingDate = null;
        Date accountingDatePlus = null;

        if (trnDate != null) {
            accountingDateCon = dateUtil.getStringTODateWk(trnDate);
        }

        Optional<DayEndStockClosingSummary> dayEndStockClosingSummary = dayEndStockClosingSummaryRepository.checkLastDate(branch);
        if (dayEndStockClosingSummary.isPresent()) {
            if (dayEndStockClosingSummary.get().getAdate() != null) {
                lastAccountingDate = dayEndStockClosingSummary.get().getAdate();
            }
        } else {
            lastAccountingDate = dateUtil.getStringTODateWk(trnDate);
        }

        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(lastAccountingDate);
        calendar.add(Calendar.DATE, 1);
        accountingDatePlus = calendar.getTime();

        if (lastAccountingDate.equals(accountingDateCon)) {

            stringObjectMap.put("Date", dateUtil.getDateTOStringWk(calendar.getTime()));
            stringObjectMap.put("201", " Already Updated This Date,  We Need Account Closing this Date " + dateUtil.getDateTOStringWk(calendar.getTime()));

        } else if (accountingDateCon.before(lastAccountingDate)) {

            stringObjectMap.put("Date", dateUtil.getDateTOStringWk(calendar.getTime()));
            stringObjectMap.put("201", " Already Updated This Date,  We Need Account Closing this Date " + dateUtil.getDateTOStringWk(calendar.getTime()));

        } else if (accountingDateCon.after(accountingDatePlus)) {

            stringObjectMap.put("Date", dateUtil.getDateTOStringWk(calendar.getTime()));
            stringObjectMap.put("201", " We Need Account Closing the Previous Date " + dateUtil.getDateTOStringWk(calendar.getTime()));

        } else {

            stringObjectMap.put("Date", trnDate);
            stringObjectMap.put("200", " Today Day End Account Need to Close End of the Day.. " + trnDate);

        }

        return new ResponseEntity<Map<String, Object>>(stringObjectMap, HttpStatus.OK);
    }

    public Map<String, Object> openingStock(List<Double> lotList, Long productId, Long branch, Date accountDate) {


        Map<String, Object> stringMap = new HashMap<>();

        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(accountDate);
        calendar.add(Calendar.DATE, -1);

        accountDate = calendar.getTime();

        for (Double lot : lotList) {
            Object count = physicalStockRepository.rawMaterialMovementSummary(productId, branch, lot, accountDate);
            stringMap.put(lot.toString(), count);
        }

        return stringMap;

    }


    public Map<String, Object> closingStock(List<Double> lotList, Long productId, Long branch, Date accountDate) {

        Map<String, Object> stringMap = new HashMap<>();

        for (Double lot : lotList) {
            Object count = physicalStockRepository.rawMaterialMovementSummary(productId, branch, lot, accountDate);
            stringMap.put(lot.toString(), count);
        }

        return stringMap;

    }


    public ResponseEntity<?> rawMaterialMovementQtyWiseSummary(String type, String accountingDate, Long productId, Long branch, Long reqBranch, Long issueBranch, Double lot, User loggedUser) throws UsernameNotFoundException {

        Date accountingDateCon = null;
        if (accountingDate != null) {
            accountingDateCon = dateUtil.getStringTODateWk(accountingDate);
        }

        System.out.println("accountingDateCon = " + accountingDateCon + " == " + loggedUser.getBranch_id());

        List<RawMaterialMovementQtyWiseSummaryDto> result = null;

        if (type.equalsIgnoreCase("openingStock")) {

            result = openingStockRawMaterialMovementQtyWiseSummary(branch, productId, lot, accountingDateCon);

        } else if (type.equalsIgnoreCase("purchase")) {

            result = purchaseRawMaterialMovementQtyWiseSummary(branch, productId, lot, accountingDateCon);

        } else if (type.equalsIgnoreCase("receivedFrom")) {

            result = receivedFromRawMaterialMovementQtyWiseSummary(branch, issueBranch, productId, lot, accountingDateCon);

        } else if (type.equalsIgnoreCase("issueTo")) {

            result = issueToRawMaterialMovementQtyWiseSummary(branch, reqBranch, productId, lot, accountingDateCon);

        } else if (type.equalsIgnoreCase("issueToProcess")) {

            result = issueToProcessRawMaterialMovementQtyWiseSummary(branch, productId, lot, accountingDateCon);

        } else if (type.equalsIgnoreCase("receivedFromProcess")) {

            result = receiveFromProcessRawMaterialMovementQtyWiseSummary(branch, productId, lot, accountingDateCon);

        }


        if (!result.isEmpty()) {
            return new ResponseEntity<List<RawMaterialMovementQtyWiseSummaryDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<RawMaterialMovementQtyWiseSummaryDto>>(HttpStatus.NOT_FOUND);
        }
    }


    public List<RawMaterialMovementQtyWiseSummaryDto> openingStockRawMaterialMovementQtyWiseSummary(Long branch, Long productId, Double lot, Date accountingDateCon) {

        List<RawMaterialMovementQtyWiseSummaryDto> trnList = new ArrayList<>();

        List<RawMaterialMovementQtyWiseSummary> qtyWiseSummaries = binStockLogRepository.getPurchaseRawMaterialMovementQtyWiseSummary(branch, productId, lot, accountingDateCon);
        if (!qtyWiseSummaries.isEmpty()) {
            for (RawMaterialMovementQtyWiseSummary rawMaterialMovementQtyWiseSummary : qtyWiseSummaries) {
                RawMaterialMovementQtyWiseSummaryDto trn = new RawMaterialMovementQtyWiseSummaryDto(rawMaterialMovementQtyWiseSummary);
                List<RawMaterialMovementQtyWiseSummary> binWiseQtyWise = binStockLogRepository.getPurchaseRawMaterialMovementQtyWiseSummaryBinAndGodown(branch, productId, lot, rawMaterialMovementQtyWiseSummary.getBatchNo());
                List<RawMaterialMovementBinQtyDto> qtyDtoList = binWiseQtyWise.stream().map(RawMaterialMovementBinQtyDto::new).collect(Collectors.toList());
                trn.setTrnBinWiseQty(qtyDtoList);
                trnList.add(trn);
            }
        }

        return trnList;

    }


    public List<RawMaterialMovementQtyWiseSummaryDto> purchaseRawMaterialMovementQtyWiseSummary(Long branch, Long productId, Double lot, Date accountingDateCon) {

        List<RawMaterialMovementQtyWiseSummaryDto> trnList = new ArrayList<>();

        List<RawMaterialMovementQtyWiseSummary> qtyWiseSummaries = binStockLogRepository.getPurchaseRawMaterialMovementQtyWiseSummary(branch, productId, lot, accountingDateCon);
        if (!qtyWiseSummaries.isEmpty()) {
            for (RawMaterialMovementQtyWiseSummary rawMaterialMovementQtyWiseSummary : qtyWiseSummaries) {
                RawMaterialMovementQtyWiseSummaryDto trn = new RawMaterialMovementQtyWiseSummaryDto(rawMaterialMovementQtyWiseSummary);
                List<RawMaterialMovementQtyWiseSummary> binWiseQtyWise = binStockLogRepository.getPurchaseRawMaterialMovementQtyWiseSummaryBinAndGodown(branch, productId, lot, rawMaterialMovementQtyWiseSummary.getBatchNo());
                List<RawMaterialMovementBinQtyDto> qtyDtoList = binWiseQtyWise.stream().map(RawMaterialMovementBinQtyDto::new).collect(Collectors.toList());
                trn.setTrnBinWiseQty(qtyDtoList);
                trnList.add(trn);
            }
        }

        return trnList;

    }


    public List<RawMaterialMovementQtyWiseSummaryDto> receivedFromRawMaterialMovementQtyWiseSummary(Long reqBranch, Long issueBranch, Long productId, Double lot, Date accountingDateCon) {

        List<RawMaterialMovementQtyWiseSummaryDto> trnList = new ArrayList<>();

        List<RawMaterialMovementQtyWiseSummary> qtyWiseSummaries = binStockLogRepository.rawMaterialMovementQtyWiseSummaryReceivedFrom(reqBranch, issueBranch, productId, lot, accountingDateCon);
        if (!qtyWiseSummaries.isEmpty()) {
            for (RawMaterialMovementQtyWiseSummary rawMaterialMovementQtyWiseSummary : qtyWiseSummaries) {
                RawMaterialMovementQtyWiseSummaryDto trn = new RawMaterialMovementQtyWiseSummaryDto(rawMaterialMovementQtyWiseSummary);
                List<RawMaterialMovementQtyWiseSummary> binWiseQtyWise = binStockLogRepository.rawMaterialMovementQtyWiseSummaryReceivedBinAndGodown(productId, lot, rawMaterialMovementQtyWiseSummary.getTrnNo());
                List<RawMaterialMovementBinQtyDto> qtyDtoList = binWiseQtyWise.stream().map(RawMaterialMovementBinQtyDto::new).collect(Collectors.toList());
                trn.setTrnBinWiseQty(qtyDtoList);
                trnList.add(trn);
            }
        }

        return trnList;

    }


    public List<RawMaterialMovementQtyWiseSummaryDto> issueToRawMaterialMovementQtyWiseSummary(Long issueBranch, Long reqBranch, Long productId, Double lot, Date accountingDateCon) {

        List<RawMaterialMovementQtyWiseSummaryDto> trnList = new ArrayList<>();

        List<RawMaterialMovementQtyWiseSummary> qtyWiseSummaries = binStockLogRepository.rawMaterialMovementQtyWiseSummaryIssueTo(reqBranch, issueBranch, productId, lot, accountingDateCon);
        if (!qtyWiseSummaries.isEmpty()) {
            for (RawMaterialMovementQtyWiseSummary rawMaterialMovementQtyWiseSummary : qtyWiseSummaries) {
                RawMaterialMovementQtyWiseSummaryDto trn = new RawMaterialMovementQtyWiseSummaryDto(rawMaterialMovementQtyWiseSummary);
                List<RawMaterialMovementQtyWiseSummary> binWiseQtyWise = binStockLogRepository.rawMaterialMovementQtyWiseSummaryIssueToBinAndGodown(productId, lot, rawMaterialMovementQtyWiseSummary.getTrnNo());
                List<RawMaterialMovementBinQtyDto> qtyDtoList = binWiseQtyWise.stream().map(RawMaterialMovementBinQtyDto::new).collect(Collectors.toList());
                trn.setTrnBinWiseQty(qtyDtoList);
                trnList.add(trn);
            }
        }

        return trnList;
    }


    public List<RawMaterialMovementQtyWiseSummaryDto> issueToProcessRawMaterialMovementQtyWiseSummary(Long branch, Long productId, Double lot, Date accountingDateCon) {

        List<RawMaterialMovementQtyWiseSummaryDto> trnList = new ArrayList<>();

        List<RawMaterialMovementQtyWiseSummary> qtyWiseSummaries = binStockLogRepository.rawMaterialMovementSummaryProduction(branch, productId, lot, accountingDateCon, "PRODUCTION", "I");
        if (!qtyWiseSummaries.isEmpty()) {
            for (RawMaterialMovementQtyWiseSummary rawMaterialMovementQtyWiseSummary : qtyWiseSummaries) {
                RawMaterialMovementQtyWiseSummaryDto trn = new RawMaterialMovementQtyWiseSummaryDto(rawMaterialMovementQtyWiseSummary);
                List<RawMaterialMovementQtyWiseSummary> binWiseQtyWise = binStockLogRepository.rawMaterialMovementSummaryProductionBinAndGodown(productId, lot, rawMaterialMovementQtyWiseSummary.getBatchNo(), rawMaterialMovementQtyWiseSummary.getTrnNo(), "PRODUCTION", "I");
                List<RawMaterialMovementBinQtyDto> qtyDtoList = binWiseQtyWise.stream().map(RawMaterialMovementBinQtyDto::new).collect(Collectors.toList());
                trn.setTrnBinWiseQty(qtyDtoList);
                trnList.add(trn);
            }
        }
        return trnList;
    }


    public List<RawMaterialMovementQtyWiseSummaryDto> receiveFromProcessRawMaterialMovementQtyWiseSummary(Long branch, Long productId, Double lot, Date accountingDateCon) {

        List<RawMaterialMovementQtyWiseSummaryDto> trnList = new ArrayList<>();

        List<RawMaterialMovementQtyWiseSummary> qtyWiseSummaries = binStockLogRepository.rawMaterialMovementSummaryProduction(branch, productId, lot, accountingDateCon, "PRODUCTION", "R");
        if (!qtyWiseSummaries.isEmpty()) {
            for (RawMaterialMovementQtyWiseSummary rawMaterialMovementQtyWiseSummary : qtyWiseSummaries) {
                RawMaterialMovementQtyWiseSummaryDto trn = new RawMaterialMovementQtyWiseSummaryDto(rawMaterialMovementQtyWiseSummary);
                List<RawMaterialMovementQtyWiseSummary> binWiseQtyWise = binStockLogRepository.rawMaterialMovementSummaryProductionBinAndGodown(productId, lot, rawMaterialMovementQtyWiseSummary.getBatchNo(), rawMaterialMovementQtyWiseSummary.getTrnNo(), "PRODUCTION", "R");
                List<RawMaterialMovementBinQtyDto> qtyDtoList = binWiseQtyWise.stream().map(RawMaterialMovementBinQtyDto::new).collect(Collectors.toList());
                trn.setTrnBinWiseQty(qtyDtoList);
                trnList.add(trn);
            }
        }
        return trnList;
    }


    public ResponseEntity<?> updateSummary(Long branch, String accountingDate, User loggedUser) throws UsernameNotFoundException {

        //newly changes added
        Date accountingDateCon = null;

        if (branch != null) {
            branch = loggedUser.getBranch_id();
        }
        if (accountingDate != null) {
            accountingDateCon = dateUtil.getStringTODateWk(accountingDate);
        }

        List<Long> branchList = Arrays.asList(branch);

        List<Long> childBranchList = dayEndClosingBranchGroupRepository.findChildBranchIdByBranchId(branch);
        if (childBranchList != null && !childBranchList.isEmpty()) {
            logger.info("Child Branch List for Day End Closing: {}", childBranchList);
            branchList.addAll(childBranchList);
        }
        logger.info("Branch List for Day End Closing: {}", branchList);

        updateAllProductClosingStock(loggedUser, branchList, accountingDateCon);
        updateLedger(loggedUser, branchList, accountingDateCon);
        updateStatus(branchList, accountingDateCon);
        updatePhysicalStock(accountingDateCon, branchList, loggedUser);

        return new ResponseEntity<List<YieldSummaryDto>>(new ArrayList<>(), HttpStatus.OK);

    }

	public ResponseEntity<?> dayEndClosingValidation(String accountingDate, User user) {
		 Date accountingDateCon = null;
		 if (accountingDate != null) {
	            accountingDateCon = dateUtil.getStringTODateWk(accountingDate);
	        }
		 List<BranchStockList> closedBranches =dayEndStockClosingSummaryRepository.findDayEndClosedBranchesList();
		 List<Long> bId = closedBranches.stream().map(BranchStockList :: getId).distinct()
         .collect(Collectors.toList());
		 List<Long> closedBrancheDetails = dayEndStockClosingSummaryRepository.findByClosedBranchDetails(bId,accountingDateCon);
		 
		    if (closedBrancheDetails.isEmpty()) {
		        return ResponseEntity.ok(new Status("200", "Now Entry Allowed"));
		    }	
		    if (closedBrancheDetails.size() == bId.size()) {
		        return ResponseEntity.ok(new Status("200", "Now Entry Allowed"));
		    }
		    
		    List<String> closedBranchNames = closedBranches.stream()
		            .filter(b -> closedBrancheDetails.contains(b.getId()))
		            .map(BranchStockList::getBranchName)
		            .collect(Collectors.toList());

		    List<String> pendingBranchNames = closedBranches.stream()
		            .filter(b -> !closedBrancheDetails.contains(b.getId()))
		            .map(BranchStockList::getBranchName)
		            .collect(Collectors.toList());
		  
		    String message = "Date " + accountingDate +
		            " already closed for: " + String.join(", ", closedBranchNames) +
		            ". Please close remaining branches: " +
		            String.join(", ", pendingBranchNames) +
		            ". Then only entry allowed.";

		    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new Status(
                            "403", message));
		}
			

}
