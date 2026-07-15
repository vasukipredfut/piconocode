package com.aria.model;

import com.aria.dto.SalaryVoucherDto;
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
@Table(name = "salary_voucher")
public class SalaryVoucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private Long company;
    @Column
    private Long  formid;
    @Column
    private String  vouno;
    @Column
    @Temporal(TemporalType.DATE)
    private Date voudt;
    @Column
    private double amt;
    @Column
    private Long voushift;
    @Column
    private String trnno;
    @Column
    private String inv;
    @Column
    @Temporal(TemporalType.DATE)
    private Date idate;
    @Column
    private Long istaff;
    @Column
    private String verify;
    @Column
    @Temporal(TemporalType.DATE)
    private Date vdate;
    @Column
    private Long vstaff;
    @Column
    private String approval;
    @Column
    @Temporal(TemporalType.DATE)
    private Date apdate;
    @Column
    private Long apstaff;
    @Column
    private Long authenty;
    @Column
    @Temporal(TemporalType.DATE)
    private Date adate;
    @Column
    private Long astaff;
    @Column
    private String hoprocess;
    @Column
    @Temporal(TemporalType.DATE)
    private Date hdate;
    @Column
    private Long hstaff;
    @Column
    private String payment;
    @Column
    @Temporal(TemporalType.DATE)
    private Date pdate;
    @Column
    private Long pstaff;
    @Column
    private String cform;
    @Column
    @Temporal(TemporalType.DATE)
    private Date cformdt;
    @Column
    private Long cfstaff;
    @Column
    private String form17;
    @Column
    @Temporal(TemporalType.DATE)
    private Date f17dt;
    @Column
    private Long f17staff;
    @Column
    private Long department;
    @Column
    private Date bentdt;
    @Column
    private Long vtype;
    @Column
    private Long items;

    @Column
    private Long itemtype;

    @Column
    private Date entry;

    @Column
    private String bendt;
    @Column
    private String iendt;
    @Column
    private String vendt;
    @Column
    private String apendt;
    @Column
    private String aendt;
    @Column
    private Long branch;
    @Column
    private Long dept;
    @Column
    private Long trnyear;
    @Column
    private String trnmonth;
    @Column
    private Long trnweekid;
    @Column
    @Temporal(TemporalType.DATE)
    private Date fmperiod;
    @Column
    @Temporal(TemporalType.DATE)
    private Date toperiod;
    @Column
    private String usecid;
    @Column
    private String vgrp;
    @Column
    private Long incid;
    @Column
    private String descx;

    @Column
    private String designation;

    @Column
    private String trntype;

    @Column
    private int formT;

    @Column
    private int formR;

    @Column
    private String description;


    public SalaryVoucher(SalaryVoucherDto salaryVoucher) {
        DateUtil dateUtil = new DateUtil();
        this.id = salaryVoucher.getId();
        this.company = salaryVoucher.getCompany();
        this.formid = salaryVoucher.getFormid();
        this.vouno = salaryVoucher.getVouno();
        this.amt = salaryVoucher.getAmt();
        this.voushift = salaryVoucher.getVoushift();
        this.trnno = salaryVoucher.getTrnno();
        this.inv =salaryVoucher.getInv();
        this.idate = dateUtil.getStringTODateWk(salaryVoucher.getIdate());
        this.istaff = salaryVoucher.getIstaff();
        this.hoprocess = salaryVoucher.getHoprocess();
        this.hdate = dateUtil.getStringTODateWk(salaryVoucher.getIdate());
        this.hstaff = salaryVoucher.getIstaff();
        this.payment = salaryVoucher.getPayment();
        this.pdate = dateUtil.getStringTODateWk(salaryVoucher.getIdate());
        this.pstaff = salaryVoucher.getIstaff();
        this.cform = salaryVoucher.getCform();
        this.cformdt = dateUtil.getStringTODateWk(salaryVoucher.getCformdt());
        this.cfstaff = salaryVoucher.getCfstaff();
        this.form17 = salaryVoucher.getForm17();
        this.f17dt = dateUtil.getStringTODateWk(salaryVoucher.getF17dt());
        this.f17staff = salaryVoucher.getF17staff();
        this.department = salaryVoucher.getDepartment();
        this.bentdt = dateUtil.getStringTODateWk(salaryVoucher.getBentdt());
        this.vtype = salaryVoucher.getVtype();
        this.items = salaryVoucher.getItems();
        this.itemtype = salaryVoucher.getItemtype();
       /* this.entry = salaryVoucher.getEntry();
       this.bendt = salaryVoucher.getBendt();
        this.iendt = salaryVoucher.getIendt();
        this.vendt = salaryVoucher.getVendt();
        this.apendt = salaryVoucher.getApendt();
        this.aendt = salaryVoucher.getAendt();*/
        this.branch = salaryVoucher.getBranch();
        this.dept = salaryVoucher.getDept();
        this.trnyear = salaryVoucher.getTrnyear();
        this.trnmonth = salaryVoucher.getTrnmonth();
        this.trnweekid = salaryVoucher.getTrnweekid();
        this.fmperiod = dateUtil.getStringTODateWk(salaryVoucher.getFmperiod());
        this.toperiod = dateUtil.getStringTODateWk(salaryVoucher.getToperiod());
        this.usecid = salaryVoucher.getUsecid();
        this.vgrp = salaryVoucher.getVgrp();
        this.incid = salaryVoucher.getIncid();
        this.descx = salaryVoucher.getDescx();
        this.designation = salaryVoucher.getDesignation();
        this.trntype = salaryVoucher.getTrntype();
        this.formT = salaryVoucher.getFormT();
        this.formR = salaryVoucher.getFormR();
        this.description = salaryVoucher.getDescription();
    }
}
