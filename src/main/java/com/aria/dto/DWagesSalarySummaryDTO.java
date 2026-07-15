package com.aria.dto;

import java.time.LocalDateTime;

import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DWagesSalarySummaryDTO {

	
    private Long allotmentId;

    private String shiftDate;
    private String shiftType;
    private Long shiftId;

    private Long workedDept;
    private String workedDeptName;
    private Long workedBranch;
    private String workedBranchName;

    private Long empId;
    private String employeeName;
    private String joiningDate;
    private String experience;
    private String sex;
    private String contract;
    private int emp_type;
    private Long daCatId;

    private Long luzone;
    private Long designationId;
    private String designationName;
    private String basic;
    private String da;
    private String allowance;
    private String total;
    private String wages;
    private Long pShiftId;
    private String status;

    private Long fromDept;
    private String fromDeptName;
    private Long fromBranch;
    private String fromBranchName;
    
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private String totalworkingdays;



    public DWagesSalarySummaryDTO(DWagesSalarySummary salarySummary) {
        DateUtil dateUtil = new DateUtil();
        this.allotmentId = salarySummary.getAllotmentId();
        if(salarySummary.getShiftDate() != null) {
            this.shiftDate = dateUtil.getDateTOStringWk(salarySummary.getShiftDate());
        }
        this.shiftType = salarySummary.getShiftType();
        this.shiftId = salarySummary.getShiftId();
        this.workedDept = salarySummary.getWorkedDept();
        this.workedDeptName = salarySummary.getWorkedDeptName();
        this.workedBranch = salarySummary.getWorkedBranch();
        this.workedBranchName = salarySummary.getWorkedBranchName();
        this.empId = salarySummary.getEmpId();
        this.contract = salarySummary.getContract();
        this.emp_type = salarySummary.getEmp_type();
        this.luzone = salarySummary.getLuzone();
        this.pShiftId = salarySummary.getPShiftId();
        this.fromBranch = salarySummary.getFromBranch();
        this.fromBranchName = salarySummary.getFromBranchName();
        this.fromDept = salarySummary.getFromDept();
        this.fromDeptName = salarySummary.getFromDeptName();

    }

}
