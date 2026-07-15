package com.aria.dto;

import com.aria.model.ASRSProductionDetails;

import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;


@NoArgsConstructor
@Getter
@Setter
public class ASRSProductionDTO {

    private Long id;
    private String trnNo;
    private String journalName;
    private Long journalId;
    private Long productId;
    private String productName;
    private String status;
    private String batchNo;
    private Long batchId;
    private Long shiftId;
    private String shiftDate;
    private String shiftName;
 // 26-08-2025 created by dhivya (ASRSAmendment related field ), discussed by alaguraja sir    
    private Long dominoCount;  
    private Long amendmentCount;    
    private Long presentDominoCount;  
    private Long amendmentCreatedBy;  
    private LocalDateTime amendmentCreatedTime;
    private String reason;
    
//  29-08-2025 created by dhivya (for amendment restriction), discussed by alaguraja sir
    //private String  asrsStatus;
    private String amendmentCreatedByName;
  
    private Long palletQty;
    private String productionStatus;

    List<ASRSPalletDetailsDTO> palletDetails;

    public ASRSProductionDTO(ASRSProductionDetails productionEntry) {
        DateUtil dateUtil = new DateUtil();
        this.id = productionEntry.getId();
        this.trnNo = productionEntry.getTrnNo();
        this.journalName = productionEntry.getJournalName();
        this.journalId = productionEntry.getJournalId();
        this.productId = productionEntry.getProductId();
        this.productName = productionEntry.getProductName();
        this.status = productionEntry.getStatus();
        this.batchNo = productionEntry.getBatchNo();
        this.batchId = productionEntry.getBatchId();
        this.shiftId = productionEntry.getShiftId();
        this.shiftDate = dateUtil.getDateTOStringWk(productionEntry.getShiftDate());
        this.shiftName = productionEntry.getShiftName();
        this.dominoCount=productionEntry.getDominoCount();
        this.amendmentCount=productionEntry.getAmendmentCount();
        this.presentDominoCount=productionEntry.getPresentDominoCount();
        this.amendmentCreatedBy=productionEntry.getAmendmentCreatedBy();
        this.amendmentCreatedTime=productionEntry.getAmendmentCreatedTime();
        this.reason=productionEntry.getReason();
        this.status=productionEntry.getStatus();

    }
}
