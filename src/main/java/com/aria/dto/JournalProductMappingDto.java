package com.aria.dto;

import com.aria.model.JournalProductMapping;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
@NoArgsConstructor
public class JournalProductMappingDto {


    private Long id;

    private Long journalId;
    private Long branchId;
    private Long deptId;

    private Long productId;

    private String productName;

    private String considerAs;

    private String bomTakeType;

    private String quantity;

    private String batchProductType;

    private String cost;

    private Double costPer;

    
    private Double fqty;


    private Double costAmount;

    
    private String pridunit;

    
    private String activity;

    
    private String trackno;

    private Boolean applicableForJcal;

    private String bottleWeight;

    //private String calculationType;

    private List<JournalAttributesMappingDto> dataSetList;

    public JournalProductMappingDto(JournalProductMapping journalProductMapping) {
        this.id = journalProductMapping.getId();
        this.journalId = journalProductMapping.getJournalId();
        this.productId = journalProductMapping.getProductId();
        this.productName = journalProductMapping.getProductName();
        this.considerAs = journalProductMapping.getConsiderAs();
        this.batchProductType = journalProductMapping.getBatchProductType();
        this.quantity = journalProductMapping.getQuantity();
        this.cost = journalProductMapping.getCost();
        this.costPer = journalProductMapping.getCost_per();
        this.fqty = journalProductMapping.getFqty();
        this.costAmount = journalProductMapping.getCost_amount();
        this.pridunit = journalProductMapping.getPridunit();
        this.activity = journalProductMapping.getActivity();
        this.trackno = journalProductMapping.getTrackno();
        this.bomTakeType = journalProductMapping.getBomTakeType();
        this.applicableForJcal = journalProductMapping.getApplicableForJcal();
        this.bottleWeight = journalProductMapping.getBottleWeight();
        
    }
}
