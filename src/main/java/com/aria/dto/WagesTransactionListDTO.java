package com.aria.dto;

import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WagesTransactionListDTO {

    private String shiftDate;
    private Long shiftId;
    private Long pshift;
    private String shiftType;
    private String pshiftName;
    private String deptName;
    private String branchName;
    private Long deptId;
    private Long daCatId;
    private String category;
    private Long branchId;


    public WagesTransactionListDTO(WagesTransactionList wagesTransactionList) {
        DateUtil dateUtil = new DateUtil();
        this.shiftDate = dateUtil.getDateTOStringWk(wagesTransactionList.getShiftDate());
        this.shiftId = wagesTransactionList.getShiftId();
        this.pshift = wagesTransactionList.getPshift();
        this.shiftType = wagesTransactionList.getShiftType();
        this.pshiftName = wagesTransactionList.getPshiftName();
        this.deptName = wagesTransactionList.getDeptName();
        this.branchName = wagesTransactionList.getBranchName();
        this.branchId = wagesTransactionList.getBranchId();
        this.deptId = wagesTransactionList.getDeptId();
        this.daCatId = wagesTransactionList.getDaCatId();
        if (wagesTransactionList.getDaCatId() != null && wagesTransactionList.getDaCatId() == 6) {
            category = "WagesApprentice";
        } else {
            category = "Wages";
        }

    }
}
