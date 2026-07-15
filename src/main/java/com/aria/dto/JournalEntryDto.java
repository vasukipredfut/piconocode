package com.aria.dto;

import com.aria.model.JournalEntry;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class JournalEntryDto {

    private Long id;

    private String trntype;

    private String trnno;

    private Date trndate;

    private Long pshiftid;

    private Long sjid;

    private Long wcid;

    private String wctable;

    private Long proid;

    private String productName;


    private String binid;
    private Long binname;
    private Long batchid;
    private String batchno;
    private String reduce_batchno;


    private String type;

    private String bomIntakeType;

    private Double stkqty;

    private Double lot;

    private String unitName;

    private Double avgcost;

    private Double cost;

    private String atrnno;

    private Date atrndate;

    private String astatus;

    private Long aby;

    private Long godown;
    private Long godownId;
    private String godownName;

    private Long did;

    private Long bid;

    private Double totalInputQty;

    private Double totalOutputQty;


    public JournalEntryDto(JournalEntry journalEntry) {
        this.id = journalEntry.getId();
        this.trntype = journalEntry.getTrntype();
        this.trnno = journalEntry.getTrnno();
        this.trndate = journalEntry.getTrndate();
        this.pshiftid = journalEntry.getPshiftid();
        this.sjid = journalEntry.getSjid();
        this.wcid = journalEntry.getWcid();
        this.wctable = journalEntry.getWctable();
        this.proid = journalEntry.getProid();
        this.binid = journalEntry.getBinid();
        this.binname = journalEntry.getBinname();
        this.batchid = journalEntry.getBatchid();
        this.batchno = journalEntry.getBatchno();
        this.reduce_batchno = journalEntry.getReduce_batchno();
        this.type = journalEntry.getType();
        this.stkqty = journalEntry.getStkqty();
        this.lot = journalEntry.getLot();
        this.avgcost = journalEntry.getAvgcost();
        this.cost = journalEntry.getCost();
        this.atrndate = journalEntry.getAtrndate();
        this.astatus = journalEntry.getAstatus();
        this.godown = journalEntry.getGodown();
        this.did = journalEntry.getDid();
        this.bid = journalEntry.getBid();
        this.totalInputQty = journalEntry.getTotalInputQty();
        this.totalOutputQty = journalEntry.getTotalOutputQty();
        this.bomIntakeType = journalEntry.getBomIntakeType();

    }



    public JournalEntryDto(JournalEntryDataList journalEntry) {
        this.id = journalEntry.getId();
        this.trntype = journalEntry.getTrntype();
        this.trnno = journalEntry.getTrnno();
        this.trndate = journalEntry.getTrndate();
        this.pshiftid = journalEntry.getPshiftid();
        this.sjid = journalEntry.getSjid();
        this.wcid = journalEntry.getWcid();
        this.wctable = journalEntry.getWctable();
        this.proid = journalEntry.getProid();
        this.binid = journalEntry.getBinid();
        this.binname = journalEntry.getBinname();
        this.batchid = journalEntry.getBatchid();
        this.batchno = journalEntry.getBatchno();
        this.reduce_batchno = journalEntry.getReduce_batchno();
        this.type = journalEntry.getType();
        this.stkqty = journalEntry.getStkqty();
        this.lot = journalEntry.getLot();
        this.avgcost = journalEntry.getAvgcost();
        this.cost = journalEntry.getCost();
        this.atrndate = journalEntry.getAtrndate();
        this.astatus = journalEntry.getAstatus();
        this.godown = journalEntry.getGodown();
        this.did = journalEntry.getDid();
        this.bid = journalEntry.getBid();
        this.totalInputQty = journalEntry.getTotal_input_qty();
        this.bomIntakeType = journalEntry.getBomTakeType();

    }
}
