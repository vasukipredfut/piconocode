package com.aria.service;

import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.Branch;
import com.aria.model.DepartmentDetails;
import com.aria.model.FinClose2;
import com.aria.model.Organisation;
import com.aria.model.PShifts;
import com.aria.model.QtyMain;
import com.aria.model.RSubHead;
import com.aria.model.ReptGrp;
import com.aria.model.ReptName;
import com.aria.model.User;
import com.aria.utils.DateUtil;
import com.aria.utils.IdConstants;
import com.aria.utils.ReportUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
public class ReportService {

    public static final Logger logger = LoggerFactory.getLogger(ReportService.class);


    @Autowired
    DateUtil dateUtil;

    @Autowired
    ReportUtils reportUtils;

    @Autowired
    UnitReportProductsRepository unitReportProductsRepository;

    @Autowired
    UnitReportFinalProductRepository unitReportFinalProductRepository;

    @Autowired
    UnitReportTableMapRepository unitReportTableMapRepository;


    @Autowired
    JournalRepository journalRepository;

    @Autowired
    BRMProductsRepository brmProductsRepository;

    @Autowired
    BinstockAuditLogRepository binstockAuditLogRepository;

    @Autowired
    JournalEntryRepository journalEntryRepository;

    @Autowired
    DayEndStockClosingSummaryRepository dayEndStockClosingSummaryRepository;

    @Autowired
    ProductMasterRepository productMasterRepository;


    @Autowired
    BinStockRepository binStockRepository;


    @Autowired
    PShiftsRepository pShiftsRepository;

    @Autowired
    DepartmentDetailsRepository departmentRepository;

    @Autowired
    BranchRepository branchRepository;

    @Autowired
    BinStockLogRepository binStockLogRepository;

    @Autowired
    OrganisationRepository organisationRepository;

    @Autowired
    FinishedGoodsMappingDataRepository finishedGoodsMappingDataRepository;
    
    @Autowired
    FinCloseRepository finCloseRepository;
    
    @Autowired
    FinClose1Repository finClose1Repository;
    
    @Autowired
    FinClose2Repository finClose2Repository;
    
    @Autowired
    ReptNameRepository reptNameRepository;
    
    @Autowired
    ReptGrpRepository reptGrpRepository;
    
    @Autowired
    RSubHeadRepository rSubHeadRepository;
    
    @Autowired
    StkOilListRepository stkOilListRepository;
    public ResponseEntity<?> getUnitReport(User loggeduser, String type, Long branch, String fromDate, String toDate, Long dept) throws UsernameNotFoundException {

        Date start = null;
        Date end = null;

        if (fromDate != null)
            start = dateUtil.getStringTODateWk(fromDate);

        if (toDate != null)
            end = dateUtil.getStringTODateWk(toDate);


        DayEndClosingReportDto closingReportDto = new DayEndClosingReportDto();

        CompanyList companyList = organisationRepository.findByFirm(loggeduser.getOrg_id());
        if (companyList != null) {
            closingReportDto.setFirmDetails(new Company(companyList));
        }
        BranchAddressList branchAddressList1 = branchRepository.getBranchId(branch);
        if (branchAddressList1 != null) {
            closingReportDto.setPlantDetails(new BranchAddress(branchAddressList1));
        }

        Map<String, Object> stringObjectMap = new LinkedHashMap<>();

        List<UnitReportProductsList> items = null;
        List<Long> itemIds = null;
        List<Long> journalIds = new ArrayList<>();

        List<UnitReportClosingDto> closingDtoList = new ArrayList<>();
        List<UnitReportClosingOilDto> oilClosingDtoList = new ArrayList<>();
        List<UnitReportClosingSummaryDto> summaryDtoList = new ArrayList<>();

        UnitReportMapDTO unitReportMapDTO = null;

        if (type.equalsIgnoreCase("SESAMEOIL")) {

            UnitReportMapList unitReportMapLists = unitReportTableMapRepository.findBy(1L);
            unitReportMapDTO = new UnitReportMapDTO(unitReportMapLists);

        } else if (type.equalsIgnoreCase("GROUNDNUTOIL")) {

            UnitReportMapList unitReportMapLists = unitReportTableMapRepository.findBy(2L);
            unitReportMapDTO = new UnitReportMapDTO(unitReportMapLists);

        }

        journalIds.add(unitReportMapDTO.getJournalId());

        if (type.equalsIgnoreCase("SESAMEOIL")) {

            itemIds = unitReportProductsRepository.findByInput(1L);
            items = unitReportProductsRepository.findByInputProducts(1L, "Y");

        } else if (type.equalsIgnoreCase("GROUNDNUTOIL")) {

            itemIds = unitReportProductsRepository.findByInput(2L);
            items = unitReportProductsRepository.findByInputProducts(2L, "Y");

        }

        System.out.println("itemids == " + itemIds);
        System.out.println("start == " + start);
        System.out.println("end == " + end);
        System.out.println("unitReportMapDTO.getRotaryJournalId() == " + unitReportMapDTO.getRotaryJournalId());
        System.out.println("unitReportMapDTO.getJournalId() == " + unitReportMapDTO.getJournalId());

        Map<String, Object> inputProductMap = new LinkedHashMap<>();

        inputProductMap.put("headers", reportUtils.getUnitReportSeasameInputMap());

        List<DayEndStockList> openList = binstockAuditLogRepository.findBY(branch, "OPENING", itemIds, start, end);
        Map<Long, Double> openMap = openList.stream().collect(Collectors.toMap(tuple -> tuple.getItemId(), tuple -> tuple.getQty()));
        System.out.println("openMap == " + openMap);
        List<DayEndStockList> closeList = binstockAuditLogRepository.findBY(branch, "CLOSING", itemIds, start, end);
        Map<Long, Double> closeMap = closeList.stream().collect(Collectors.toMap(tuple -> tuple.getItemId(), tuple -> tuple.getQty()));
        System.out.println("closeMap == " + closeMap);
        List<DayEndStockList> partList = binstockAuditLogRepository.findBY(branch, "Stock Inward", itemIds, start, end);
        Map<Long, Double> partMap = partList.stream().collect(Collectors.toMap(tuple -> tuple.getItemId(), tuple -> tuple.getQty()));
        System.out.println("partMap == " + partMap);
        List<DayEndStockList> purchaseReturnList = binstockAuditLogRepository.findBY(branch, "PurchaseReturn", itemIds, start, end);
        Map<Long, Double> purchaseReturnMap = purchaseReturnList.stream().collect(Collectors.toMap(tuple -> tuple.getItemId(), tuple -> tuple.getQty()));
        System.out.println("purchaseReturnMap == " + purchaseReturnMap);
        List<DayEndStockList> shortageList = binstockAuditLogRepository.findBY(branch, "Shortage", itemIds, start, end);
        Map<Long, Double> shortageMap = shortageList.stream().collect(Collectors.toMap(tuple -> tuple.getItemId(), tuple -> tuple.getQty()));
        System.out.println("shortageMap == " + shortageMap);
        List<DayEndStockList> excessList = binstockAuditLogRepository.findBY(branch, "Excess", itemIds, start, end);
        Map<Long, Double> excessMap = excessList.stream().collect(Collectors.toMap(tuple -> tuple.getItemId(), tuple -> tuple.getQty()));
        System.out.println("excessMap == " + excessMap);
        List<DayEndStockList> receiveList = binstockAuditLogRepository.findBYUniTReceipt(branch,"R", "MATERIAL_TRANSACTION", itemIds, start, end);
        Map<Long, Double> receiveMap = receiveList.stream().collect(Collectors.toMap(tuple -> tuple.getItemId(), tuple -> tuple.getQty()));
        System.out.println("receiveMap == " + receiveMap);

        List<DayEndStockList> issueList = binstockAuditLogRepository.findBYUnitIssue(branch,"I", "MATERIAL_TRANSACTION",itemIds, start, end);
        Map<Long, Double> issueMap = issueList.stream().collect(Collectors.toMap(tuple -> tuple.getItemId(), tuple -> tuple.getQty()));
        System.out.println("issueMap == " + issueMap);

        List<DayEndStockList> isssueQcList = binStockLogRepository.findBYQcWithOutJournals(branch, 4L, "PRODUCTION", "I", itemIds, start, end,unitReportMapDTO.getRotaryJournalId());
        Map<Long, Double> isssueQcMap = isssueQcList.stream().collect(Collectors.toMap(tuple -> tuple.getItemId(), tuple -> tuple.getQty()));
        System.out.println("isssueQcMap == " + isssueQcMap);

        List<DayEndStockList> receiptQcList = binStockLogRepository.findBYQcWithOutJournal(branch, 4L, "PRODUCTION", "R", itemIds, start, end);
        Map<Long, Double> receiptQcMap = receiptQcList.stream().collect(Collectors.toMap(tuple -> tuple.getItemId(), tuple -> tuple.getQty()));
        System.out.println("receiptQcMap == " + receiptQcMap);

        List<DayEndStockList> isssueRotaryList = binStockLogRepository.findBYRotary(branch, "PRODUCTION", "I", itemIds, unitReportMapDTO.getRotaryJournalId(), start, end);
        Map<Long, Double> isssueRotaryMap = isssueRotaryList.stream().collect(Collectors.toMap(tuple -> tuple.getItemId(), tuple -> tuple.getQty()));
        System.out.println("isssueRotaryMap == " + isssueRotaryMap);
        List<DayEndStockList> receiptRotaryList = binStockLogRepository.findBYRotary(branch, "PRODUCTION", "R", itemIds, unitReportMapDTO.getRotaryJournalId(), start, end);
        Map<Long, Double> receiptRotaryMap = receiptRotaryList.stream().collect(Collectors.toMap(tuple -> tuple.getItemId(), tuple -> tuple.getQty()));
        System.out.println("receiptRotaryMap == " + receiptRotaryMap);
        
        List<DayEndStockList> issueToDespatch = binStockLogRepository.findBYCrushing(branch, "DISPATCH", "I", itemIds, start, end);
        Map<Long, Double> issueToDespatchMap = issueToDespatch.stream().collect(Collectors.toMap(tuple -> tuple.getItemId(), tuple -> tuple.getQty()));


        if (items != null && !items.isEmpty()) {

            for (UnitReportProductsList item : items) {

                UnitReportClosingDto closingDto = new UnitReportClosingDto();
                closingDto.setItemId(item.getProductId());
                closingDto.setItemName(item.getProductName());

                Long itemId = item.getProductId();

                if (openMap.containsKey(itemId)) {
                    closingDto.setOpeningQty(convertQty(itemId,openMap.get(itemId)));
                }

                if (partMap.containsKey(itemId)) {
                    closingDto.setReceipt_party_qty(convertQty(itemId,partMap.get(itemId)));
                }

                if (receiveMap.containsKey(itemId)) {
                    closingDto.setReceipt_other_plant_qty(convertQty(itemId,receiveMap.get(itemId)));
                }

                if (excessMap.containsKey(itemId)) {
                    closingDto.setReceipt_excess_qty(convertQty(itemId,excessMap.get(itemId)));
                }

                if (purchaseReturnMap.containsKey(itemId)) {
                    closingDto.setIssue_return_qty(convertQty(itemId,purchaseReturnMap.get(itemId)));
                }

                if (isssueQcMap.containsKey(itemId)) {
                    closingDto.setIssue_qc_qty(convertQty(itemId,isssueQcMap.get(itemId)));
                }

                if (receiptQcMap.containsKey(itemId)) {
                    closingDto.setReceipt_qc_qty(convertQty(itemId,receiptQcMap.get(itemId)));
                }

                if (issueMap.containsKey(itemId)) {
                    closingDto.setIssue_other_plant_qty(convertQty(itemId,issueMap.get(itemId)));
                }

                if (shortageMap.containsKey(itemId)) {
                    closingDto.setShortage_qty(convertQty(itemId,shortageMap.get(itemId)));
                }

                if (closeMap.containsKey(itemId)) {
                    closingDto.setClosingQty(convertQty(itemId,closeMap.get(itemId)));
                }

                if (isssueRotaryMap.containsKey(itemId)) {
                    closingDto.setIssue_rotary_qty(convertQty(itemId,isssueRotaryMap.get(itemId)));
                }

                if (receiptRotaryMap.containsKey(itemId)) {
                    closingDto.setReceipt_rotary_qty(convertQty(itemId,receiptRotaryMap.get(itemId)));
                }
                if (issueToDespatchMap.containsKey(itemId)) {
                    closingDto.setIssue_sales_qty(convertQty(itemId,issueToDespatchMap.get(itemId)));
                }

                Double crushingQty = journalEntryRepository.issueQty(journalIds, itemId, start, end,branch);

                closingDto.setCrushingQty(convertQty(itemId,crushingQty));

                closingDtoList.add(closingDto);
            }

        }

        inputProductMap.put("inputProduct", closingDtoList);
        closingReportDto.setInputProducts(inputProductMap);

        Map<String, Object> outputProductMap = new LinkedHashMap<>();
        outputProductMap.put("headers", reportUtils.getUnitReportSeasameOutPutMap());

        List<Long> oilItemIds = null;
        List<UnitReportProductsList> oilItems = null;
        List<Long> oilJournalIds = new ArrayList<>();


        if (type.equalsIgnoreCase("SESAMEOIL")) {

            oilItemIds = unitReportProductsRepository.findByOutput(1L);
            oilItems = unitReportProductsRepository.findByInputProducts(1L, "N");

            oilJournalIds.add(unitReportMapDTO.getJournalId());

        } else if (type.equalsIgnoreCase("GROUNDNUTOIL")) {

            oilItemIds = unitReportProductsRepository.findByOutput(2L);
            oilItems = unitReportProductsRepository.findByInputProducts(2L, "N");

            oilJournalIds.add(unitReportMapDTO.getJournalId());
        }


        List<DayEndStockList> oilOpenList = binstockAuditLogRepository.findBY(branch, "OPENING", oilItemIds, start, end);

        Map<Long, Double> oilOpenMap = oilOpenList.stream().collect(Collectors.toMap(tuple -> tuple.getItemId(),
                tuple -> tuple.getQty()));

        List<DayEndStockList> oilCloseList = binstockAuditLogRepository.findBY(branch, "CLOSING", oilItemIds, start, end);
        Map<Long, Double> oilCloseMap = oilCloseList.stream().collect(Collectors.toMap(tuple -> tuple.getItemId(),
                tuple -> tuple.getQty()));

        List<DayEndStockList> oilReceiveList = binstockAuditLogRepository.findBYUniTReceipt(branch,"R", "MATERIAL_TRANSACTION",oilItemIds, start, end);
        Map<Long, Double> oilReceiveMap = oilReceiveList.stream().collect(Collectors.toMap(tuple -> tuple.getItemId(),
                tuple -> tuple.getQty()));

        List<DayEndStockList> oilIssueList = binstockAuditLogRepository.findBYUnitIssue(branch,"I", "MATERIAL_TRANSACTION",oilItemIds, start, end);
        Map<Long, Double> oilIssueMap = oilIssueList.stream().collect(Collectors.toMap(tuple -> tuple.getItemId(),
                tuple -> tuple.getQty()));

        List<DayEndStockList> oilReceiptRotaryList = binStockLogRepository.findBYRotary(branch, "PRODUCTION", "R", oilItemIds, unitReportMapDTO.getRotaryJournalId(), start, end);
        Map<Long, Double> oilReceiptRotaryMap = oilReceiptRotaryList.stream().collect(Collectors.toMap(tuple -> tuple.getItemId(), tuple -> tuple.getQty()));

        List<DayEndStockList> oilReceiptCrushingList = binStockLogRepository.findBYCrushing(branch, "PRODUCTION", "R", oilItemIds, start, end);
        Map<Long, Double> oilReceiptCrushingMap = oilReceiptCrushingList.stream().collect(Collectors.toMap(tuple -> tuple.getItemId(), tuple -> tuple.getQty()));

        List<DayEndStockList> oilIsssuePackinglist = binStockLogRepository.findBYPackingList(branch,"PRODUCTION", "I", oilItemIds, start, end , unitReportMapDTO.getPackJournalId());
        Map<Long, Double> oilIsssuePackingMap = oilIsssuePackinglist.stream().collect(Collectors.toMap(tuple -> tuple.getItemId(), tuple -> tuple.getQty()));

        List<DayEndStockList> oilIssueToProduction = binStockLogRepository.findBYCrushing(branch, "PRODUCTION", "I", oilItemIds, start, end);
        Map<Long, Double> oilIssueToProductionMap = oilIssueToProduction.stream().collect(Collectors.toMap(tuple -> tuple.getItemId(), tuple -> tuple.getQty()));

        List<DayEndStockList> oilIssueToDespatch = binStockLogRepository.findBYCrushing(branch, "DISPATCH", "I", oilItemIds, start, end);
        Map<Long, Double> oilIssueToDespatchMap = oilIssueToDespatch.stream().collect(Collectors.toMap(tuple -> tuple.getItemId(), tuple -> tuple.getQty()));

        List<DayEndStockList> oilReceiptPackinglist = binStockLogRepository.findBYPackingList(branch, "PRODUCTION", "R", oilItemIds, start, end , unitReportMapDTO.getPackJournalId());
        Map<Long, Double> oiiReceiptPackingMap = oilReceiptPackinglist.stream().collect(Collectors.toMap(tuple -> tuple.getItemId(), tuple -> tuple.getQty()));
        Double oilIsssuePackinglistSesame=0.0;
        Double oilReceiptPackinglistSesame=0.0;
        Long packingItemId = type.equalsIgnoreCase("SESAMEOIL") ? 777L : 778L;   
         oilIsssuePackinglistSesame = binStockLogRepository.findBYPackingListSesame(branch,"PRODUCTION", "I", packingItemId, start, end , 10L );
         oilReceiptPackinglistSesame = binStockLogRepository.findBYPackingListSplit(branch, packingItemId, start, end  );
        
        	 
        Set<Long> specialItems = new HashSet<>(Arrays.asList(777L, 778L));
        if (oilItems != null && !oilItems.isEmpty()) {

            for (UnitReportProductsList item : oilItems) {

                UnitReportClosingOilDto oilClosingDto = new UnitReportClosingOilDto();
                oilClosingDto.setItemId(item.getProductId());
                oilClosingDto.setItemName(item.getProductName());

                Long itemId = item.getProductId();

                System.out.println("oil itemId == " + itemId);
                System.out.println("oil itemName() == " + item.getProductName());

                if (oilOpenMap.containsKey(itemId)) {
                    oilClosingDto.setOpeningQty(oilOpenMap.get(itemId));
                }
                if (oilCloseMap.containsKey(itemId)) {
                    oilClosingDto.setClosingQty(oilCloseMap.get(itemId));
                }
                if (oilReceiveMap.containsKey(itemId)) {
                    oilClosingDto.setReceipt_from_other_plant_qty(oilReceiveMap.get(itemId));
                }
                if (oilIssueMap.containsKey(itemId)) {
                    oilClosingDto.setIssue_from_other_plant_qty(oilIssueMap.get(itemId));
                }
                boolean isSpecialItem = itemId != null && itemId.equals(packingItemId);
                oilClosingDto.setIssue_from_packing_qty(isSpecialItem ? oilReceiptPackinglistSesame: oiiReceiptPackingMap.getOrDefault(itemId, 0.0));

                oilClosingDto.setIssue_to_packing_qty(isSpecialItem ? oilIsssuePackinglistSesame : oilIsssuePackingMap.getOrDefault(itemId, 0.0));
                
                if (oilReceiptRotaryMap.containsKey(itemId)) {
                    oilClosingDto.setReceipt_from_rotary_qty(oilReceiptRotaryMap.get(itemId));
                }
                if (oilReceiptCrushingMap.containsKey(itemId)) {
                    oilClosingDto.setReceipt_from_crushing_qty(oilReceiptCrushingMap.get(itemId));
                }                             
                if (oilIssueToDespatchMap.containsKey(itemId)) {
                    oilClosingDto.setIssue_to_dispatch_qty(oilIssueToDespatchMap.get(itemId));
                }
                if (Long.valueOf(5758).equals(itemId)
                        && oilReceiptCrushingMap.containsKey(itemId)) {

                    oilClosingDto.setDryageQty(oilReceiptCrushingMap.get(itemId));
                }
                Double productionQty = journalEntryRepository.issueQty(oilJournalIds, itemId, start, end,branch);

                oilClosingDto.setIssue_from_production(productionQty);

                Double crushingQty = journalEntryRepository.receiveQty(oilJournalIds, itemId, start, end,branch);

                oilClosingDto.setCrushingQty(crushingQty);
                
                oilClosingDtoList.add(oilClosingDto);

            }

        }

        outputProductMap.put("outputProduct", oilClosingDtoList);
        closingReportDto.setOutputProducts(outputProductMap);

        Map<String, Object> summaryProductMap = new LinkedHashMap<>();

        summaryProductMap.put("headers", reportUtils.getUnitReportSeasameSummaryMap());

        List<Long> summaryItemIds = null;
        List<DayEndStockList> summaryItems = null;
        List<Long> summaryJournalIds = null;

        if (type.equalsIgnoreCase("SESAMEOIL")) {

            summaryItemIds = Stream.of(IdConstants.sesameSummaryProductId.split("\\,")).map(Long::parseLong).collect(Collectors.toList());
            summaryItems = brmProductsRepository.findBY(summaryItemIds);

        } else if (type.equalsIgnoreCase("GROUNDNUTOIL")) {

            summaryItemIds = Stream.of(IdConstants.groundnutSummaryProductId.split("\\,")).map(Long::parseLong).collect(Collectors.toList());
            summaryItems = brmProductsRepository.findBY(summaryItemIds);

        }
        Date startMonthfirstDate = dateUtil.getStartMonthDay(start);
        System.out.println("== startMonthfirstDate == " + startMonthfirstDate);
        if (summaryItems != null && !summaryItems.isEmpty()) {

            for (DayEndStockList item : summaryItems) {

                UnitReportClosingSummaryDto oilClosingDto = new UnitReportClosingSummaryDto();
                oilClosingDto.setItemId(item.getItemId());
                oilClosingDto.setItemName(item.getItemName());

                Long itemId = item.getItemId();
                System.out.println("summaryItems itemId == " + itemId);
                System.out.println("summaryItems itemName() == " + item.getItemName());

                Double productionQty = journalEntryRepository.issueQty(oilJournalIds, itemId, startMonthfirstDate, end,branch);
                //Double productionQty = journalEntryRepository.issueQtyWithOutJournal( itemId, startMonthfirstDate, end);

                oilClosingDto.setCrushing_issue_Qty(convertQty(itemId,productionQty));

                Double crushingQty = journalEntryRepository.receiveQty(oilJournalIds, itemId, startMonthfirstDate, end,branch);
                //Double crushingQty = journalEntryRepository.receiveQtyWithOutJournal(itemId, startMonthfirstDate, end);

                oilClosingDto.setCrushing_receive_Qty(convertQty(itemId,crushingQty));
                List<Long> rotaryJournal= new ArrayList();
                rotaryJournal.add(unitReportMapDTO.getRotaryJournalId());
                Double productionRotaryQty = journalEntryRepository.issueQty(rotaryJournal, itemId, startMonthfirstDate, end,branch);
                oilClosingDto.setRotary_issue_Qty(convertQty(itemId,productionRotaryQty));
                Double crushingRotaryQty = journalEntryRepository.receiveQty(rotaryJournal, itemId, startMonthfirstDate, end,branch);
                oilClosingDto.setRotary_receive_Qty(convertQty(itemId,crushingRotaryQty));
                summaryDtoList.add(oilClosingDto);
            }


            closingReportDto.setSummaryList(summaryDtoList);
        }

        System.out.println("== start == " + start);
        System.out.println("== end == " + end);
        List<UnitReportMapList> headername = new ArrayList<>();
        if (type.equalsIgnoreCase("SESAMEOIL")) {

        	  headername = unitReportFinalProductRepository.findBySjid(unitReportMapDTO.getJournalId());

        } else if (type.equalsIgnoreCase("GROUNDNUTOIL")) {

        	 headername = unitReportFinalProductRepository.findBySjid(unitReportMapDTO.getJournalId());

        }
        List<OilPackedDataDto> tableData = new ArrayList<>();

        if (headername != null && !headername.isEmpty()) {

            for (UnitReportMapList summary : headername) {

                if (summary.getProductId1() == null) {
                    continue; // safety check
                }

                OilPackedDataDto dto = new OilPackedDataDto();
                dto.setId(summary.getProductId1());
                dto.setTableName(summary.getProductName1());

                oilPackedDetail(dto, summary, start, end, branch, loggeduser);

                tableData.add(dto);
            }
        }

        closingReportDto.setPackingData(tableData);
        
//        List<PackingList> packingLists = journalEntryRepository.getPackingDetails(10L, branch, start, end);
//
//        List<UnitReportPackingDto> packingDtos = packingLists.stream().map(data -> new UnitReportPackingDto(data)).collect(Collectors.toList());

//        closingReportDto.setPackingList(tableData);

        return new ResponseEntity<DayEndClosingReportDto>(closingReportDto, HttpStatus.OK);
    }

    private void oilPackedDetail(OilPackedDataDto dto, UnitReportMapList summary, Date start, Date end,
			Long branch, User loggeduser) {
    	List<Long> pId = stkOilListRepository.findByRefid(summary.getProductId1());
    	List<ByeProductDtoList> list =
                binStockLogRepository.findBYPackingLists(
                        branch,
                        null,
                        "PRODUCTION",
                        loggeduser.getOrg_id(),
                        start,
                        end,
                        pId);

        List<UnitReportPackingDto> collect = list.stream()
                .map(UnitReportPackingDto::new)
                .collect(Collectors.toList());

        dto.setPackingList(collect); 
	}
    private static final Set<Long> BAG_CONVERSION_IDS = 
            new HashSet<>(Arrays.asList(1040L, 29938L, 770L));

    private Double convertQty(Long itemId, Double qty) {

        if (qty == null) {
            return 0.0;
        }

        if (itemId != null && BAG_CONVERSION_IDS.contains(itemId)) {
            return qty / 74;
        }

        return qty;
    }

	public ResponseEntity<?> stockPaperProductList(Long branchId, Long deptId, String type) throws UsernameNotFoundException {
        List<Long> itemIds = null;
        List<StockPaperItems> list = null;
        if (type.equalsIgnoreCase("AL")) {
            list = binStockRepository.getStockPaperAllItems(branchId, deptId);
        } else {
            itemIds = productMasterRepository.findBYAllProductIds(type);
            list = binStockRepository.getStockPaperItems(itemIds, branchId, deptId);
        }
        if (!list.isEmpty()) {
            List<StockPaperItemsDto> result = list.stream().map(data -> {
                StockPaperItemsDto stockPaperDto = new StockPaperItemsDto(data);
                return stockPaperDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<StockPaperItemsDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<StockPaperItemsDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    public ResponseEntity<?> stockPaperReport(Long branchId, Long deptId, Long shiftId, Date shiftDate,
                                              List<StockPaperProducts> productsWithLot) throws UsernameNotFoundException {

        StockPaperResult stockPaperResult = new StockPaperResult();
        Optional<PShifts> pShifts = pShiftsRepository.findById(shiftId);
        if (pShifts.isPresent()) {
            stockPaperResult.setShiftName(pShifts.get().getName());
            stockPaperResult.setShiftId(pShifts.get().getId());
            stockPaperResult.setShiftDate(dateUtil.getDateTOStringWk(dateUtil.getISTDate()));
        }
        Optional<Branch> branchOptional = branchRepository.findById(branchId);
        if (branchOptional.isPresent()) {
            stockPaperResult.setBranchName(branchOptional.get().getBranchName());
        }

        Optional<DepartmentDetails> detailsOptional = departmentRepository.findById(deptId);
        if (detailsOptional.isPresent()) {
            stockPaperResult.setDeptName(detailsOptional.get().getName());
        }

        List<StockPaperDto> stockPaperDtoList = new ArrayList<>();
        List<Long> binNoList = new ArrayList<>();
        for (StockPaperProducts product : productsWithLot) {

            List<Long> binNo1 = binStockRepository.getStockPaperBinList(product.getProductId(), product.getLot(), branchId, deptId);
            binNoList.addAll(binNo1);
            List<Long> binNo2 = binStockLogRepository.getStockPaperBinList1(product.getProductId(), product.getLot(), branchId, deptId, shiftDate);
            binNoList.addAll(binNo2);
            Set<Long> distinctList = new HashSet<>(binNoList);
            for (Long binNo : distinctList) {

                StockPaper stockPaper = binStockRepository.getStockPaperItems(binNo, product.getProductId(), product.getLot(), branchId, deptId);
                if (stockPaper != null) {
                    StockPaperDto stockPaperDto = new StockPaperDto(stockPaper);
                    List<StockPaper> issueList = binStockLogRepository.getStockPaperIssueItems(binNo, product.getProductId(), product.getLot(), branchId, deptId, shiftDate, "I");
                    if (!issueList.isEmpty()) {
                        List<StockPaperIssueReceipts> issues = issueList.stream().map(data -> new StockPaperIssueReceipts(data)).collect(Collectors.toList());
                        stockPaperDto.setReceiptsList(issues);
                    }

                    List<StockPaper> reciptList = binStockLogRepository.getStockPaperIssueItems(binNo, product.getProductId(), product.getLot(), branchId, deptId, shiftDate, "R");
                    if (!reciptList.isEmpty()) {
                        List<StockPaperIssueReceipts> receiptsList = reciptList.stream().map(data -> new StockPaperIssueReceipts(data)).collect(Collectors.toList());
                        stockPaperDto.setReceiptsList(receiptsList);
                    }
                    stockPaperDtoList.add(stockPaperDto);
                }


            }
        }

        stockPaperResult.setStockPapers(stockPaperDtoList);

        return new ResponseEntity<StockPaperResult>(stockPaperResult, HttpStatus.OK);

    }


    public ResponseEntity<?> getFinishedGoodsSummary(User loggedUser, Long branchId, String dateValue) {

        Date date = null;
        try {

            if (dateValue != null) {
                date = dateUtil.getStringTODateWk(dateValue);
            }
           
            
            Map<String, Object> resultMap = new HashMap<>();
            updateHeaderDetails(resultMap, loggedUser.getOrg_id(), branchId);

            List<FinishedGoodsSummary> finishedGoodsSummaryList = finishedGoodsMappingDataRepository.getFinishedGoodsSummaryTableList();
            if (finishedGoodsSummaryList != null && !finishedGoodsSummaryList.isEmpty()) {

                List<Map<String, Object>> tableMap = new ArrayList<>();
                for (FinishedGoodsSummary summary : finishedGoodsSummaryList) {
                    Map<String, Object> summaryMap = new HashMap<>();
                    summaryMap.put("tableName", summary.getName());
                    summaryMap.put("id", summary.getId());
                    if(branchId !=0) {
                    finishedGoodsSummaryTableData(summaryMap, summary, branchId, date);
                    }else {
                    finishedGoodsSummaryTableDataByAllBranch(summaryMap, summary, date,loggedUser);
                    }
                    tableMap.add(summaryMap);
                }

                resultMap.put("tableData", tableMap);

            }

            return new ResponseEntity<Map<String, Object>>(resultMap, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    public void finishedGoodsSummaryTableData(Map<String, Object> table,
            FinishedGoodsSummary summary,
            Long branchId,
            Date date) {

    	logger.info("finishedGoodsSummaryTableData - tableName: {}, branchId: {}, date: {}",
    			summary.getName(), branchId, date);

    	try {

	List<FinishedGoodsSummary> headerList =
			finishedGoodsMappingDataRepository.getFinishedGoodsSummaryProductNameList(summary.getId());

	List<FinishedGoodsSummary> productTypeList =
			finishedGoodsMappingDataRepository.getFinishedGoodsSummaryProductTypeNameList(summary.getId());

	logger.info("headerList size: {}", headerList.size());
	logger.info("productTypeList size: {}", productTypeList.size());

	List<Long> productIds = productTypeList.stream()
			.map(FinishedGoodsSummary::getProductId)
			.collect(Collectors.toList());

	List<DayEndStockList> closingStock =
			dayEndStockClosingSummaryRepository.getClosingStock(branchId, productIds, date);

	Map<Long, Double> closingMap = closingStock.stream()
			.collect(Collectors.toMap(DayEndStockList::getItemId, DayEndStockList::getQty));

	List<DayEndStockList> lotStock =
			binstockAuditLogRepository.findByLot(productIds, branchId, date);

	Map<Long, Double> lotMap = lotStock.stream()
			.collect(Collectors.toMap(DayEndStockList::getItemId, DayEndStockList::getLot));

	List<Map<String, Object>> headerData = new ArrayList<>();

	for (FinishedGoodsSummary header : headerList) {

		Map<String, Object> headerMap = new HashMap<>();
		headerMap.put("itemHeaderName", header.getName());
		headerMap.put("orderNo", header.getOrderNo());

		List<Map<String, Object>> typeData = new ArrayList<>();

		List<FinishedGoodsSummary> filteredTypes = productTypeList.stream()
				.filter(x -> x.getSName() != null && x.getSName().contains(header.getName()))
				.collect(Collectors.toList());

		for (FinishedGoodsSummary item : filteredTypes) {

			Map<String, Object> data = new HashMap<>();

			Long itemId = item.getProductId();

			Double qty = closingMap.getOrDefault(itemId, 0.0);
			Double lot = lotMap.getOrDefault(itemId, 0.0);

			Double total = qty * lot;
			
			Long pds = item.getPds() == null ? 0 : item.getPds();

			data.put("itemTypeName", item.getName());
			data.put("orderNo", item.getOrderNo());
			data.put("plant" + branchId, qty);
			data.put("total", total);
			data.put("pds", pds);
			data.put("noOfDays", (total == 0 || pds == 0) ? 0 : total / pds);

			typeData.add(data);
		}

		headerMap.put("itemTypeWithDetails", typeData);

		headerData.add(headerMap);
	}

	table.put("itemHeader", headerData);

	logger.info("finishedGoodsSummaryTableData - headerData size: {}", headerData.size());

	} catch (Exception e) {
		logger.error("Error in finishedGoodsSummaryTableData", e);
		throw new RuntimeException(e);
	}
    }
    
    
    public void finishedGoodsSummaryTableDataByAllBranch(Map<String, Object> table,FinishedGoodsSummary summary,Date date, User loggedUser) {

        logger.info("finishedGoodsSummaryTableData - tableName: {}, date: {}",
                summary.getName(), date);

        try {

            List<FinishedGoodsSummary> headerList =
                    finishedGoodsMappingDataRepository.getFinishedGoodsSummaryProductNameList(summary.getId());

            List<FinishedGoodsSummary> productTypeList =
                    finishedGoodsMappingDataRepository.getFinishedGoodsSummaryProductTypeNameList(summary.getId());

            List<Long> productIds = productTypeList.stream()
                    .map(FinishedGoodsSummary::getProductId)
                    .collect(Collectors.toList());

            List<DayEndStockList> closingStock =
                    dayEndStockClosingSummaryRepository.getClosingStockforall( productIds, date);

            List<DayEndStockList> lotStock =
                    binstockAuditLogRepository.findByLotForall(productIds, date);
         
            Map<String, Double> closingMap = new HashMap<String, Double>();
            Map<String, Double> lotMap = new HashMap<String, Double>();

            Set<Long> branchIds = new HashSet<>(
                    branchRepository.findByBranch(loggedUser.getOrg_id())
            );
            
           
            for (DayEndStockList d : closingStock) {
                String key = d.getItemId() + "_" + d.getBranchId();
                Double qty = d.getQty() == null ? 0.0 : d.getQty();

                if (closingMap.containsKey(key)) {
                    closingMap.put(key, closingMap.get(key) + qty);
                } else {
                    closingMap.put(key, qty);
                }               
            }      
            for (DayEndStockList d : lotStock) {
                String key = d.getItemId() + "_" + d.getBranchId();
                Double lot = d.getLot() == null ? 0.0 : d.getLot();

                if (lotMap.containsKey(key)) {
                    lotMap.put(key, lotMap.get(key) + lot);
                } else {
                    lotMap.put(key, lot);
                }
                            }
            List<Map<String, Object>> headerData = new ArrayList<Map<String, Object>>();

            for (FinishedGoodsSummary header : headerList) {

                Map<String, Object> headerMap = new HashMap<String, Object>();
                headerMap.put("itemHeaderName", header.getName());
                headerMap.put("orderNo", header.getOrderNo());
                List<Map<String, Object>> typeData = new ArrayList<Map<String, Object>>();
                List<FinishedGoodsSummary> filteredTypes = productTypeList.stream()
                        .filter(x -> x.getSName() != null && x.getSName().contains(header.getName()))
                        .collect(Collectors.toList());
                for (FinishedGoodsSummary item : filteredTypes) {
                    Map<String, Object> data = new HashMap<String, Object>();
                    Long itemId = item.getProductId();
                    Double total = 0.0;
                    for (Long brId : branchIds) {
                        String key = itemId + "_" + brId;
                        Double qty = closingMap.containsKey(key) ? closingMap.get(key) : 0.0;
                        Double lot = lotMap.containsKey(key) ? lotMap.get(key) : 0.0;
                        Double branchTotal = qty * lot;
                        data.put("plant" + brId, qty);
                        total += branchTotal;
                    }
                    Long pds = item.getPds() == null ? 0 : item.getPds();
                    data.put("itemTypeName", item.getName());
                    data.put("orderNo", item.getOrderNo());
                    data.put("total", total);
                    data.put("pds", pds);
                    data.put("noOfDays", (total == 0 || pds == 0) ? 0 : total / pds);
                    typeData.add(data);
                }

                headerMap.put("itemTypeWithDetails", typeData);
                headerData.add(headerMap);
            }

            table.put("itemHeader", headerData);

        } catch (Exception e) {
            logger.error("Error in finishedGoodsSummaryTableData", e);
            throw new RuntimeException(e);
        }
    }

    
    
    private void updateHeaderDetails(Map<String, Object> resultMap, Long firmId, Long branchId) {

        //  Firm Details
        CompanyList companyList = organisationRepository.findByFirm(firmId);

        if (companyList != null) {
            Map<String, Object> firmMap = new HashMap<>();
            firmMap.put("id", companyList.getId());
            firmMap.put("name", companyList.getName());
            firmMap.put("address", companyList.getAddress());
            firmMap.put("city", companyList.getCity());
            firmMap.put("state", companyList.getState());
            firmMap.put("pinCode", companyList.getPinCode());
            firmMap.put("phoneNo", companyList.getPhoneNo());
            firmMap.put("email", companyList.getEmail());
            firmMap.put("fax", companyList.getFax());
            firmMap.put("gst", companyList.getGST());

            resultMap.put("firmDetails", firmMap);
        }

        //  Branch Details
        Map<String, Object> branchMap = new HashMap<>();

        if (branchId != null && branchId != 0) {

            BranchAddressList branch = branchRepository.getBranchId(branchId);

            if (branch != null) {
                branchMap.put("branchId", branch.getBranchId());
                branchMap.put("branchName", branch.getBranchName());
                branchMap.put("address", branch.getAddress());
                branchMap.put("city", branch.getCity());
                branchMap.put("state", branch.getState());
                branchMap.put("pinCode", branch.getPinCode());
                branchMap.put("phoneNo", branch.getPhoneNo());
                branchMap.put("email", branch.getEmail());
                branchMap.put("fax", branch.getFax());
            }

        } else {
            branchMap.put("branchId", 0);
            branchMap.put("branchName", "All Branch");
        }

        resultMap.put("branchDetails", branchMap);
    }
//    public void finishedGoodsSummaryTableData(Map<String, Object> table, FinishedGoodsSummary summary, Long branchId, Date date) {
//
//        logger.info("finishedGoodsSummaryTableData - tableName: {}, productId: {}, branchId: {}, date: {}", summary.getName(), summary.getProductId(), branchId, date);
//        try {
//
//            List<FinishedGoodsSummary> summaryList = finishedGoodsMappingDataRepository.getFinishedGoodsSummaryProductNameList(summary.getId());
//            logger.info("finishedGoodsSummaryTableData - summaryList size: {}", summaryList.size());
//            List<Map<String, Object>> dataMap = new ArrayList<>();
//
//            if (summaryList != null && !summaryList.isEmpty()) {
//
//                for (FinishedGoodsSummary item : summaryList) {
//
//                    Map<String, Object> data = new HashMap<>();
//                    data.put("itemHeaderName", item.getName());
//                    data.put("orderNo", item.getOrderNo());
//
//                    finishedGoodsSummaryTableProductTypeData(data, item, summary.getId(), branchId, date);
//
//                    dataMap.add(data);
//                }
//            }
//            logger.info("finishedGoodsSummaryTableData - dataMap size: {}", dataMap.size());
//            table.put("itemHeader", dataMap);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    public void finishedGoodsSummaryTableProductTypeData(Map<String, Object> table, FinishedGoodsSummary summary, Long tableId, Long branchId, Date date) {
//        logger.info("finishedGoodsSummaryTableProductTypeData - tableName: {}, productId: {}, branchId: {}, date: {}", summary.getName(), summary.getProductId(), branchId, date);
//
//        try {
//
//            List<FinishedGoodsSummary> summaryList = finishedGoodsMappingDataRepository.getFinishedGoodsSummaryProductTypeNameList(tableId, summary.getName());
//
//            logger.info("finishedGoodsSummaryTableProductTypeData - summaryList size: {}", summaryList.size());
//
//            List<Map<String, Object>> dataMap = new ArrayList<>();
//            List<Long> productIds = summaryList.stream()
//                    .map(FinishedGoodsSummary::getProductId)
//                    .collect(Collectors.toList());
//            List<DayEndStockList> cosingStock = dayEndStockClosingSummaryRepository.getClosingStock( branchId,productIds, date);
//            Map<Long, Double> shortageMap = cosingStock.stream().collect(Collectors.toMap(tuple -> tuple.getItemId(), tuple -> tuple.getQty()));
//            List<DayEndStockList> lotStock = binstockAuditLogRepository.findByLot(productIds, branchId, date);
//            Map<Long, Double> lotMap = lotStock.stream().collect(Collectors.toMap(tuple -> tuple.getItemId(), tuple -> tuple.getLot()));
//
//            
//            
//            if (summaryList != null && !summaryList.isEmpty()) {
//                for (FinishedGoodsSummary item : summaryList) {
//
//                    Map<String, Object> data = new HashMap<>();
//                    data.put("itemTypeName", item.getName());
//                    data.put("orderNo", item.getOrderNo());
////                    finishedGoodsSummaryTableProductData(data, item, branchId, date);                  
//                   
//                    Long itemId = item.getProductId();
//
//                    Double qty = shortageMap.getOrDefault(itemId, 0.0);
//                    Double lot = lotMap.getOrDefault(itemId, 0.0);
//
//                    data.put("plant" + branchId, qty);
//
//                    Double total = qty * lot;
//                    data.put("total", total);
//
//                    Long pds = item.getPds();
//                    data.put("pds", pds == null ? 0 : pds);
//
//                    if (total == 0 || pds == null || pds == 0) {
//                        data.put("noOfDays", 0);
//                    } else {
//                        data.put("noOfDays", total / pds);
//                    }
//                    
//                    dataMap.add(data);
//                }
//            }
//
//
//            logger.info("finishedGoodsSummaryTableProductTypeData - dataMap size: {}", dataMap.size());
//            table.put("itemTypeWithDetails", dataMap);
//        } catch (Exception e) {
//            logger.error("Error in finishedGoodsSummaryTableProductTypeData", e);
//            throw new RuntimeException(e);
//        }
//    }

//    public void finishedGoodsSummaryTableProductData(Map<String, Object> table, FinishedGoodsSummary summary, Long branchId, Date date) {
//
//        logger.info("FinishedGoodsSummaryTableProductData - productId: {}, branchId: {}, date: {}", summary.getProductId(), branchId, date);
//        try {
//            Map<String, Object> data = new HashMap<>();
//
//            if (branchId == null) {
//
//                List<Long> branchIds = reportUtils.getBranchIds();
//
//                for (Long branch : branchIds) {
//
//                    if (branch == 1L) {
//                        Double qty = dayEndStockClosingSummaryRepository.getCosingStock(summary.getProductId(), branch, date);
//                        if(qty == null){
//                            data.put("plant" + branch, 0);
//                        }else {
//                            data.put("plant" + branch, qty);
//                        }
//                    } else if (branch == 2L) {
//                        Double qty = dayEndStockClosingSummaryRepository.getCosingStock(summary.getProductId(), branch, date);
//                        if(qty == null){
//                            data.put("plant" + branch, 0);
//                        }else {
//                            data.put("plant" + branch, qty);
//                        }
//                    } else if (branch == 3L) {
//                        Double qty = dayEndStockClosingSummaryRepository.getCosingStock(summary.getProductId(), branch, date);
//                        if(qty == null){
//                            data.put("plant" + branch, 0);
//                        }else {
//                            data.put("plant" + branch, qty);
//                        }
//                    }
//                }
//
//                data.put("total", 0);
//                data.put("pds", 0);
//                data.put("noOfDays", 0);
//
//            } else {
//            	System.out.print(summary.getProductId());
//            	System.out.print(date);
//            	System.out.print(branchId);
//            	  Double qty = dayEndStockClosingSummaryRepository
//            	            .getCosingStock( branchId,summary.getProductId(), date);
//
//            	    if (qty == null) {
//            	        qty = 0.0;
//            	    }
//            	    data.put("plant" + branchId, qty);
//
//            	    Double lot = binstockAuditLogRepository
//            	            .findByLot(summary.getProductId(), branchId, date);
//
//            	    if (lot == null) {
//            	        lot = 0.0;
//            	    }
//
//            	    Double total = qty * lot;
//            	    data.put("total", total);
//
//            	    Long pds = summary.getPds();
//            	    data.put("pds", pds == null ? 0 : pds);
//
//            	    if (total == 0 || pds == null || pds == 0) {
//            	        data.put("noOfDays", 0);
//            	    } else {
//            	        Double noOfDay = total / pds;
//            	        data.put("noOfDays", noOfDay);
//            	    }
//            }
//            
//          table.put("itemTypeWithStockDetails", data);
//
//        } catch (Exception e) {
//            logger.error("Error in finishedGoodsSummaryTableProductData", e);
//            logger.error("Table: " + table);
//        }
//    }
//
//    
    
    public ResponseEntity<?> getFinishedGoodsClosedSummary(User loggedUser, String dateValue, Long month, Long year) {

        Date date = null;    
       
            if (dateValue != null) {
                date = dateUtil.getStringTODateWk(dateValue);
            }
            Map<String, Object> resultMap = new HashMap<>();
            updateHeaderDetails(resultMap, loggedUser.getOrg_id(),loggedUser.getBranch_id());
            List<FinClose2> finishedGoodsClosedSummaryList = finClose2Repository.getFinishedGoodsClosedSummaryTableList();
            if (finishedGoodsClosedSummaryList != null && !finishedGoodsClosedSummaryList.isEmpty()) {
                List<Map<String, Object>> tableMap = new ArrayList<>();
                for (FinClose2 summary : finishedGoodsClosedSummaryList) {
                    Map<String, Object> summaryMap = new HashMap<>();
                    summaryMap.put("tableName", summary.getName());
                    summaryMap.put("id", summary.getId());
                    finishedGoodsClosedSummaryTableData(summaryMap, summary, date,month,year);
                    tableMap.add(summaryMap);
                }
                resultMap.put("tableData", tableMap);
                }
            return new ResponseEntity<Map<String, Object>>(resultMap, HttpStatus.OK);        
    }

    public void finishedGoodsClosedSummaryTableData(Map<String, Object> summaryMap, FinClose2 summary, Date date, Long month, Long year) {
		 
		            List<FinishedGoodsSummary> summaryList = finClose1Repository.getFinishedGoodsClosedSummaryProductNameList(summary.getId());
		            List<Map<String, Object>> dataMap = new ArrayList<>();
		            if (summaryList != null && !summaryList.isEmpty()) {
		                for (FinishedGoodsSummary item : summaryList) {
		                    Map<String, Object> data = new HashMap<>();
		                    data.put("itemHeaderName", item.getName());
		                    data.put("orderNo", item.getOrderNo());
		                    finishedGoodsClosedSummaryTableProductTypeData(data, item, summary.getId(), date,month, year);
		                    dataMap.add(data);
		                }
		            }		           
		            summaryMap.put("itemHeader", dataMap);		    
	}
    
    
    public void finishedGoodsClosedSummaryTableProductTypeData(Map<String, Object> table, FinishedGoodsSummary item2, Long tableId, Date date, Long month, Long year) {
    	List<Object[]> results ;
    	if(date != null) {
       results = dayEndStockClosingSummaryRepository.getClosedCosingStock(tableId, item2.getOrderNo(), date);
    	}else{
    		results = dayEndStockClosingSummaryRepository.getClosedCosingStockByMonth(tableId, item2.getOrderNo(), month,year);
    	}
        Map<String, Double> branchWiseMap = results.stream()
            .collect(Collectors.toMap(
                row -> "plant" + ((Number) row[0]).longValue(),
                row -> (double) Math.round(((Number) row[1]).doubleValue())
            ));       
        List<Long> branchIds = reportUtils.getBranchIds();
        branchIds.forEach(branch -> branchWiseMap.putIfAbsent("plant" + branch.longValue(), 0.0));       
        table.put("itemTypeWithStockDetails", branchWiseMap);
          }

	public ResponseEntity<?> getAllFGName() {
		
			List<ReptName> FgName = reptNameRepository.findfgname();
	    	if(!FgName.isEmpty()) {
	        List<ReptNameDto> fgNameDto = FgName.stream().map(val -> new ReptNameDto(val)).collect(Collectors.toList());
	        return new ResponseEntity<List<ReptNameDto>>(fgNameDto, HttpStatus.OK);
	    	}
	    	else {
	    		  return new ResponseEntity<List<ReptNameDto>>(new ArrayList<>(), HttpStatus.OK);
	    	}
			
		
	}
	public ResponseEntity<?> getFinishedGoodsStockSummary(
	        User user, String fromDate, String toDate,
	        Long branch, Long fgId, Long firmId) {

	    Date start = null;
	    Date end = null;

	    if (fromDate != null)
	        start = dateUtil.getStringTODateWk(fromDate);

	    if (toDate != null)
	        end = dateUtil.getStringTODateWk(toDate);

	    Map<String, Object> resultMap = new HashMap<>();

	    resultMap.put("header", reportUtils.getFgStockReportSeasameInputMap(firmId, branch));
	    updateHeaderDetails(resultMap, firmId, branch);

	    if (fgId != null) {
	        Optional<ReptName> fgOpt = reptNameRepository.findById(fgId);
	        if (fgOpt.isPresent()) {
	            ReptName fg = fgOpt.get();
	            Map<String, Object> fgMap = new HashMap<>();
	            fgMap.put("id", fg.getId());
	            fgMap.put("name", fg.getName());
	            resultMap.put("finishedGoods", fgMap);
	        }
	    }

	    List<RSubHead> rsubheadList = rSubHeadRepository.findAllSubName(fgId);

	    if (rsubheadList == null || rsubheadList.isEmpty()) {
	        resultMap.put("tableData", new ArrayList<>());
	        return ResponseEntity.ok(resultMap);
	    }

	    //  Collect data
	    Map<Long, List<UnitReportProductsList>> subheadItemMap = new HashMap<>();
	    Set<Long> allProductIds = new HashSet<>();
	    Set<Double> allLots = new HashSet<>();

	    for (RSubHead rsubhead : rsubheadList) {
	        List<UnitReportProductsList> oilItems =
	                reptGrpRepository.findByRnameAndRsubheadName(fgId, rsubhead.getId());

	        if (oilItems == null) oilItems = new ArrayList<>();

	        subheadItemMap.put(rsubhead.getId(), oilItems);

	        for (UnitReportProductsList item : oilItems) {
	            if (item.getProductId() != null)
	                allProductIds.add(item.getProductId());

	            if (item.getLot() != null)
	                allLots.add(item.getLot());
	        }
	    }

	    if (allProductIds.isEmpty()) {
	        resultMap.put("tableData", new ArrayList<>());
	        return ResponseEntity.ok(resultMap);
	    }

	    List<Long> productIds = new ArrayList<>(allProductIds);
	    List<Double> lots = new ArrayList<>(allLots);

	    // DB CALLS (ONCE ONLY)
	    Map<String, Double> openMap = groupByItemLot(
	            binstockAuditLogRepository.findBYStockRept(branch, "OPENING", productIds, start, end, lots, firmId));

	    Map<String, Double> purchaseMap = groupByItemLot(
	            binstockAuditLogRepository.findBYStockRept(branch, "Stock Inward", productIds, start, end, lots, firmId));

	    Map<String, Double> productionReceive = groupByItemLot(
	            binStockLogRepository.findBYStockCrushing(branch, "PRODUCTION", "R", productIds, start, end, lots, firmId));

	    Map<String, Double> productionIssue = groupByItemLot(
	            binStockLogRepository.findBYStockCrushing(branch, "PRODUCTION", "I", productIds, start, end, lots, firmId));

	    Map<String, Double> sales = groupByItemLot(
	            binStockLogRepository.findBYStockCrushing(branch, "DISPATCH", "I", productIds, start, end, lots, firmId));

	    Map<String, Double> purchaseReturnMap = groupByItemLot(
	            binstockAuditLogRepository.findBYStockRept(branch, "PurchaseReturn", productIds, start, end, lots, firmId));

	    Map<String, Double> shortageMap = groupByItemLot(
	            binstockAuditLogRepository.findBYStockRept(branch, "Shortage", productIds, start, end, lots, firmId));

	    Map<String, Double> excessMap = groupByItemLot(
	            binstockAuditLogRepository.findBYStockRept(branch, "Excess", productIds, start, end, lots, firmId));

	    Map<String, Double> salesReturnMap = groupByItemLot(
	            binStockLogRepository.findBYStockCrushing(branch, "SALES_RETURN", "I", productIds, start, end, lots, firmId));

	    Map<String, Double> closeMap = groupByItemLot(
	            binstockAuditLogRepository.findBYStockRept(branch, "CLOSING", productIds, start, end, lots, firmId));

	    Map<String, Double> splitIssueMap = groupByItemLot(
	            binStockLogRepository.findByFgStock(branch, "Scrab", productIds, start, end, lots, firmId));

	    Map<String, Double> splitReceiveMap = groupByItemLot(
	            binStockLogRepository.findByFgStock(branch, "Stock", productIds, start, end, lots, firmId));

	    Map<String, Double> interPIssueMap = groupByItemBranchLot(
	            binStockLogRepository.findBYFgStockInterIssue("MATERIAL_TRANSACTION", "I", productIds, start, end, lots, firmId));

	    Map<String, Double> interPReceiptMap = groupByItemBranchLot(
	            binStockLogRepository.findBYFgStockInterIssue("MATERIAL_TRANSACTION", "R", productIds, start, end, lots, firmId));

	    List<BranchStockList> closedBranches =
	            dayEndStockClosingSummaryRepository.findDayEndClosedBranches(branch);

	    //  Branch map 
	    Map<Long, String> branchMap = new HashMap<>();
	    for (BranchStockList br : closedBranches) {
	        branchMap.put(br.getId(), br.getBranchName());
	    }

	    // BUILD RESPONSE
	    List<Map<String, Object>> tableMap = new ArrayList<>();

	    for (RSubHead rsubhead : rsubheadList) {

	        Map<String, Object> summaryMap = new HashMap<>();
	        summaryMap.put("rsubname", rsubhead.getName());
	        summaryMap.put("rsubheadId", rsubhead.getId());

	        List<UnitReportProductsList> oilItems = subheadItemMap.get(rsubhead.getId());

	        if (oilItems == null || oilItems.isEmpty()) {
	            summaryMap.put("rows", new ArrayList<>());
	            tableMap.add(summaryMap);
	            continue;
	        }

	        List<FgStockReportDto> rowList = new ArrayList<>(oilItems.size());
	        Set<String> processed = new HashSet<>();

	        for (UnitReportProductsList item : oilItems) {

	            Long itemId = item.getProductId();
	            Double lot = item.getLot() == null ? 0.0 : item.getLot();

	            String key = itemId + "_" + lot;

	            // duplicate avoid
	            if (!processed.add(key)) continue;

	            FgStockReportDto dto = new FgStockReportDto();

	            dto.setItemId(itemId);
	            dto.setItemName(item.getProductName());
	            dto.setLot(lot);
	            dto.setUnit(item.getUnit());

	            dto.setOpeningQty(openMap.getOrDefault(key, 0.0));
	            dto.setPurchaseQty(purchaseMap.getOrDefault(key, 0.0));
	            dto.setProductionReceiveQty(productionReceive.getOrDefault(key, 0.0));
	            dto.setProductionIssueQty(productionIssue.getOrDefault(key, 0.0));
	            dto.setSalesQty(sales.getOrDefault(key, 0.0));
	            dto.setPurchaseReturnQty(purchaseReturnMap.getOrDefault(key, 0.0));
	            dto.setShortageQty(shortageMap.getOrDefault(key, 0.0));
	            dto.setExcessQty(excessMap.getOrDefault(key, 0.0));
	            dto.setSalesReturnQty(salesReturnMap.getOrDefault(key, 0.0));
	            dto.setClosingQty(closeMap.getOrDefault(key, 0.0));
	            dto.setSplitIssue(splitIssueMap.getOrDefault(key, 0.0));
	            dto.setSplitReceipt(splitReceiveMap.getOrDefault(key, 0.0));

	            Map<String, Double> interIssue = new HashMap<>();
	            Map<String, Double> interReceipt = new HashMap<>();

	            for (Map.Entry<Long, String> br : branchMap.entrySet()) {

	                String branchKey = itemId + "_" + br.getKey() + "_" + lot;

	                interIssue.put(br.getValue(),
	                        interPIssueMap.getOrDefault(branchKey, 0.0));

	                interReceipt.put(br.getValue(),
	                        interPReceiptMap.getOrDefault(branchKey, 0.0));
	            }

	            dto.setInterIssueQty(interIssue);
	            dto.setInterReceiptQty(interReceipt);

	            rowList.add(dto);
	        }

	        summaryMap.put("rows", rowList);
	        tableMap.add(summaryMap);
	    }

	    resultMap.put("tableData", tableMap);

	    return ResponseEntity.ok(resultMap);
	}
	
	private Map<String, Double> groupByItemLot(List<DayEndStockList> list) {
	    if (list == null || list.isEmpty()) return Collections.emptyMap();

	    return list.stream().collect(Collectors.groupingBy(
	            t -> t.getItemId() + "_" + t.getLot(),
	            Collectors.summingDouble(t -> t.getQty() == null ? 0.0 : t.getQty())
	    ));
	}

	private Map<String, Double> groupByItemBranchLot(List<DayEndStockList> list) {
	    if (list == null || list.isEmpty()) return Collections.emptyMap();

	    return list.stream().collect(Collectors.groupingBy(
	            t -> t.getItemId() + "_" + t.getBranchId() + "_" + t.getLot(),
	            Collectors.summingDouble(t -> t.getQty() == null ? 0.0 : t.getQty())
	    ));
	}
	
	

	public ResponseEntity<?> oilPackedDetails(User user, String fromDate, String toDate, Long branch, Long sjid) {

	    Date startDate = null;
	    Date endDate = null;

	    if (fromDate != null) {
	        startDate = dateUtil.getStringTODateWk(fromDate);
	    }
	    if (toDate != null) {
	        endDate = dateUtil.getStringTODateWk(toDate);
	    }

	    OilPackedDetailsDto result = new OilPackedDetailsDto();
	    updateHeaderDetails(result, user.getOrg_id(), branch);

	    List<Map<String, Object>> tableData = new ArrayList<>();

	    List<UnitReportMapList> headername = unitReportFinalProductRepository.findBySjid(sjid);
	    String headerName = unitReportTableMapRepository.findByName(sjid);
	    if(headerName.equalsIgnoreCase("GNUT_OIL")) {
	    	headerName = "GROUNDNUT_OIL";
	    }

	    for (UnitReportMapList summary : headername) {

	        Map<String, Object> summaryMap = new HashMap<>();
	        summaryMap.put("tableName", summary.getProductName1());
	        summaryMap.put("id", summary.getProductId1());

	        oilPackedDetails(summaryMap, summary, startDate, endDate, branch, user);

	        tableData.add(summaryMap);
	    }

	    Map<String, Object> finalResponse = new HashMap<>();
	    finalResponse.put("firmDetails", result.getFirmDetails());
	    finalResponse.put("plantDetails", result.getPlantDetails());
	    finalResponse.put("tableData", tableData);
	    finalResponse.put("headerName", headerName);

	    return new ResponseEntity<>(finalResponse, HttpStatus.OK);
	}
   
        private void oilPackedDetails(Map<String, Object> summaryMap, UnitReportMapList summary, Date startDate,
			Date endDate, Long branch, User user) {
        	List<Long> pId = stkOilListRepository.findByRefid(summary.getProductId1());
        	List<ByeProductDtoList> list = binStockLogRepository.findBYPackingLists(branch,10L,"PRODUCTION",user.getOrg_id(),startDate,endDate,pId);
            List<ByeProductDto> collect = list.stream().map(data->{
            	ByeProductDto dto = new ByeProductDto(data);
            	return dto;
            }).collect(Collectors.toList());
            summaryMap.put("products", collect);
	}
		
      
	
	
	private void updateHeaderDetails(OilPackedDetailsDto result, Long firmId, Long branch) {
		 CompanyList companyList = organisationRepository.findByFirm(firmId);
	        if (companyList != null) {
	        	result.setFirmDetails(new Company(companyList));
	        }
	        BranchAddressList branchAddressList1 = branchRepository.getBranchId(branch);
	        if (branchAddressList1 != null) {
	        	result.setPlantDetails(new BranchAddress(branchAddressList1));
	        }
	}

	public ResponseEntity<?> getAllGroup() {
		List<UnitReportMapList> findAllNameAndSjid = unitReportTableMapRepository.findAllNameAndSjid();
		List<OilPackedDto> collect = findAllNameAndSjid.stream().map(c->{
			OilPackedDto dto = new OilPackedDto(c);	
			 dto.setName(formatName(c.getProductName1()));
			return dto;
		}).collect(Collectors.toList());
		return new ResponseEntity<>(collect, HttpStatus.OK);
		
	}
	private String formatName(String dbName) {

	    if (dbName == null || dbName.trim().isEmpty()) {
	        return dbName;
	    }

	    // remove "_oil" if exists
	    String cleaned = dbName.toLowerCase().replace("_oil", "");

	    // split by underscore
	    String[] words = cleaned.split("_");

	    StringBuilder formatted = new StringBuilder();

	    for (String word : words) {
	        if (!word.isEmpty()) {
	            formatted.append(Character.toUpperCase(word.charAt(0)))
	                     .append(word.substring(1))
	                     .append(" ");
	        }
	    }

	    return formatted.toString().trim() + " Oil Packed";
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}