package com.aria.dto;

import java.util.Date;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SalesvraBillResList {

	private Long id;
    private String billno;
    private String billdate;
    private String employeename;
    private String deductiontype;
    private String companyname;
    private String caddress;
    private String trnno;
    private Double amount;
    private String ccity;
    private String plant;
    private String date;
    private String fromDate;
    private String todate;
    public SalesvraBillResList(SalesvraBillList s) {

        this.id = s.getId();
        this.billno = s.getBillno();
        this.billdate = s.getBilldate();
        this.employeename = s.getEmployeename();
        this.deductiontype = s.getDeductiontype();
        this.companyname = s.getCompanyname();   // FIXED
        this.caddress = s.getCaddress();         // FIXED
        this.trnno = s.getTrnno();
        this.ccity = s.getCcity();               // FIXED
        this.plant = s.getPlant();               // FIXED
    }
}
