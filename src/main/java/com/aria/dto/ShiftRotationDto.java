package com.aria.dto;

import com.aria.model.ShiftRotation;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
public class ShiftRotationDto {

    private Long id;

    private Long orgId;

    private Long deptID;

    private Long branchId;

    private String branchName;

    private String deptName;

    private String fromDate;

    private String toDate;

    private String category;

    private String noOfEmployees;

    private Long createdBy;

    private Long updatedBy;

    private LocalDateTime created_date;

    private LocalDateTime updated_date;
    
    private List<ShiftRotationMappingDto> shift1Staffs;

    private List<ShiftRotationMappingDto> shift2Staffs;

    private List<ShiftRotationMappingDto> shift3Staffs;

    private Long shift1_id;
    
    private Long shift2_id;

    private Long shift3_id;

    private String shiftName;


    public ShiftRotationDto(ShiftRotation shiftRotation) {
        DateUtil dateUtil = new DateUtil();
        this.id = shiftRotation.getId();
        if (shiftRotation.getOrganisation() != null) {
            this.orgId = shiftRotation.getOrganisation().getId();
        }
        if (shiftRotation.getDepartmentDetails() != null) {
            this.deptID = shiftRotation.getDepartmentDetails().getId();
            this.deptName = shiftRotation.getDepartmentDetails().getName();
        }
        if (shiftRotation.getBranch() != null) {
            this.branchId = shiftRotation.getBranch().getId();
            this.branchName = shiftRotation.getBranch().getBranchName();
        }
        this.fromDate = dateUtil.getDateTOStringWk(shiftRotation.getFromDate());
        this.toDate = dateUtil.getDateTOStringWk(shiftRotation.getToDate());

        this.shift1_id = shiftRotation.getShift1_id();
        this.shift2_id=shiftRotation.getShift2_id();
        this.shift3_id=shiftRotation.getShift3_id();
     
        this.shiftName = shiftRotation.getShiftName();
        this.noOfEmployees = shiftRotation.getNoOfEmployees();
        this.category = shiftRotation.getCategory();
        this.createdBy = shiftRotation.getCreatedBy();
        this.updatedBy = shiftRotation.getUpdatedBy();
        this.created_date = shiftRotation.getCreated_date();
        this.updated_date = shiftRotation.getUpdated_date();
    }
}
