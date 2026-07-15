package com.aria.dto;

import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ShiftAllotmentListDto {

    private String id;

    private Long empId;
    private String empName;
    private String empCategory;

    private Long deptID;
    private String deptName;

    private Long branchId;
    private String branchName;

    private String exemptedShiftName;
    private String shiftCategory;

    private Long shiftId;
    private String exemptedShiftDate;

    public ShiftAllotmentListDto(ShiftAllotmentList shiftAllotmentList) {
        DateUtil dateUtil = new DateUtil();
        this.id = shiftAllotmentList.getId();
        this.empId = shiftAllotmentList.getEmpId();
        this.empName = shiftAllotmentList.getEmpName();
        this.empCategory = shiftAllotmentList.getEmpCategory();
        this.shiftCategory=shiftAllotmentList.getShiftCategory();
        this.deptID = shiftAllotmentList.getDeptID();
        this.deptName = shiftAllotmentList.getDeptName();
        this.branchId = shiftAllotmentList.getBranchId();
        this.branchName = shiftAllotmentList.getBranchName();
        this.exemptedShiftName = shiftAllotmentList.getExemptedShiftName();
        this.shiftId = shiftAllotmentList.getShiftId();
        if (shiftAllotmentList.getExemptedShiftDate() != null) {
            this.exemptedShiftDate = dateUtil.getDateTOStringWk(shiftAllotmentList.getExemptedShiftDate());
        }
    }
}
