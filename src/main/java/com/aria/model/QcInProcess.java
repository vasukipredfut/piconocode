package com.aria.model;

import java.time.LocalDateTime;
import java.util.Date;

import jakarta.persistence.*;


import com.aria.dto.QcInProcessDto;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "qc_inprocess")
public class QcInProcess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String logtype;

    @Column
    private Long logid;

    @Column
    private Long schid;

    @Column
    private Long mcid;

    @Column
    private String batchno;

    @Column
    private Long batchid;


    @Column
    private Long tgid;

    @Column
    private Long atid;

    @Column
    private Long resid;

    @Column
    private String resval;

    @Column
    private int resscore;

    @Column
    private String scoreto;

    @Column
    private Long scoretoid;

    @Column
    private String tno;

    @Column
    @Temporal(TemporalType.DATE)
    private Date entry;

    @Column
    private String restype;

    @Column
    private String dcolor;

    @Column
    private String dunit;

    @Column
    private Long mmcid;

    @Column
    private Long mcauxid;

    @Column
    private String mctable;

    @Column
    private String mcauxtable;

    @Column
    private String mmctable;

    @Column
    private Long shiftid;

    @Column
    private Date shiftdate;

    @Column
    private Long kpi;

    @Column
    private Long proreportdbid;

    @Column
    private String journalflag;


    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(updatable = false)
    private LocalDateTime created_date;

    @Column
    private LocalDateTime updated_date;

    @Column
    private String status;

    @Column
    private Long dept;

    @Column
    private Long branch;

    @Column
    private Long refId;

    @Column
    private Long binname;

    @Column
    private String binid;
    
    @Column 
    private Double quantity;
    @Column
    private Long batchBinNo;
    @Column
    private Long batchProduct;
    @Column
    private String packingBatchNo;
  

    public QcInProcess(QcInProcessDto model) {
        DateUtil dateUtil = new DateUtil();
        this.id = model.getId();
        this.logid = model.getLogid();
        this.logtype = model.getLogtype();
        this.schid = model.getSchid();
        this.mcid = model.getMcid();
        this.batchno = model.getBatchno();
        this.batchid = model.getBatchid();
        //this.tcid = model.getTcid();
        this.tgid = model.getTgid();
        //this.taskid = model.getTaskid();
        this.atid = model.getAtid();
        this.resid = model.getResid();
        this.resval = model.getResval();
        this.resscore = model.getResscore();
        this.scoreto = model.getScoreto();
        this.scoretoid = model.getScoretoid();
        this.tno = model.getTno();
        if(model.getEntry() != null) {
            this.entry = dateUtil.getStringTODateWk(model.getEntry());
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
        if(model.getShiftDate() != null) {
            this.shiftdate = dateUtil.getStringTODateWk(model.getShiftDate());
        }
        this.kpi = model.getKpiNameId();
        this.proreportdbid = model.getProreportdbid();
        this.journalflag = model.getJournalflag();
        this.createdBy = model.getCreatedBy();
        this.updatedBy = model.getUpdatedBy();
        this.created_date = model.getCreated_date();
        this.updated_date = model.getUpdated_date();

        this.status = model.getStatus();
        this.branch = model.getBranch();
        this.dept = model.getDept();
        this.refId = model.getProductMasterId();
        this.binid = model.getBinId();
        this.binname = model.getBinName();
        this.quantity=model.getQuantity();
        this.batchBinNo=model.getBatchBinNo();
        this.batchProduct=model.getBatchProduct();
        this.packingBatchNo=model.getPackingBatchNo();
    }
}
