package com.aria.dto;

import com.aria.model.InwardCheck;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor

public class InwardCheckDto {

    private Long id;

    private String completedstep;

    private String status;

    private String inwardDate;

    private String recheckdesc;
    private String recheckLog;

    private String batchNo;

    private Long createdBy;

    private Long updatedBy;
    
    private Long productId;

    private LocalDateTime created_date;

    private LocalDateTime updated_date;



    public InwardCheckDto(InwardCheck inwardInspectionEntry) {
        this.id = inwardInspectionEntry.getId();
        this.completedstep = inwardInspectionEntry.getCompletedstep();
        this.recheckLog = inwardInspectionEntry.getRecheckLog();
        this.recheckdesc = inwardInspectionEntry.getRecheckdesc();
        this.status = inwardInspectionEntry.getStatus();
        this.createdBy = inwardInspectionEntry.getCreatedBy();
        this.updatedBy = inwardInspectionEntry.getUpdatedBy();
        this.created_date = inwardInspectionEntry.getCreated_date();
        this.batchNo = inwardInspectionEntry.getBatchNo();
        this.productId=inwardInspectionEntry.getProductId();
        if (inwardInspectionEntry.getUpdated_date() != null) {
            this.updated_date = inwardInspectionEntry.getUpdated_date();
        } else {
            this.updated_date = inwardInspectionEntry.getCreated_date();
        }

    }
}