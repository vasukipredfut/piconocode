package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ProductionEntryFormDto {

    private Long id;

    private String dcType;

    private Long tsId;

    private String tsName;

    private String tsTable;

    private String trnNo;

    private Long dept;

    private Long branch;
    private Long firmId;

    private Long shiftId;

    private String shiftDate;

    private Long pshift;

    private String psStatus;

    private Long batchId;

    private String batchNo;

    private String pplan;

    private Long journalId;

    private Long mmcId;

    private String mmcTable;

    private Long mcauxId;
    private Long dispUnit;

    private String mcauxTable;

    private String dispName;

    private String total;

    private String avg;

    private String usecid;

    private String upstatus;

    private int disporder;

    private String orderNumber;

    private String godown;

    private Long godownId;

    private String temperature;

    private String humidity;

    private Long createdBy;

    private Long updatedBy;

    private LocalDateTime created_date;

    private LocalDateTime updated_date;

    private String status;

    private String entryIsOn;

    private String inwardDate;

    List<ProductionEntryDataSetDto> dataSetList;
    List<ProductionEntryClosingDataDto> bomDataList;
    List<ProductionEntryClosingDataDto> byeDataList;

    private String type;



}
