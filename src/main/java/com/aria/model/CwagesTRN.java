package com.aria.model;

import com.aria.dto.CWagesSalaryEmployeeDTO;
import com.aria.dto.CWagesSalaryReqDTO;
import com.aria.dto.CwagesTRNDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "cwagestrn")
public class CwagesTRN {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    @Temporal(TemporalType.DATE)
    private Date shiftdate;

    @Column
    private Long trnid;

    @Column
    private String trnno;

    @Column
    private Long shiftid;

    @Column
    private Long pshift;

    @Column
    private Long empcode;

    @Column
    private Double amt;

    @Column
    private Long logid;

    @Column
    private String usecid;

    public CwagesTRN(CwagesTRNDTO cwages) {
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

    public CwagesTRN(CwagesSUM cwagesSUM, CWagesSalaryReqDTO cWagesSalaryReqDTO , CWagesSalaryEmployeeDTO cwages) {
        //this.id = cwagesSUM.getId();
        this.shiftdate = cwagesSUM.getShiftdate();
        this.trnid = cwagesSUM.getId();
        this.trnno = cwagesSUM.getTrnno();
        this.shiftid = cWagesSalaryReqDTO.getShiftId();
        this.pshift = cwagesSUM.getPshift();
        this.empcode = cwages.getEmpCode();
        this.amt = cwages.getAmt();
    }
}
