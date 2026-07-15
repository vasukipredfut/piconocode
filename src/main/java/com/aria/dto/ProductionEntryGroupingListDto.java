package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductionEntryGroupingListDto {


    private Long colId;
    private String colName;
    private String colLot;
    private String colType;
    private String colCate;
    private String colTable;
    private String dataType;
    private String resultVal;
    private String considerAs;
    private String pType;
    private String bomIntakeType;



    public ProductionEntryGroupingListDto(ProductionEntryGroupingList productionEntryGroupingList) {
        this.colId = productionEntryGroupingList.getColId();
        this.colName = productionEntryGroupingList.getColName();
        this.colLot = productionEntryGroupingList.getColLot();
        this.colType = productionEntryGroupingList.getColType();
        this.colCate = productionEntryGroupingList.getColCate();
        this.colTable = productionEntryGroupingList.getColTable();
        this.dataType = productionEntryGroupingList.getDataType();
        this.resultVal = productionEntryGroupingList.getResultVal();
        this.considerAs = productionEntryGroupingList.getConsiderAs();
        this.pType = productionEntryGroupingList.getpType();
        this.bomIntakeType = productionEntryGroupingList.getBomIntakeType();
    }
}
