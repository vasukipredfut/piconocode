package com.aria.model;

import com.aria.dto.EsiVoucherDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "esi_voucher")
public class EsiVoucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long month;

    @Column
    private Long year;

    @Column
    private Long cid;

    @Column
    private Long upby;

    @Column
    private Long bid;

    @Column
    private String noe;

    @Column
    private String particulars;

    @Column
    private double amount;

    @Column
    private String exnoe;

    @Column
    private String exparticulars;

    @Column
    private double examount;

    @Column
    private double eesi;

    @Column
    private double desi;

    @Column
    private Long tnoe;

    @Column
    private double nettotal;

    @Column
    private double tesi;

    @Column
    private String dverify;

    @Column
    private Long dby;

    @Column
    private Date don;

    @Column
    private String pverify;

    @Column
    private Long pby;

    @Column
    private Date pon;

    @Column
    private String vouno;

    @Column
    private String auth;

    @Column
    private Long auby;

    @Column
    private Date auon;

    @Column
    private Date upon;

    @Column
    private String hoprocess;

    @Column
    private String status;


    public EsiVoucher(EsiVoucherDto voucher) {
        this.id = voucher.getId();
        this.month = voucher.getMonth();
        this.year = voucher.getYear();
        this.cid = voucher.getCid();
        this.upby = voucher.getUpby();
        this.bid = voucher.getBid();
        this.noe = voucher.getNoe();
        this.particulars = voucher.getParticulars();
        this.amount = voucher.getAmount();
        this.exnoe = voucher.getExnoe();
        this.exparticulars = voucher.getExparticulars();
        this.examount = voucher.getExamount();
        this.eesi = voucher.getEesi();
        this.desi = voucher.getDesi();
        this.tnoe = voucher.getTnoe();
        this.nettotal = voucher.getNettotal();
        this.tesi = voucher.getTesi();

        this.vouno = voucher.getVouno();

        //this.upon = voucher.getUpon();
        this.hoprocess = voucher.getHoprocess();
        this.status = voucher.getStatus();
    }
}
