package com.aria.dto;

import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DWagesSalaryListDto {

    private String shiftDate;
    private String shiftType;
    private Long shiftId;
    private Long pshiftId;
    private String pShiftName;
    private String trnNo;
    private String trnName;
    private Long branchId;
    private Long deptId;
    private String refTrnTable;
    private String refTrnNo;
    private Long refTrnId;
    private Long itemId;
    private String status;



    public DWagesSalaryListDto(DWagesSalaryList wagesSalary) {
        DateUtil dateUtil = new DateUtil();
        this.shiftDate = dateUtil.getDateTOStringWk(wagesSalary.getShiftDate());
        this.pshiftId = wagesSalary.getPshiftId();
        this.shiftId = wagesSalary.getPshiftId();
        this.shiftType = wagesSalary.getShiftType();
        this.pShiftName = wagesSalary.getpShiftName();
        this.trnNo = wagesSalary.getTrnNo();
        this.trnName = wagesSalary.getTrnName();
        this.refTrnNo = wagesSalary.getRefTrnNo();
        this.refTrnId = wagesSalary.getRefTrnId();
        this.refTrnTable = wagesSalary.getRefTrnTable();
        this.itemId = wagesSalary.getItemId();
        this.branchId = wagesSalary.getBranchId();
        this.deptId = wagesSalary.getDeptId();
    }
}
