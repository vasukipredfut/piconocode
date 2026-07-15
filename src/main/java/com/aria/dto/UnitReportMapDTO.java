package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UnitReportMapDTO {

    private Long productId1;
    private String productName1;
    private Long productId2;
    private String productName2;
    private Long sbyieldProductId;
    private String sbyieldProductName;
    private Long journalId;
    private Long rotaryJournalId;
    private Long packJournalId;


    public UnitReportMapDTO(UnitReportMapList unitReportMap) {
        this.productId1 = unitReportMap.getProductId1();
        this.productName1 = unitReportMap.getProductName1();
        this.productId2 = unitReportMap.getProductId2();
        this.productName2 = unitReportMap.getProductName2();
        this.sbyieldProductId = unitReportMap.getSbyieldProductId();
        this.sbyieldProductName = unitReportMap.getSbyieldProductName();
        this.journalId = unitReportMap.getJournalId();
        this.rotaryJournalId = unitReportMap.getRotaryJournalId();
        this.packJournalId=unitReportMap.getPackJournalId();
    }
}


