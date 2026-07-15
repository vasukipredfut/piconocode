package com.aria.dto;

import com.aria.model.DutyAllotment;
import com.aria.model.DutyTransfer;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class DutyAllotmentDto {


    private Long id;

    private String shiftDate;

    private String category;

    private String shift;

    private Long shiftId;

    private Long branchId;

    private String branchName;

    private Long deptId;

    private String deptName;

    private int active;

    private Long createdBy;

    private Long updatedBy;

    private LocalDateTime created_date;

    private LocalDateTime updated_date;

    private List<AssignDutyDto> assignDuties;


    public DutyAllotmentDto(DutyAllotment dutyAllotment) {
        DateUtil dateUtil = new DateUtil();
        this.id = dutyAllotment.getId();
        this.category = dutyAllotment.getCategory();
        this.shift = dutyAllotment.getShift();
        this.shiftId = dutyAllotment.getShift_id();
        this.shiftDate = dateUtil.getDateTOStringWk(dutyAllotment.getShiftDate());
        if (dutyAllotment.getDepartmentDetails() != null) {
            this.deptId = dutyAllotment.getDepartmentDetails().getId();
            this.deptName = dutyAllotment.getDepartmentDetails().getName();
        }
        this.active = dutyAllotment.getActive();
        this.createdBy = dutyAllotment.getCreatedBy();
        this.updatedBy = dutyAllotment.getUpdatedBy();
        this.created_date = dutyAllotment.getCreated_date();
        if (dutyAllotment.getUpdated_date() != null) {
            this.updated_date = dutyAllotment.getUpdated_date();
        } else {
            this.updated_date = dutyAllotment.getCreated_date();
        }
        this.assignDuties = dutyAllotment.getAssignDuties() != null
                ? dutyAllotment.getAssignDuties().stream().map(field -> new AssignDutyDto(field)).collect(Collectors.toList()) : null;
    }
}
