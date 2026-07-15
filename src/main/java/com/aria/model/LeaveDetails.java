package com.aria.model;

import com.aria.dto.LeaveDetailsDto;
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
@Table(name = "leave_details")
public class LeaveDetails {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "org_id", referencedColumnName = "id")
    private Organisation organisation;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "branch_id")
    private Long branchId;

    @Column(name = "dept_id")
    private Long deptId;

    @Column(name = "employee_name")
    private String employeeName;

    @Column(name = "leave_type")
    private String leaveType;

    @Column(name = "leave_from")
    private Date leaveFrom;

    @Column(name = "leave_to")
    private Date leaveTo;

    @Column(name = "from_date_session1")
    private Integer fromDateSession1;

    @Column(name = "from_date_session2")
    private Integer fromDateSession2;

    @Column(name = "to_date_session1")
    private Integer toDateSession1;

    @Column(name = "to_date_session2")
    private Integer toDateSession2;

    @Column(name = "remaining_leave")
    private Integer remainingLeave;

    @Column(name = "no_of_days")
    private Integer noOfDays;

    @Column(name = "leave_reason")
    private String leaveReason;

    @Column
    private String status;

    @Column(name = "reason_for_rejection")
    private String reasonForRejection;

    @Column(name = "week_Off_Days")
    private String weekOffDays;

    @Column
    private int active;

    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(updatable = false)
    private LocalDateTime created_date;

    @Column
    private LocalDateTime updated_date;

    @Column
    private Long verified_by;

    @Column
    private LocalDateTime verified_date;

    @Column
    private Long approved_by;

    @Column
    private LocalDateTime approved__date;

    @Column
    private Long rejected_by;

    @Column
    private LocalDateTime rejected__date;



    public LeaveDetails(LeaveDetailsDto leaveDetails) {
        this.id = leaveDetails.getId();
        this.userId = leaveDetails.getUserId();
        this.branchId = leaveDetails.getBranchId();
        this.deptId = leaveDetails.getDeptId();
        this.employeeName = leaveDetails.getEmployeeName();
        if (leaveDetails.getLeaveType() != null) {
            this.leaveType = leaveDetails.getLeaveType().trim();
        }
        this.fromDateSession1 = leaveDetails.getFromDateSession1();
        this.fromDateSession2 = leaveDetails.getFromDateSession2();
        this.toDateSession1 = leaveDetails.getToDateSession1();
        this.toDateSession2 = leaveDetails.getToDateSession2();
        this.remainingLeave = leaveDetails.getRemainingLeave();
        this.noOfDays = leaveDetails.getNoOfDays();
        if (leaveDetails.getLeaveReason() != null) {
            this.leaveReason = leaveDetails.getLeaveReason().trim();
        }
        this.status = leaveDetails.getStatus();
        if (leaveDetails.getReasonForRejection() != null) {
            this.reasonForRejection = leaveDetails.getReasonForRejection().trim();
        }
        if(leaveDetails.getWeekOffDays() != null) {
            this.weekOffDays = leaveDetails.getWeekOffDays();
        }
        this.active = leaveDetails.getActive();
        this.createdBy = leaveDetails.getCreatedBy();
        this.updatedBy = leaveDetails.getUpdatedBy();
        this.created_date = leaveDetails.getCreated_date();
        if (leaveDetails.getUpdated_date() != null) {
            this.updated_date = leaveDetails.getUpdated_date();
        } else {
            this.updated_date = leaveDetails.getCreated_date();
        }
    }
}
