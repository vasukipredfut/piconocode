package com.aria.dto;


import com.aria.dao.LeaveDetailsRepository;
import com.aria.model.LeaveDetails;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class LeaveDetailsDto {

    private Long id;

    private Long userId;
    private Long branchId;
    private Long deptId;

    private String employeeName;

    private String designationName;

    private String leaveType;

    private String leaveFrom;

    private String leaveTo;

    private Integer fromDateSession1;

    private Integer fromDateSession2;

    private Integer toDateSession1;

    private Integer toDateSession2;

    private Integer remainingLeave;

    private Integer noOfDays;

    private String leaveReason;

    private String status;

    private String reasonForRejection;

    private int active;

    private Long createdBy;

    private Long updatedBy;

    private Long approveBy;

    private String approveByName;

    private LocalDateTime created_date;

    private LocalDateTime updated_date;

    private String weekOffDays;


    private Long verifiedBy;
    private String verifiedByName;
    private String verifiedDate;
    private Long approvedBy;
    private String approvedByName;
    private String approvedDate;

    private Long rejectedBy;
    private String rejectedByName;
    private String rejectedDate;

    public LeaveDetailsDto(LeaveDetails leaveDetails) {
        DateUtil dateUtil = new DateUtil();
        this.id = leaveDetails.getId();
        this.userId = leaveDetails.getUserId();
        this.employeeName = leaveDetails.getEmployeeName();
        this.leaveType = leaveDetails.getLeaveType();
        this.remainingLeave = leaveDetails.getRemainingLeave();
        this.noOfDays = leaveDetails.getNoOfDays();
        this.fromDateSession1 = leaveDetails.getFromDateSession1();
        this.fromDateSession2 = leaveDetails.getFromDateSession2();
        this.toDateSession1 = leaveDetails.getToDateSession1();
        this.toDateSession2 = leaveDetails.getToDateSession2();
        this.leaveReason = leaveDetails.getLeaveReason();
        this.status = leaveDetails.getStatus();
        this.reasonForRejection = leaveDetails.getReasonForRejection();
        this.weekOffDays = leaveDetails.getWeekOffDays();
        this.active = leaveDetails.getActive();
        this.createdBy = leaveDetails.getCreatedBy();
        this.updatedBy = leaveDetails.getUpdatedBy();
        this.created_date = leaveDetails.getCreated_date();
        if (leaveDetails.getUpdated_date() != null) {
            this.updated_date = leaveDetails.getUpdated_date();
        } else {
            this.updated_date = leaveDetails.getCreated_date();
        }

        this.verifiedBy = leaveDetails.getVerified_by();
        if (leaveDetails.getVerified_date() != null) {
            this.verifiedDate = dateUtil.getLocalDateTimeToDateString(leaveDetails.getVerified_date());
        }

        this.approvedBy = leaveDetails.getApproved_by();
        if (leaveDetails.getApproved__date() != null) {
            this.approvedDate = dateUtil.getLocalDateTimeToDateString(leaveDetails.getApproved__date());
        }

        this.rejectedBy = leaveDetails.getRejected_by();
        if (leaveDetails.getRejected__date() != null) {
            this.rejectedDate = dateUtil.getLocalDateTimeToDateString(leaveDetails.getRejected__date());
        }
    }


    public LeaveDetailsDto(LeaveDetailsList leaveDetails, LeaveDetailsRepository leaveDetailsRepository) {

        DateUtil dateUtil = new DateUtil();

        this.id = leaveDetails.getId();
        this.userId = leaveDetails.getUserId();
        this.employeeName = leaveDetails.getEmployeeName();
        this.leaveType = leaveDetails.getLeaveType();
        this.remainingLeave = leaveDetails.getRemainingLeave();
        this.noOfDays = leaveDetails.getNoOfDays();
        this.leaveReason = leaveDetails.getLeaveReason();
        this.status = leaveDetails.getStatus();
        this.reasonForRejection = leaveDetails.getReasonForRejection();
        this.weekOffDays = leaveDetails.getWeekOffDays();
        this.designationName = leaveDetails.getDesignationName();

        LeaveDetails opLeaveDetails = leaveDetailsRepository.findLeaveDetailsById(leaveDetails.getId());
        if(opLeaveDetails != null) {
            this.createdBy = opLeaveDetails.getCreatedBy();
            if (opLeaveDetails.getUpdated_date() != null) {
                this.updated_date = opLeaveDetails.getUpdated_date();
            } else {
                this.updated_date = opLeaveDetails.getCreated_date();
            }
            this.verifiedBy = opLeaveDetails.getVerified_by();
            if (opLeaveDetails.getVerified_date() != null) {
                this.verifiedDate = dateUtil.getLocalDateTimeToDateString(opLeaveDetails.getVerified_date());
            }
            this.approvedBy = opLeaveDetails.getApproved_by();
            if (opLeaveDetails.getApproved__date() != null) {
                this.approvedDate = dateUtil.getLocalDateTimeToDateString(opLeaveDetails.getApproved__date());
            }
            this.rejectedBy = opLeaveDetails.getRejected_by();
            if (opLeaveDetails.getRejected__date() != null) {
                this.rejectedDate = dateUtil.getLocalDateTimeToDateString(opLeaveDetails.getRejected__date());
            }
        }
    }
}
