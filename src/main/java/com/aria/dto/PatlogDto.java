package com.aria.dto;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Date;


import com.aria.model.Patlog;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PatlogDto {

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

    private Date shiftdate;

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

    public PatlogDto(Patlog model) {
        this.id = model.getId();
        this.logid = model.getLogid();
        this.logtype = model.getLogtype();
        this.schid = model.getSchid();
        this.mcid = model.getMcid();
        this.batchno = model.getBatchno();
        this.batchid = model.getBatchid();
        //this.tcid = model.getTcid();
        this.tgid = model.getTgid();
       // this.taskid = model.getTaskid();
        this.atid = model.getAtid();
        this.resid = model.getResid();
        this.resval = model.getResval();
        this.resscore = model.getResscore();
        this.scoreto = model.getScoreto();
        this.scoretoid = model.getScoretoid();
        this.tno = model.getTno();
//		this.entry = model.getEntry();
        this.restype = model.getRestype();
        this.dcolor = model.getDcolor();
        this.dunit = model.getDunit();
        this.mmcid = model.getMcid();
        this.mcauxid = model.getMcauxid();
        this.mctable = model.getMctable();
        this.mcauxtable = model.getMcauxtable();
        this.mmctable = model.getMctable();
        this.shiftid = model.getShiftid();
//		this.shiftdate = model.getShiftdate()
        this.kpi = model.getKpi();
        this.proreportdbid = model.getProreportdbid();
        this.journalflag = model.getJournalflag();

        this.createdBy = model.getCreatedBy();
        this.updatedBy = model.getUpdatedBy();
        this.created_date = model.getCreated_date();
        this.updated_date = model.getUpdated_date();
    }



    public PatlogDto(Object patId, Object taskCategoryId, Object taskCategoryName, Object taskNameId, Object taskName,
                     Object taskGroupId, Object taskGroupName, Object kpiNameId, Object kpiName, Object productId,
                     Object productName, Object attributeId, Object attributeName, Object attributeValue, Object skipflag) {

        if (taskCategoryId != null) {
            this.taskCategoryId = ((BigInteger) taskCategoryId).longValue();
        }
        if (taskNameId != null) {
            this.taskNameId = ((BigInteger) taskNameId).longValue();
        }
        if (taskGroupId != null) {
            this.taskGroupId = ((BigInteger) taskGroupId).longValue();
        }
        if (kpiNameId != null) {
            this.kpiNameId = ((BigInteger) kpiNameId).longValue();
        }
        if (productId != null) {
            this.productMasterId = ((BigInteger) productId).longValue();
        }
        if (attributeId != null) {
            this.attributeId = ((BigInteger) attributeId).longValue();
        }
        this.id = ((Integer) patId).longValue();
        if (taskCategoryName != null) {
            this.taskCategory = taskCategoryName.toString();
        }
        if (taskName != null) {
            this.taskName = taskName.toString();
        }
        if (taskGroupName != null) {
            this.taskGroup = taskGroupName.toString();
        }
        if (kpiName != null) {
            this.kpiName = kpiName.toString();
        }
        if (productName != null) {
            this.productMasterName = productName.toString();
        }
        if (attributeName != null) {
            this.attributeName = attributeName.toString();
        }
        if (attributeValue != null) {
            this.attributeValue = attributeValue.toString();
        }
        if (skipflag != null) {
            this.skipflag = skipflag.toString();
        }

    }

    public PatlogDto(Object patId, Object kpiNameId, Object kpiName, Object batchNo, Object entryType) {
        this.id = ((Integer) patId).longValue();
        this.kpiName = kpiName.toString();
        this.kpiNameId = ((BigInteger) kpiNameId).longValue();
        this.batchno = batchNo.toString();
        if (entryType != null) {
            this.entryType = entryType.toString();
        }
    }

}
