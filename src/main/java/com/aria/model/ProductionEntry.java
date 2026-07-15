package com.aria.model;

import com.aria.dto.ProductionEntryDto;
import com.aria.dto.ProductionEntryFormDto;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.Date;
import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "production_entry")
public class ProductionEntry {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dctype")
    private String dcType;

    @Column(name = "tsid")
    private Long tsId;

    @Column(name = "tsname")
    private String tsName;

    @Column(name = "tstable")
    private String tsTable;

    @Column(name = "trnno")
    private String trnNo;

    @Column(name = "dept")
    private Long dept;

    @Column(name = "branch")
    private Long branch;

    @Column(name = "shiftid")
    private Long shiftId;

    @Column(name = "shiftdate")
    @Temporal(TemporalType.DATE)
    private Date shiftDate;

    @Column(name = "pshift")
    private Long pshift;

    /*@Column(name = "psstatus")
    private String psStatus; */

    @Column(name = "batchid")
    private Long batchId;

    @Column(name = "batchno")
    private String batchNo;

    /*@Column(name = "pplan")
    private String pplan;*/

    @Column(name = "sjid")
    private Long journalId;

    @Column(name = "mmcid")
    private Long mmcId;

    @Column(name = "mmctable")
    private String mmcTable;

    @Column(name = "mcauxid")
    private Long mcauxId;

    @Column(name = "mcauxtable")
    private String mcauxTable;

    @Column(name = "dispname")
    private String dispName;

    @Column(name = "colid")
    private Long colid;

    @Column(name = "colname")
    private String colName;

    @Column(name = "coltable")
    private String colTable;

    @Column(name = "coltype")
    private String colType;

    @Column(name = "colcate")
    private String colCate;

    @Column(name = "dispunit")
    private Long dispUnit;

    @Column(name = "datatype")
    private String dataType;

    @Column(name = "data_res_values")
    private String dataResValues;

    @Column(name = "c1")
    private String c1;

    @Column(name = "c2")
    private String c2;

    @Column(name = "c3")
    private String c3;

    @Column(name = "c4")
    private String c4;

    @Column(name = "c5")
    private String c5;

    @Column(name = "c6")
    private String c6;

    @Column(name = "c7")
    private String c7;

    @Column(name = "c8")
    private String c8;

    @Column(name = "c9")
    private String c9;

    @Column(name = "c10")
    private String c10;

    @Column(name = "c11")
    private String c11;

    @Column(name = "c12")
    private String c12;

    @Column(name = "c13")
    private String c13;

    @Column(name = "CTOTAL")
    private double CTOTAL;

    @Column(name = "CAVG")
    private double CAVG;

    @Column(name = "total")
    private String total;

    @Column(name = "avg")
    private String avg;

    @Column(name = "usecid")
    private String usecid;

    @Column(name = "upstatus")
    private String upstatus;

    @Column(name = "disporder")
    private int disporder;

    @Column(name = "order_number")
    private String orderNumber;

    @Column(name = "godownid")
    private Long godownId;

    @Column(name = "godown")
    private String godown;

    @Column(name = "temperature")
    private String temperature;

    @Column(name = "humidity")
    private String humidity;

    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(updatable = false)
    private LocalDateTime created_date;

    @Column
    private LocalDateTime updated_date;

    @Column(name = "status")
    private String status;

    @Column(name = "inward_date")
    @Temporal(TemporalType.DATE)
    private Date inwardDate;

    @Column(name = "consider_as")
    private String considerAs;

    @Column
    private String col_lot;

    @Column(name = "ptype")
    private String pType;

    @Column(name = "accounting_date")
    @Temporal(TemporalType.DATE)
    private Date accounting_date;

    @Column(name = "firm_id")
    private Long firmId;

    @Column(name = "bom_Intake_Type")
    private String bomIntakeType;

    public ProductionEntry(ProductionEntryDto productionEntry) {
        DateUtil dateUtil = new DateUtil();
        this.id = productionEntry.getId();
        this.dcType = productionEntry.getDcType();
        this.tsId = productionEntry.getTsId();
        this.tsName = productionEntry.getTsName();
        this.tsTable = productionEntry.getTsTable();
        this.trnNo = productionEntry.getTrnNo();
        this.dept = productionEntry.getDept();
        this.branch = productionEntry.getBranch();
        this.shiftId = productionEntry.getShiftId();
        this.shiftDate = dateUtil.getStringTODateWk(productionEntry.getShiftDate());
        this.pshift = productionEntry.getShiftId();
        //this.psStatus = productionEntry.getPsStatus();
        this.batchId = productionEntry.getBatchId();
        this.batchNo = productionEntry.getBatchNo();
        //this.pplan = productionEntry.getPplan();
        this.journalId = productionEntry.getJournalId();
        this.mmcId = productionEntry.getMmcId();
        this.mmcTable = productionEntry.getMmcTable();
        this.mcauxId = productionEntry.getMcauxId();
        this.mcauxTable = productionEntry.getMcauxTable();
        this.dispName = productionEntry.getDispName();
        this.pType = productionEntry.getPType();
       /* this.colid = productionEntry.getColid();
        this.colTable = productionEntry.getColTable();
        this.colType = productionEntry.getColType();
        this.colCate = productionEntry.getColCate();
        this.dispUnit = productionEntry.getDispUnit();
        this.dataType = productionEntry.getDataType();
        this.c1 = productionEntry.getC1();
        this.c2 = productionEntry.getC2();
        this.c3 = productionEntry.getC3();
        this.c4 = productionEntry.getC4();
        this.c5 = productionEntry.getC5();
        this.c6 = productionEntry.getC6();
        this.c7 = productionEntry.getC7();
        this.c8 = productionEntry.getC8();
        this.c9 = productionEntry.getC9();
        this.c10 = productionEntry.getC10();
        this.c11 = productionEntry.getC11();
        this.c12 = productionEntry.getC12();
        this.c13 = productionEntry.getC13();
        this.CTOTAL = productionEntry.getCTOTAL();
        this.CAVG = productionEntry.getCAVG();*/
        this.total = productionEntry.getTotal();
        this.avg = productionEntry.getAvg();
        this.usecid = productionEntry.getUsecid();
        this.upstatus = productionEntry.getUpstatus();
        this.disporder = productionEntry.getDisporder();
        this.orderNumber = productionEntry.getOrderNumber();
        this.godown = productionEntry.getGodown();
        this.temperature = productionEntry.getTemperature();
        this.humidity = productionEntry.getHumidity();
        this.createdBy = productionEntry.getCreatedBy();
        this.updatedBy = productionEntry.getUpdatedBy();
        this.created_date = productionEntry.getCreated_date();
        this.status = productionEntry.getStatus();
        this.updated_date = productionEntry.getUpdated_date();
    }

    public ProductionEntry(ProductionEntryFormDto productionEntry) {
        DateUtil dateUtil = new DateUtil();
        this.id = productionEntry.getId();
        this.dcType = productionEntry.getDcType();
        this.tsId = productionEntry.getTsId();
        this.tsName = productionEntry.getTsName();
        this.tsTable = productionEntry.getTsTable();
        this.trnNo = productionEntry.getTrnNo();
        this.dept = productionEntry.getDept();
        this.branch = productionEntry.getBranch();
        this.shiftId = productionEntry.getShiftId();
        this.shiftDate = dateUtil.getStringTODateWk(productionEntry.getShiftDate());
        this.pshift = productionEntry.getShiftId();
        //this.psStatus = productionEntry.getPsStatus();
        this.batchId = productionEntry.getBatchId();
        this.batchNo = productionEntry.getBatchNo();
       // this.pplan = productionEntry.getPplan();
        this.journalId = productionEntry.getJournalId();
        this.dispUnit = productionEntry.getDispUnit();
        this.mmcId = productionEntry.getMmcId();
        this.mmcTable = productionEntry.getMmcTable();
        this.mcauxId = productionEntry.getMcauxId();
        this.mcauxTable = productionEntry.getMcauxTable();
        this.dispName = productionEntry.getDispName();
        this.total = productionEntry.getTotal();
        this.avg = productionEntry.getAvg();
        this.usecid = productionEntry.getUsecid();
        this.upstatus = productionEntry.getUpstatus();
        this.disporder = productionEntry.getDisporder();
        this.orderNumber = productionEntry.getOrderNumber();
        this.godown = productionEntry.getGodown();
        this.godownId = productionEntry.getGodownId();
        this.status = productionEntry.getStatus();
        this.temperature = productionEntry.getTemperature();
        this.humidity = productionEntry.getHumidity();
        this.createdBy = productionEntry.getCreatedBy();
        this.updatedBy = productionEntry.getUpdatedBy();
        this.created_date = productionEntry.getCreated_date();
        this.updated_date = productionEntry.getUpdated_date();
        if(productionEntry.getInwardDate() != null) {
            this.inwardDate = dateUtil.getStringTODateWk(productionEntry.getInwardDate());
        }
        this.firmId = productionEntry.getFirmId();
    }

}
