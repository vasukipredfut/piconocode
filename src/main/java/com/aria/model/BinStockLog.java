package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "stbin")
public class BinStockLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String trnno;

    @Column
    private Long billid;

    @Column
    private Long bid;

    @Column
    private String bno;

    @Column
    private Date bdt;

    @Column
    private String batchno;

    @Column
    private Long refid;

    @Column
    private String reftype;

    @Column
    private Double urate;

    @Column
    private Double lot;

    @Column
    private Double qty;

    @Column
    private Double ltotal;

    @Column
    private String gcode;

    @Column
    private Double fre;

    @Column
    private Double oth;

    @Column
    private Double avgcost;

    @Column
    private Double cost;

    @Column
    private Long aby;

    @Column
    private Long vby;

    @Column
    private Long authenty;

    @Column
    private Long conid;

    @Column
    private String contype;

    @Column
    private Long gname;

    @Column
    private String ttable;

    @Column
    private String trntype;

    @Column
    private Long conzoneid;

    @Column
    private String conzone;

    @Column
    private String stkrno;

    @Column
    private String stkpno;

    @Column
    private String sflag;

    @Column
    private String dktno;

    @Column
    private String fgcode;

    @Column
    private String fgname;

    @Column
    private String trnstatus;

    @Column
    private Date adate;

    @Column
    private Long gid;

    @Column
    private String stkstatus;

    @Column
    private Long dept;

    @Column
    private Long branch;

    @Column
    private Long batchid;

    @Column
    private Long cid;

    @Column
    private Long fmid;

    @Column
    private String fmtable;

    @Column
    private Long toid;

    @Column
    private String totable;

    @Column
    private Long aref;

    @Column
    private String bintype;

    @Column
    private Date updatedon;

    @Column
    private Long sjid;

    @Column
    private Long poud;

    @Column
    private Date ddate;

    @Column
    private Date idate;

    @Column
    private String naration;

    @Column(name = "entry_type")
    private String entryType;

    @Column(name = "trn_date")
    @Temporal(TemporalType.DATE)
    private Date trnDate;

    @Column(name = "sal_status")
    private String salStatus;

    @Column(name = "accounting_date")
    @Temporal(TemporalType.DATE)
    private Date accounting_date;


    public BinStockLog(BinStock binStock) {

        this.trnno = binStock.getLTrnNo();
        this.billid = binStock.getPurchaseBillId();
        //this.bid = bid;
        //this.bno = bno;
        this.bdt = binStock.getInwarDate();
        this.batchno = binStock.getBatchNo();
        this.refid = binStock.getRefId();

        this.lot = binStock.getLot();
        this.qty = binStock.getStkQty();

        this.gcode = binStock.getBinId();
        this.avgcost = binStock.getAvgCost();
        this.cost = binStock.getCost();
        this.gname = binStock.getBinName();
       /*
        this.reftype = reftype;
        this.urate = urate;
        this.ltotal = ltotal;
        this.fre = fre;
        this.oth = oth;
        this.aby = aby;
        this.vby = vby;
        this.authenty = authenty;
        this.conid = conid;
        this.contype = contype;
        this.ttable = ttable;
        this.trntype = trntype;
        this.conzoneid = conzoneid;
        this.conzone = conzone;
        this.stkrno = stkrno;
        this.stkpno = stkpno;
        this.sflag = sflag;
        this.dktno = dktno;
        this.fgcode = fgcode;
        this.fgname = fgname;
        this.trnstatus = trnstatus;
        this.adate = adate;
        this.stkstatus = binStock.get; */

        this.gid = binStock.getGid();
        this.dept = binStock.getDept();
        this.branch = binStock.getBranch();
        this.batchid = binStock.getBatchId();
        this.cid = binStock.getCid();
        /*this.fmid = fmid;
        this.fmtable = fmtable;
        this.toid = toid;
        this.totable = totable;
        this.aref = aref;*/
        this.bintype = binStock.getBinType();
        /*this.updatedon = updatedon;
        this.sjid = binStock.get();
        this.poud = poud;
        this.ddate = ddate;
        this.idate = idate;
        this.naration = naration;
        */
    }
}
