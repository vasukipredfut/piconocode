package com.aria.model;


import com.aria.dto.ASRSProductionDTO;
import com.aria.dto.ASRSProductionEntryDTO;
import com.aria.dto.ASRSProductionEntryList;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "asrs_production_detail")
public class ASRSProductionDetails {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String trnNo;

    @Column
    private Long journalId;

    @Column
    private String journalName;

    @Column
    private Long productId;

    @Column
    private String productName;

    @Column
    private String status;

    @Column
    private String batchNo;

    @Column
    private Long batchId;

    @Column
    private Long shiftId;

    @Column
    @Temporal(TemporalType.DATE)
    private Date shiftDate;

    @Column
    private String shiftName;
 // 26-08-2025 created by dhivya (ASRSAmendment related field ), discussed by alaguraja sir      
    @Column
    private Long dominoCount;
    
    @Column
    private Long amendmentCount;
    
    @Column
    private Long presentDominoCount;
    
    @Column(name="amendment_createdby")
    private Long amendmentCreatedBy;
    
    @Column
    private String reason;
    
    @Column
    private LocalDateTime amendmentCreatedTime;


    // commented by alagu not need this column
/*//  29-08-2025 created by dhivya (for amendment restriction), discussed by alaguraja sir
    @Column(name="asrs_status")
    private String  asrsStatus;
    
    @Column(name="amendment_createdbyname") 
   private String amendmentCreatedByName;
*/
   
   


    public ASRSProductionDetails(ASRSProductionDTO productionEntry) {
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
        this.shiftDate = dateUtil.getStringTODateWk(productionEntry.getShiftDate());
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
