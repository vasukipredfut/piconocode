package com.aria.dto;


import com.aria.model.DutyTransferAssignDuty;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class DutyTransferAssignDutyDto {

    private Long id;

    private Long transferId;

    private Long staffId;

    private String employeeName;

    private Long currentDeptId;

    private String currentDeptName;

    private Long currentBranchId;

    private String currentBranchName;

    private Long assignDeptId;

    private String assignDeptName;

    private Long assignBranchId;

    private String assignBranchName;

    private String assignedDate;

    private Long createdBy;

    private Long updatedBy;

    private LocalDateTime created_date;

    private LocalDateTime updated_date;

    private int emp_type;

    public DutyTransferAssignDutyDto(DutyTransferAssignDuty assignDutyDetails) {
        this.id = assignDutyDetails.getId();
        this.transferId = assignDutyDetails.getTransferId();
        /*if (assignDutyDetails.getStaff() != null) {
            this.staffId = assignDutyDetails.getStaff().getId();
            this.employeeName = assignDutyDetails.getStaff().getEmployeename();
        }*/
        this.staffId = assignDutyDetails.getUser_id();
        if (assignDutyDetails.getCurrentBranch() != null) {
            this.currentBranchId = assignDutyDetails.getCurrentBranch().getId();
            this.currentBranchName = assignDutyDetails.getCurrentBranch().getBranchName();
        }
        if (assignDutyDetails.getCurrentDepartment() != null) {
            this.currentDeptId = assignDutyDetails.getCurrentDepartment().getId();
            this.currentDeptName = assignDutyDetails.getCurrentDepartment().getName();
        }
        if (assignDutyDetails.getAssignBranch() != null) {
            this.assignBranchId = assignDutyDetails.getAssignBranch().getId();
            this.assignBranchName = assignDutyDetails.getAssignBranch().getBranchName();
        }
        if (assignDutyDetails.getAssignDepartment() != null) {
            this.assignDeptId = assignDutyDetails.getAssignDepartment().getId();
            this.assignDeptName = assignDutyDetails.getAssignDepartment().getName();
        }
        this.emp_type = assignDutyDetails.getEmp_type();
        this.createdBy = assignDutyDetails.getCreatedBy();
        this.updatedBy = assignDutyDetails.getUpdatedBy();
        this.created_date = assignDutyDetails.getCreated_date();
        if (assignDutyDetails.getUpdated_date() != null) {
            this.updated_date = assignDutyDetails.getUpdated_date();
        } else {
            this.updated_date = assignDutyDetails.getCreated_date();
        }
    }


    public DutyTransferAssignDutyDto(DutyTransferAssignDutyList dutyList) {
        DateUtil dateUtil = new DateUtil();
        this.id = dutyList.getTransferId();
        this.transferId = dutyList.getTransferId();
        this.staffId = dutyList.getStaffId();
        this.employeeName = dutyList.getEmployeeName();
        this.currentDeptId = dutyList.getCurrentDeptId();
        this.currentDeptName = dutyList.getCurrentDeptName();
        this.currentBranchId = dutyList.getCurrentBranchId();
        this.currentBranchName = dutyList.getCurrentBranchName();
        this.assignDeptId = dutyList.getAssignDeptId();
        this.assignDeptName = dutyList.getAssignDeptName();
        this.assignBranchId = dutyList.getAssignBranchId();
        this.assignBranchName = dutyList.getAssignBranchName();
        if (dutyList.getAssignedDate() != null) {
            this.assignedDate = dateUtil.getDateTOStringWk(dutyList.getAssignedDate());
        }
    }
}
