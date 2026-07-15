package com.aria.dto;

import com.aria.model.CwagesTRN;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class CwagesTRNDTO {


    private Long id;


    private Date shiftdate;


    private Long trnid;


    private String trnno;


    private Long shiftid;


    private Long pshift;


    private Long empcode;


    private Double amt;


    private Long logid;


    private String usecid;


    public CwagesTRNDTO(CwagesTRN cwages) {
        this.id = cwages.getId();
        this.shiftdate = cwages.getShiftdate();
        this.trnid = cwages.getTrnid();
        this.trnno = cwages.getTrnno();
        this.shiftid = cwages.getShiftid();
        this.pshift = cwages.getPshift();
        this.empcode = cwages.getEmpcode();
        this.amt = cwages.getAmt();
        this.logid = cwages.getLogid();
        this.usecid = cwages.getUsecid();
    }
}
