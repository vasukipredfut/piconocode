package com.aria.dto;

import com.aria.model.CwagesSUM;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;


@Getter
@Setter
@NoArgsConstructor
public class CwagesSUMDTO {

    private Long id;

   
    private String trnno;
   
    private String reftrnno;
   
    private Long reftrnid;
   
    private String reftrntable;

   
    private Date shiftdate;
   
    private Date trndate;

   
    private Long pshift;
   
    private String expref;
   
    private Long refid;
   
    private Double trnqty;
   
    private Double qty;
   
    private Double amt;
   
    private Long cby;
   
    private String con;
   
    private Long vby;
   
    private String von;
   
    private Long dept;
   
    private Long branch;
   
    private String usecid;

    public CwagesSUMDTO(CwagesSUM cwages) {
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
}
