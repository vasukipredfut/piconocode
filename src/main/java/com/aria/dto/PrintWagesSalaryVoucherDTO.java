package com.aria.dto;

import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PrintWagesSalaryVoucherDTO {


    public String shiftDate;
    public String trnNo ;
    public Long branch;
    public String branchName ;
    public Long dept ;
    public String deptName ;
    public Long pshiftId ;
    public String pshiftName ; 
    public String trnDate ;
    public Double trnQty ;
    public Double amount ;
    public String transactionTable;
    public Long createdBy;
    public String createdByName;
    public String createdDate;
    public String createdDateTime;

    public PrintWagesSalaryVoucherDTO(PrintWagesSalaryVoucher printWagesSalaryVoucher ) {
        DateUtil dateUtil = new DateUtil();
        this.shiftDate = dateUtil.getDateTOStringWk(printWagesSalaryVoucher.getShiftDate());
        this.trnNo = printWagesSalaryVoucher.getTrnNo();
        this.branch = printWagesSalaryVoucher.getBranch();
        this.branchName = printWagesSalaryVoucher.getBranchName();
        this.dept = printWagesSalaryVoucher.getDept();
        this.deptName = printWagesSalaryVoucher.getDeptName();
        this.pshiftId = printWagesSalaryVoucher.getPshiftId();
        this.pshiftName = printWagesSalaryVoucher.getPshiftName();
        this.trnDate = dateUtil.getDateTOStringWk(printWagesSalaryVoucher.getTrnDate());
        this.trnQty = printWagesSalaryVoucher.getTrnQty();
        this.amount = printWagesSalaryVoucher.getAmount();
        this.transactionTable = printWagesSalaryVoucher.getTransactionTable();
        this.createdBy = printWagesSalaryVoucher.getCreatedBy();
        this.createdByName = printWagesSalaryVoucher.getCreatedByName();
        this.createdDate = dateUtil.getLocalDateTimeToStringDDMMYYYY1(printWagesSalaryVoucher.getCreatedDate());
        this.createdDateTime = dateUtil.getLocalDateTimeToDateTimeString(printWagesSalaryVoucher.getCreatedDate());
    }
}
