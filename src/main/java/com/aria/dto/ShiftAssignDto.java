package com.aria.dto;

import com.aria.model.ShiftAssign;
import com.aria.model.ShiftRotation;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
public class ShiftAssignDto {

    private Long id;

    private Long orgId;

    private Long department_id;

    private Long branch_id;

    private String branchName;

    private String deptName;

    private String fromDate;

    private String toDate;

    private Long weekOff;

    private String weekOffday;

    private Long designation_id;

    private String designation;

    private Long createdBy;

    private Long updatedBy;

    private LocalDateTime created_date;

    private LocalDateTime updated_date;

    private Long empId;

    private String employeeName;

    private Long shiftId;

    private Long shift_rotation_id;

    private String shiftName;


    public ShiftAssignDto(ShiftAssign shiftAssign) {
        this.id = shiftAssign.getId();
        if (shiftAssign.getOrganisation() != null) {
            this.orgId = shiftAssign.getOrganisation().getId();
        }
        this.department_id = shiftAssign.getDepartment_id();

        this.branch_id = shiftAssign.getBranch_id();

        if (shiftAssign.getShifts() != null) {
            this.shiftId = shiftAssign.getShifts().getId();
            this.shiftName = shiftAssign.getShifts().getShiftName();
        }

        this.empId = shiftAssign.getEmp_id();


        this.shift_rotation_id = shiftAssign.getRotation_id();

        this.weekOff = shiftAssign.getWeek_day_id();
        this.createdBy = shiftAssign.getCreatedBy();
        this.updatedBy = shiftAssign.getUpdatedBy();
        this.created_date = shiftAssign.getCreated_date();
        this.updated_date = shiftAssign.getUpdated_date();
    }
}
