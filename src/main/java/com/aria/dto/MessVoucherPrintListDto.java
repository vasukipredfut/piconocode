package com.aria.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessVoucherPrintListDto {
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
    private String voucherno;
    private String date;
    private String fromDate;
    private String todate;
    private String createdByName;
    private String verifiedByName;
    private String approvedByName;
    
    public MessVoucherPrintListDto(MessVoucherPrintList p) {
        this.id = p.getId();
        this.billno = p.getBillno();
        this.billdate = p.getBilldate();
        this.employeename = p.getEmployeename();
        this.deductiontype = p.getDeductiontype();
        this.companyname = p.getCompanyname();
        this.caddress = p.getCaddress();
        this.trnno = p.getTrnno();
        this.amount = p.getAmount();
        this.ccity = p.getCcity();
        this.plant = p.getPlant();
        this.voucherno = p.getVoucherno();
        this.createdByName = p.getCreatedByName();
        this.verifiedByName = p.getVerifiedByName();
        this.approvedByName = p.getApprovedByName();
    }
}
