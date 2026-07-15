package com.aria.dto;

import com.aria.model.InwardInspectionBatchList;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class InwardInspectionBatchListDto {

    private String inwardDate;

    private String billNo;

    private String lot;
    private String noOfBag;

    private String batchNo;
    private Long receivedId;
    private String entryType;

    public InwardInspectionBatchListDto(InwardInspectionBatchList inspectionBatchList) {
        DateUtil dateUtil = new DateUtil();
        if (inspectionBatchList.getInwardDate() != null) {
            this.inwardDate = dateUtil.getDateToStringDDMMYYYY(inspectionBatchList.getInwardDate());
        }
        this.billNo = inspectionBatchList.getBillNo();
        this.lot = inspectionBatchList.getLot();
        this.batchNo = inspectionBatchList.getBatchNo();
        this.receivedId = inspectionBatchList.getReceivedId();
        this.entryType = inspectionBatchList.getEntryType();
        this.noOfBag = inspectionBatchList.getNoOfBag();
    }
}
