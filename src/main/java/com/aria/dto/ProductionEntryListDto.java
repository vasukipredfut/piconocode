package com.aria.dto;

import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductionEntryListDto {


    private String status;

    private Long journalId;

    private String journalName;

    private String shiftDate;

    private Long shiftId;

    private String shiftName;

    private String tsName;

    private String tsTable;

    private String dcType;
    private Long machineId;
    private String machineName;
    private String mmctable;
    private String batchNo;
    private Long batchId;
    private Long godownId;
    private String godownName;
    private String humidity;
    private String temperature;
    private String trnNo;
    private Boolean asrs;
    private Long tsId;



    public ProductionEntryListDto(ProductionEntryList productionEntryList) {
        DateUtil dateUtil = new DateUtil();
        if(productionEntryList.getStatus() != null && productionEntryList.getStatus().equalsIgnoreCase("Y")) {
            this.status = "Completed";
        }else {
            this.status = "InProgress";
        }
        this.journalId = productionEntryList.getJournalId();
        this.journalName = productionEntryList.getJournalName();
        if (productionEntryList.getShiftDate() != null) {
            this.shiftDate = dateUtil.getDateTOStringWk(productionEntryList.getShiftDate());
        }
        this.shiftName = productionEntryList.getShiftName();
        this.shiftId = productionEntryList.getShiftId();
        this.tsName = productionEntryList.getTsName();
        this.tsTable = productionEntryList.getTsTable();
        this.dcType = productionEntryList.getDcType();
        this.machineId = productionEntryList.getMachineId();
        this.mmctable = productionEntryList.getMmctable();
        this.batchNo = productionEntryList.getBatchNo();
        this.batchId = productionEntryList.getBatchId();
        this.godownId = productionEntryList.getGodownId();
        this.godownName = productionEntryList.getGodownName();
        this.humidity = productionEntryList.getHumidity();
        this.temperature = productionEntryList.getTemperature();
        this.trnNo = productionEntryList.getTrnNo();
        this.tsId = productionEntryList.getTsId();
    }
}
