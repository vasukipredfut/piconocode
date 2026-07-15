package com.aria.model;

import com.aria.dto.LeaveSalaryVoucherDto;
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
@Table(name = "leave_salary_voucher")
public class LeaveSalVoucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String trnno;

    @Column
    @Temporal(TemporalType.DATE)
    private Date trndate;

    @Column
    private Long year;

    @Column
    private Long cid;

    @Column
    private Long bid;

    @Column
    private Long did;

    @Column
    private String pay;

    @Column
    private double amt;

    @Column
    private Long cby;

    @Column
    private String cst;

    @Column
    private String vtype;

    @Column
    private String  verify;

    @Column
    private Long vstaff;

    @Column
    private String vendt;

    @Column
    private Long approval;

    @Column
    private Long apstaff;

    @Column
    private String  apendt;

    @Column
    private String authenty;

    @Column
    private Long  astaff;

    @Column
    private String  aendt;

    @Column
    @Temporal(TemporalType.DATE)
    private Date vdate;

    @Column
    @Temporal(TemporalType.DATE)
    private Date adate;

    @Column
    @Temporal(TemporalType.DATE)
    private Date  apdate;

    @Column
    private String  paytype;

    @Column
    private String paydesc;

    @Column
    private int month;

    public LeaveSalVoucher(LeaveSalaryVoucherDto salaryVoucher) {
        DateUtil dateUtil = new DateUtil();
        this.id = salaryVoucher.getId();
        this.trnno = salaryVoucher.getTrnno();
        this.trndate = dateUtil.getStringTODateWk(salaryVoucher.getTrndate());
        this.year = salaryVoucher.getYear();
        this.cid = salaryVoucher.getCid();
        this.bid = salaryVoucher.getBid();
        this.did = salaryVoucher.getDid();
        this.pay =salaryVoucher.getPay();
        this.amt = salaryVoucher.getAmt();
        this.cby = salaryVoucher.getCby();
        this.cst = salaryVoucher.getCst();
        this.vtype = salaryVoucher.getVtype();
        this.paytype =salaryVoucher.getPaytype();
        this.paydesc = salaryVoucher.getPaydesc();
        this.month = salaryVoucher.getMonth();
    }
}
