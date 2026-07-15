package com.aria.dto;

import com.aria.model.QcInProcess;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.time.LocalDateTime;

import jakarta.persistence.Column;



@Getter
@Setter
@NoArgsConstructor
public class QcInProcessDto {


    private Long id;

    private String logtype;

    private Long logid;

    private Long schid;

    private Long mcid;

    private String batchno;

    private Long batchid;

    // private Long tcid;

    private Long tgid;

    // private Long taskid;

    private Long atid;

    private Long resid;

    private String resval;

    private int resscore;

    private String scoreto;

    private Long scoretoid;

    private String tno;

    private String entry;

    private String restype;

    private String dcolor;

    private String dunit;

    private Long mmcid;

    private Long mcauxid;

    private String mctable;

    private String mcauxtable;

    private String mmctable;

    private Long shiftid;

    private String shiftDate;

    private Long kpi;

    private Long proreportdbid;

    private String journalflag;
    private String noOfBag;

    private Long stepId;
    private String stepName;
    private Long kpiNameId;
    private Long productMasterId;

    private String kpiName;
    private String productMasterName;

    private Long taskNameId;
    private Long taskCategoryId;
    private Long taskGroupId;

    private String taskName;
    private String taskCategory;
    private String taskGroup;

    private Long attributeId;
    private String attributeName;
    private String attributeValue;
    private String inwardDate;

    private Long createdBy;

    private Long updatedBy;

    private LocalDateTime created_date;

    private LocalDateTime updated_date;

    private String entryType;

    private String status;

    private String oldEntryType;

    private String skipflag;

    private Long dept;

    private Long branch;
    private Long binName;
    private String binId;
    private Double quantity;   
    private Long batchBinNo;  
    private Long batchProduct;   
    private String packingBatchNo;

    public QcInProcessDto(QcInProcess model) {
        DateUtil dateUtil = new DateUtil();
        this.id = model.getId();
        this.logid = model.getLogid();
        this.logtype = model.getLogtype();
        this.schid = model.getSchid();
        this.mcid = model.getMcid();
        this.batchno = model.getBatchno();
        this.batchid = model.getBatchid();
        this.tgid = model.getTgid();
        this.atid = model.getAtid();
        this.resid = model.getResid();
        this.resval = model.getResval();
        this.resscore = model.getResscore();
        this.scoreto = model.getScoreto();
        this.scoretoid = model.getScoretoid();
        this.tno = model.getTno();
        if (model.getEntry() != null) {
            this.entry = dateUtil.getDateTOStringWk(model.getEntry());
        }
        this.restype = model.getRestype();
        this.dcolor = model.getDcolor();
        this.dunit = model.getDunit();
        this.mmcid = model.getMcid();
        this.mcauxid = model.getMcauxid();
        this.mctable = model.getMctable();
        this.mcauxtable = model.getMcauxtable();
        this.mmctable = model.getMctable();
        this.shiftid = model.getShiftid();
        this.shiftDate = dateUtil.getDateTOStringWk(model.getShiftdate());
        this.kpi = model.getKpi();
        this.proreportdbid = model.getProreportdbid();
        this.journalflag = model.getJournalflag();
        this.createdBy = model.getCreatedBy();
        this.updatedBy = model.getUpdatedBy();
        this.created_date = model.getCreated_date();
        this.updated_date = model.getUpdated_date();

        this.status = model.getStatus();
        this.branch = model.getBranch();
        this.dept = model.getDept();
        this.binName = model.getBinname();
        this.binId = model.getBinid();
        this.quantity=model.getQuantity();
        this.batchBinNo=model.getBatchBinNo();
        this.batchProduct=model.getBatchProduct();
        this.packingBatchNo=model.getPackingBatchNo();
    }


}