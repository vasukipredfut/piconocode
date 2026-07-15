package com.aria.model;

import com.aria.dto.AssignDutyDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "assign_duty")
public class AssignDuty {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "allotment_id")
    private Long allotmentId;


    @Column
    public Long staff_id;

    @Column
    public String contract;

    @Column(name = "current_duty")
    private String currentDuty;

    @Column(name = "assign_duty")
    private String assignDuty;

    @Column
    public Long assign_duty_id;    // ( luzoneId )

    @OneToOne
    @JoinColumn(name = "assign_branch_id", referencedColumnName = "id")
    private Branch assignBranch;

    @OneToOne
    @JoinColumn(name = "assign_dept_id", referencedColumnName = "id")
    private DepartmentDetails assignDepartment;

    @Column
    private int active;

    @Column(name = "assigned_date")
    @Temporal(TemporalType.DATE)
    private Date assignedDate;

    @Column(name = "created_by",updatable = false)
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(updatable = false)
    private LocalDateTime created_date;

    @Column
    private LocalDateTime updated_date;

    @Column
    private int emp_type;

    @Column
    public String status;



    public AssignDuty(AssignDutyDto assignDutyDetails) {
        this.id = assignDutyDetails.getId();
        this.allotmentId = assignDutyDetails.getAllotmentId();
        this.staff_id = assignDutyDetails.getStaffId();
        this.currentDuty = assignDutyDetails.getCurrentDuty();
        this.assignDuty = assignDutyDetails.getAssignDuty();
        this.assign_duty_id = assignDutyDetails.getAssign_duty_id();
        this.active = assignDutyDetails.getActive();
        this.createdBy = assignDutyDetails.getCreatedBy();
        this.updatedBy = assignDutyDetails.getUpdatedBy();
        this.created_date = assignDutyDetails.getCreated_date();
        this.updated_date = assignDutyDetails.getUpdated_date();
        this.emp_type = assignDutyDetails.getEmp_type();
        this.status = assignDutyDetails.getStatus();
    }
}
