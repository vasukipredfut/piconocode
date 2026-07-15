package com.aria.dto;

import com.aria.model.AssignDuty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class AssignDutyDto {

    private Long id;

    private Long allotmentId;

    private Long staffId;

    private String staffName;
    private String gender;

    private String currentDuty;

    private String assignDuty;
    public Long assign_duty_id;

    private int active;

    private int emp_type;

    private Long createdBy;

    private Long updatedBy;

    private LocalDateTime created_date;

    private LocalDateTime updated_date;

    public String status;
    public AssignDutyDto(AssignDuty assignDutyDetails) {
        this.id = assignDutyDetails.getId();
        this.allotmentId = assignDutyDetails.getAllotmentId();
        this.staffId = assignDutyDetails.getStaff_id();
        this.currentDuty = assignDutyDetails.getCurrentDuty();
        this.assignDuty = assignDutyDetails.getAssignDuty();
        this.assign_duty_id = assignDutyDetails.getAssign_duty_id();
        this.active = assignDutyDetails.getActive();
        this.createdBy = assignDutyDetails.getCreatedBy();
        this.updatedBy = assignDutyDetails.getUpdatedBy();
        this.created_date = assignDutyDetails.getCreated_date();
        this.emp_type = assignDutyDetails.getEmp_type();
        this.status = assignDutyDetails.getStatus();
        if (assignDutyDetails.getUpdated_date() != null) {
            this.updated_date = assignDutyDetails.getUpdated_date();
        }else {
            this.updated_date = assignDutyDetails.getCreated_date();
        }
    }
}
