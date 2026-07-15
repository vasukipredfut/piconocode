package com.aria.model;

import com.aria.dto.CWagesSalaryReqDTO;
import com.aria.dto.CwagesSUMDTO;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "cwagessum")
public class CwagesSUM {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String trnno;

    @Column
    private String reftrnno;
    @Column
    private Long reftrnid;
    @Column
    private String reftrntable;

    @Column
    @Temporal(TemporalType.DATE)
    private Date shiftdate;
    @Column
    @Temporal(TemporalType.DATE)
    private Date trndate;

    @Column
    private Long pshift;
    @Column
    private String expref;
    @Column
    private Long refid;
    @Column
    private Double trnqty;
    @Column
    private Double qty;
    @Column
    private Double amt;
    @Column
    private Long cby;
    @Column
    private String con;
    @Column
    private Long vby;
    @Column
    private String von;
    @Column
    private Long dept;
    @Column
    private Long branch;
    @Column
    private String usecid;

    @Column
    private Double lot;

    @Column
    private String type;

    @Column(updatable = false)
    private LocalDateTime created_date;

    public CwagesSUM(CwagesSUMDTO cwages) {

        this.id = cwages.getId();
        this.trnno = cwages.getTrnno();
        this.reftrnno = cwages.getReftrnno();
        this.reftrnid = cwages.getReftrnid();
        this.reftrntable = cwages.getReftrntable();
        this.shiftdate = cwages.getShiftdate();
        this.trndate = cwages.getTrndate();
        this.pshift = cwages.getPshift();
        this.expref = cwages.getExpref();
        this.refid = cwages.getRefid();
        this.trnqty = cwages.getTrnqty();
        this.qty = cwages.getQty();
        this.amt = cwages.getAmt();
        this.cby = cwages.getCby();
        this.con = cwages.getCon();
        this.vby = cwages.getVby();
        this.von = cwages.getVon();
        this.dept = cwages.getDept();
        this.branch = cwages.getBranch();
        this.usecid = cwages.getUsecid();

    }


    public CwagesSUM(CWagesSalaryReqDTO cwages) {

        DateUtil dateUtil = new DateUtil();

        this.reftrnno = cwages.getRefTrnNo();
        this.reftrnid = cwages.getRefTrnId();
        this.reftrntable = cwages.getRefTrnTable();
        this.shiftdate = dateUtil.getStringTODateWk(cwages.getShiftDate());
        this.trndate = dateUtil.getStringTODateWk(cwages.getTrnDate());
        this.pshift = cwages.getPShift();
        this.expref = cwages.getExpref();
        this.refid = cwages.getRefid();
        this.trnqty = cwages.getTrnQty();
        this.qty = cwages.getQty();
        this.amt = cwages.getAmt();
        this.cby = cwages.getCreatedBy();
        this.dept = cwages.getDept();
        this.branch = cwages.getBranch();
        this.lot = cwages.getLot();
        this.type = cwages.getType();

    }
}
