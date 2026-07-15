package com.aria.model;

import com.aria.dto.EpfVoucherDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "epf_voucher")
public class EpfVoucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long cid;

    @Column
    private Long month;

    @Column
    private Long year;

    @Column
    private String staffsal;

    @Column
    private String tstaffsal;

    @Column
    private String dsal;

    @Column
    private String oasal;

    @Column
    private String vouno;

    @Column
    private Long  upby;

    @Column
    private Date upon;

    @Column
    private String verify;

    @Column
    private Long vby;

    @Column
    private Date von;

    @Column
    private String approve;

    @Column
    private Long apby;

    @Column
    private Date apon;

    @Column
    private String authenty;

    @Column
    private Long auby;

    @Column
    private Date auon;

    @Column
    private String hoprocess;

    @Column
    private String PMRPY_Benefit;

    @Column
    private String title;


    public EpfVoucher(EpfVoucherDto voucher) {
        this.id = voucher.getId();
        this.cid = voucher.getCid();
        this.month = voucher.getMonth();
        this.year = voucher.getYear();
        this.staffsal = voucher.getStaffsal();
        this.tstaffsal = voucher.getTstaffsal();
        this.dsal = voucher.getDsal();
        this.oasal = voucher.getOasal();
        this.vouno = voucher.getVouno();
        //this.upby = voucher.getUpby();
        //this.upon = voucher.getUpon();
        /*this.verify = voucher.getId();
        this.vby = voucher.getId();
        this.von = von;
        this.approve = approve;
        this.apby = apby;
        this.apon = apon;
        this.authenty = authenty;
        this.auby = auby;
        this.auon = auon;*/
        this.hoprocess = voucher.getHoprocess();
        this.PMRPY_Benefit = voucher.getPMRPY_Benefit();
        this.title = voucher.getTitle();
    }
}
