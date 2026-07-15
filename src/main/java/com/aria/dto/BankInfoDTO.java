package com.aria.dto;

import com.aria.model.BankInfo;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



@Getter
@Setter
@NoArgsConstructor
public class BankInfoDTO {

    private Long id;

   
    private Long vendorId;

   
    private String connectTable;

   
    private String vendorName;

   
    private String accountNo;

   
    private String ifscCode;

   
    private Long bankId;

   
    private String branch;

   
    private String bank;

   
    private String usertime;

   
    private String status;

   
    private String lastmodified;

    public BankInfoDTO(BankInfo bankInfo) {
        DateUtil dateUtil = new DateUtil();
        this.id = bankInfo.getId();
        this.vendorId = bankInfo.getId();
        this.connectTable = bankInfo.getVtable();
        this.vendorName = bankInfo.getName();
        this.accountNo = bankInfo.getAccno();
        this.ifscCode = bankInfo.getIfsc();
        this.bankId = bankInfo.getBankid();
        this.branch = bankInfo.getBranch();
        this.bank = bankInfo.getBank();
        this.usertime = bankInfo.getUsertime();
        this.status = bankInfo.getStatus();
        this.lastmodified = dateUtil.getLocalDateTimeToDateTimeString(bankInfo.getLastmodified());
    }
}
