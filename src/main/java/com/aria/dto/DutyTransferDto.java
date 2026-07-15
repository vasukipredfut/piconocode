package com.aria.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.aria.model.DutyTransfer;

@Getter
@Setter
@NoArgsConstructor
public class DutyTransferDto {

    private Long id;

    private Long branchId;

    private String shiftDate;

    private String assignedDate;

    private String category;

    private String shift;

    private Long shiftId;

    private String sex;

    private int active;

    private Long createdBy;

    private Long updatedBy;

    private LocalDateTime created_date;

    private LocalDateTime updated_date;

    private List<DutyTransferAssignDutyDto> assignDutyDetails;


    public DutyTransferDto(DutyTransfer dutyAllotment) {
        this.id = dutyAllotment.getId();
        this.branchId = dutyAllotment.getBranchId();
        this.category = dutyAllotment.getCategory();
        this.shift = dutyAllotment.getShift();
        this.shiftId = dutyAllotment.getShift_id();
        this.sex = dutyAllotment.getSex();
        this.active = dutyAllotment.getActive();
        this.createdBy = dutyAllotment.getCreatedBy();
        this.updatedBy = dutyAllotment.getUpdatedBy();
        this.created_date = dutyAllotment.getCreated_date();
        if (dutyAllotment.getUpdated_date() != null) {
            this.updated_date = dutyAllotment.getUpdated_date();
        } else {
            this.updated_date = dutyAllotment.getCreated_date();
        }
        this.assignDutyDetails = dutyAllotment.getAssignDutyDetails() != null
                ? dutyAllotment.getAssignDutyDetails().stream().map(field -> new DutyTransferAssignDutyDto(field)).collect(Collectors.toList()) : null;

    }
}
