package com.aria.model;

import com.aria.dto.SplitDto;
import com.aria.dto.SplitTransactionDto;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "split_trn")
public class SplitTransaction {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Date trndate;

    @Column
    private String   trnno;

    @Column
    private Long  splitid;

    @Column
    private Long  refid;

    @Column
    private double  qty;

    @Column
    private double  lot;

    @Column
    private Long  binname;



    @Column
    private Long  cby;

    @Column
    private String  con;

    @Column
    private Long  did;

    @Column
    private Long  bid;

    @Column
    private String  usecid;

    @Column
    private String  status;


    public SplitTransaction(SplitTransactionDto splitTransaction) {
        DateUtil dateUtil = new DateUtil();
        this.id = splitTransaction.getId();
        this.trndate = dateUtil.getStringTODateWk(splitTransaction.getTrndate());
        this.trnno = splitTransaction.getTrnno();
        this.splitid = splitTransaction.getSplitid();
        this.refid = splitTransaction.getRefid();
        this.qty = splitTransaction.getQty();
        this.lot = splitTransaction.getLot();
        this.binname = splitTransaction.getBinname();
        this.cby = splitTransaction.getCreatedBy();
        this.con = splitTransaction.getCon();
        this.did = splitTransaction.getDept();
        this.bid = splitTransaction.getBranch();
        this.usecid = splitTransaction.getUsecid();

    }

    public SplitTransaction(SplitDto splitTransaction) {
        this.splitid = splitTransaction.getSplitid();
        this.refid = splitTransaction.getRefid();
        this.qty = splitTransaction.getQty();
        this.lot = splitTransaction.getLot();
        this.binname = splitTransaction.getBinname();
        this.did = splitTransaction.getDept();
        this.bid = splitTransaction.getBranch();

    }

    public SplitTransaction(SplitDto splitTransaction , Boolean status) {
        DateUtil dateUtil = new DateUtil();
        this.id = splitTransaction.getId();
        this.trndate = dateUtil.getStringTODateWk(splitTransaction.getTrndate());
        this.trnno = splitTransaction.getTrnno();
        this.refid = splitTransaction.getRefid();
        this.binname = splitTransaction.getBinname();
        this.did = splitTransaction.getDept();
        this.bid = splitTransaction.getBranch();
        this.usecid = splitTransaction.getUsecid();

    }
}
