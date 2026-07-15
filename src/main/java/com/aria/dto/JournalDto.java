package com.aria.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.aria.model.Journal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.text.StringEscapeUtils;


@Getter
@Setter
@NoArgsConstructor
public class JournalDto {

    private Long id;

    private String journalName;

    private Long primaryProductId;

    private String primaryProductName;

    private String dataCollectionType;

    private String displayUnit;

    private String productionCategory;

    private Long deptId;

    private String deptName;

    private String activity;

    private String processType;

    private String productvityType;

    //private String excuteInBatch;

    //private String batchNumber;

    private Long companyId;

    private String activeStatus;

    //private Long costGroupId;

    //private Long productvityProId;

    //private Long batchProId;


    //private Long no1SeedId;

    //private Long no2SeedId;


    private Long batchProxId;
    private String batchProductName;

    //private String exportOrder;

    //private Long filmPrinting;

    //private String category;

    //private int inputOutputUnTally;

    //private int p1;

    //private int p2;

    //private int p3;

    private Long createdBy;

    private Long updatedBy;

    private LocalDateTime created_date;

    private LocalDateTime updated_date;
    
    //private Boolean batchrequired;
    
    private Boolean blending;

    List<JournalProductMappingDto> productDetails;
    List<JournalMachineMappingDto> machineDetails;
    List<JournalMachineMappingDto> manWorkDetails;


    private String calculationType;
    private String stockType;



    List<BranchDto> branchList;

    private Boolean asrs;

    List<BranchDto> asrsBranchList;
    
   

    //need production validation




    public JournalDto(Journal journal) {

        this.id = journal.getId();
        this.journalName = StringEscapeUtils.unescapeHtml4(journal.getJournalName());
        this.primaryProductId = journal.getPrimaryProductId();
        this.dataCollectionType = journal.getDataCollectionType();
        this.productionCategory = journal.getProductionCategory();
        this.processType = journal.getProcessType();
        this.deptId = journal.getDeptId();
        this.productvityType = journal.getProductvityType();
        this.displayUnit = journal.getDisplayUnit();
        this.activity = journal.getActivity();
        this.companyId = journal.getCompanyId();
        //this.p1 = journal.getP1();
        //this.p2 = journal.getP2();
        //this.p3 = journal.getP3();
        this.activeStatus = journal.getActiveStatus();
        //this.costGroupId = journal.getCostGroupId();
        //this.batchrequired = journal.getBatchrequired();

        //this.productvityProId = journal.getPvityProId();
        //this.batchProId = journal.getBatchProId();
        //this.no1SeedId = journal.getNo1SeedId();
        //this.no2SeedId = journal.getNo2SeedId();
        this.batchProxId = journal.getBatchProxId();
        //this.exportOrder = journal.getExportOrder();
        //this.filmPrinting = journal.getFilmPrinting();
        //this.category = journal.getCategory();
        //this.inputOutputUnTally = journal.getInputOutputUnTally();
        this.blending=journal.getBlending();

        this.createdBy = journal.getCreatedBy();
        this.updatedBy = journal.getUpdatedBy();
        this.created_date = journal.getCreated_date();
        if (journal.getUpdated_date() != null) {
            this.updated_date = journal.getUpdated_date();
        } else {
            this.updated_date = journal.getCreated_date();
        }
        this.productDetails = journal.getProductDetails() != null ? journal.getProductDetails().stream().map(obj -> new JournalProductMappingDto(obj)).collect(Collectors.toList()) : null;
        this.machineDetails = journal.getMachineDetails() != null ? journal.getMachineDetails().stream().map(obj -> new JournalMachineMappingDto(obj)).collect(Collectors.toList()) : null;
        this.manWorkDetails = journal.getManWorkDetails() != null ? journal.getManWorkDetails().stream().map(obj -> new JournalMachineMappingDto(obj)).collect(Collectors.toList()) : null;

        this.calculationType = journal.getCalculationType();
        this.stockType = journal.getStockType();
        this.asrs = journal.getAsrs();
      
    }
}