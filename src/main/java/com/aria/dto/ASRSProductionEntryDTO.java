package com.aria.dto;

import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ASRSProductionEntryDTO {


    private String trnNo;
    private String journalName;
    private Long journalId;
    private Long asrsId;
    private Long productId;
    private String productName;
    private String status;
    private String batchNo;
    private Long batchId;
    private Long shiftId;
    private String shiftDate;
    private String shiftName;

    private Long dominoCount ;

    private Double allocatedQty;

    private Double asrsQty;

    private Double deductionQty;

    private Long palletQuantity;

//  29-08-2025 created by dhivya (for amendment restriction), discussed by alaguraja sir
    private String asrsStatus;
    
   private String asrsProductType;
   
   private String partCategory;  
 


    public ASRSProductionEntryDTO(ASRSProductionEntryList productionEntry) {
        DateUtil dateUtil = new DateUtil();
        this.trnNo = productionEntry.getTrnNo();
        this.journalName = productionEntry.getJournalName();
        this.journalId = productionEntry.getJournalId();
        this.asrsId = productionEntry.getAsrsId();
        this.productId = productionEntry.getProductId();
        this.productName = productionEntry.getProductName();
        this.status = productionEntry.getStatus();
        this.batchNo = productionEntry.getBatchNo();
        this.batchId = productionEntry.getBatchId();
        this.shiftId = productionEntry.getShiftId();
        this.shiftDate = dateUtil.getDateTOStringWk(productionEntry.getShiftDate());
        this.shiftName = productionEntry.getShiftName();
        this.dominoCount = productionEntry.getDominoCount();
        this.asrsStatus=productionEntry.getAsrsStatus();
        this.palletQuantity=productionEntry.getPalletQuantity();
        this.asrsProductType=productionEntry.getAsrsProductType();
        this.partCategory=productionEntry.getPartCategory();
        
    }
}
