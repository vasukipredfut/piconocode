package com.aria.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.aria.model.ShiftAllotment;
import com.aria.model.ShiftRotationMapping;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;

@Getter
@Setter
@NoArgsConstructor
public class ShiftAllocateMappingDto {

    private String id;

    private Long deptId;

    private String departmentName;

    private Long branchId;

    private Long orgId;

    private Long shiftId;

    private Boolean staffEmployee;

    private Long createdBy;

    private Long updatedBy;

    private Long empId;

    private String employeeName;

    private LocalDateTime created_date;

    private LocalDateTime updated_date;

    private int emp_type;


    public ShiftAllocateMappingDto(ShiftAllotment shiftAllotment) {
        this.deptId = shiftAllotment.getDeptId();
        this.branchId = shiftAllotment.getBranchId();
        this.empId = shiftAllotment.getEmpId();
        this.staffEmployee = shiftAllotment.getStaffemployee();
        this.shiftId = shiftAllotment.getShiftId();
        this.id = shiftAllotment.getId();
        this.orgId = shiftAllotment.getOrgId();
        this.createdBy = shiftAllotment.getCreatedBy();
        this.updatedBy = shiftAllotment.getUpdatedBy();
        this.created_date = shiftAllotment.getCreated_date();
        this.updated_date = shiftAllotment.getUpdated_date();
        this.emp_type = shiftAllotment.getEmp_type();
    }

}
