package com.aria.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.aria.dao.DayEndStockClosingSummaryRepository;
import com.aria.dto.BranchStockList;
import com.aria.model.Branch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ReportUtils {

	@Autowired
    public DayEndStockClosingSummaryRepository dayEndStockClosingSummaryRepository;

    public Map<String, Object> getUnitReportSeasameInputMap() {

        Map<String, Object> stringObjectMap = new HashMap<>();

        stringObjectMap.put("PRODUCT", "PRODUCT");
        stringObjectMap.put("OPENING STOCK", "OPENING STOCK");

                Map<String, Object> receiptFromMap = new HashMap<>();
                receiptFromMap.put("PARTY ", "PARTY");
                receiptFromMap.put("OTHER PLANT", "OTHER PLANT");
                receiptFromMap.put("QC", "QC");
                receiptFromMap.put("EXCESS", "EXCESS");

        stringObjectMap.put("RECEIPTS FROM", receiptFromMap);

                Map<String, Object> issueTo = new HashMap<>();
                issueTo.put("CRUSHIHG ", "CRUSHIHG");
                issueTo.put("ROTARY", "ROTARY");

        stringObjectMap.put("ISSUED TO", issueTo);

                Map<String, Object> issueTo1 = new HashMap<>();
                issueTo1.put("SALES", "SALES");
                issueTo1.put("OTHER PLANT", "OTHER PLANT");
                issueTo1.put("PURCHASE RETURN", "PURCHASE RETURN");
                issueTo1.put("QC", "QC");

        stringObjectMap.put("ISSUED TO1", issueTo1);

        stringObjectMap.put("SHORTAGE", "SHORTAGE");
        stringObjectMap.put("CLOSING STOCK", "CLOSING STOCK");
        return stringObjectMap;
    }



    public Map<String, Object> getUnitReportSeasameOutPutMap() {

        Map<String, Object> stringObjectMap = new HashMap<>();

        stringObjectMap.put("PRODUCT", "PRODUCT");
        stringObjectMap.put("OPENING STOCK", "OPENING STOCK");

        Map<String, Object> receiptFromMap = new HashMap<>();
        receiptFromMap.put("PACKING ", "PACKING");
        receiptFromMap.put("OTHER PLANT", "OTHER PLANT");

        stringObjectMap.put("RECEIPTS FROM", receiptFromMap);

        Map<String, Object> receiptFrom1 = new HashMap<>();
        receiptFrom1.put("CRUSHIHG ", "CRUSHIHG");
        receiptFrom1.put("ROTARY", "ROTARY");

        stringObjectMap.put("RECEIPTS FROM1", receiptFrom1);

        Map<String, Object> issueTo = new HashMap<>();
        issueTo.put("PACKING", "PACKING");
        issueTo.put("PRODUCTION", "PRODUCTION");
        issueTo.put("OTHER PLANT", "OTHER PLANT");
        issueTo.put("DESPATCH", "DESPATCH");

        stringObjectMap.put("ISSUED TO", issueTo);

        stringObjectMap.put("DRYAGE", "DRYAGE");
        stringObjectMap.put("CLOSING STOCK", "CLOSING STOCK");
        return stringObjectMap;
    }


    public Map<String, Object> getUnitReportSeasameSummaryMap() {

        Map<String, Object> stringObjectMap = new HashMap<>();

            Map<String, Object> issueToProductionMap = new HashMap<>();

            issueToProductionMap.put("PALM JAGGERY IN KGS", "PALM JAGGERY IN KGS");
            issueToProductionMap.put("OPENING STOCK", "OPENING STOCK");

                Map<String, Object> crushingInBagsMap = new HashMap<>();

                crushingInBagsMap.put("ARIA GRADE SESAME SEED", "ARIA GRADE SESAME SEED");
                crushingInBagsMap.put("DELITE GRADE SEED", "DELITE GRADE SEED");
                crushingInBagsMap.put("TOTAL", "TOTAL");

            issueToProductionMap.put("CRUSHING IN BAGS", crushingInBagsMap);

        stringObjectMap.put("ISSUED TO PRODUCTION", issueToProductionMap);

        Map<String, Object> receivedFromProductionMap = new HashMap<>();

        receivedFromProductionMap.put("ARIA SESAME OIL IN KGS", "ARIA SESAME OIL IN KGS");
        receivedFromProductionMap.put("SESAME CRUSHING WASTE", "SESAME CRUSHING WASTE");

        Map<String, Object> ariaseasameOilCakeMap = new HashMap<>();

        ariaseasameOilCakeMap.put("IN KGS", "IN KGS");
        ariaseasameOilCakeMap.put("IN 50 KG BAGS", "IN 50 KG BAGS");

        receivedFromProductionMap.put("ARIA SESAME OIL CAKE", ariaseasameOilCakeMap);

        receivedFromProductionMap.put("DRIAGE IN KG", "DRIAGE IN KG");

        stringObjectMap.put("RECEIVED FROM PRODUCTION", receivedFromProductionMap);

        return stringObjectMap;
    }


    public List<Long> getBranchIds() {
        // Implement logic to retrieve branch IDs from database
        // Replace the following line with actual logic
        List<Long> longs = new java.util.ArrayList<>();
        longs.add(1L);
        longs.add(2L);
        longs.add(3L);
        return longs;
    }

    
    public Map<String, Object> getFgStockReportSeasameInputMap(Long firmId, Long branch) {

        Map<String, Object> mainMap = new HashMap<>();

        mainMap.put("PRODUCT NAME", "PRODUCT NAME");
        mainMap.put("DISPLAY & LOT", "DISPLAY & LOT");
        mainMap.put("DISPLAY & UNIT", "DISPLAY & UNIT");
        mainMap.put("OPENING STOCK", "OPENING STOCK");
        mainMap.put("PURCHASE", "PURCHASE");
        // PURCHASE
        Map<String, Object> OTHERPLANTRECEIPT = new HashMap<>();
        List<BranchStockList> closedBranches =dayEndStockClosingSummaryRepository.findDayEndClosedBranches(branch);
      
        for (BranchStockList br : closedBranches) {
         
            	OTHERPLANTRECEIPT.put(br.getBranchName(), br.getBranchName());
                    }
        mainMap.put("OTHERPLANTRECEIPT", OTHERPLANTRECEIPT);

        mainMap.put("BRANCHPURCHASE", "BRANCHPURCHASE");    

        mainMap.put("PRODUCTION", "PRODUCTION");     
        mainMap.put("EXCESS", "EXCESS");
        mainMap.put("SPLIT & CONV (R)", "SPLIT & CONV (R)");
        mainMap.put("SALES (RETURN)", "SALES (RETURN)");

        mainMap.put("TOTAL (R)", "TOTAL (R)");
        mainMap.put("PURCHASE RETURN", "PURCHASE RETURN");
        mainMap.put("SHORTAGE", "SHORTAGE");
        mainMap.put("OTHERPLANTISSUE", OTHERPLANTRECEIPT);
        mainMap.put("SPLIT & CONV (I)", "SPLIT & CONV (I)");
        mainMap.put("PRODUCTION (I)", "PRODUCTION (I)");
        mainMap.put("SALES", "SALES");
        mainMap.put("DAMAGE", "DAMAGE");
        mainMap.put("ISSUES", "ISSUES");
        mainMap.put("CLOSING STOCK", "CLOSING STOCK");

        return mainMap;
    }
}
