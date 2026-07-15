package com.aria.dto;

import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



@Getter
@Setter
@NoArgsConstructor
public class LeaveRegisterListDTO {

    private Long id;
    private Long userId;
    private String employeeName;
    private String leaveType;
    private String leaveReason;
    private String shiftDate;
    private String verifyByName;
    private String verifiedOn;
    private String approvedByName;
    private String approvedOn;
    private String appliedCategory;
    private String shiftName;
    private String status;

    public LeaveRegisterListDTO(LeaveRegisterList registerList) {
        DateUtil dateUtil = new DateUtil();
        this.id = registerList.getId();
        this.userId = registerList.getId();
        this.employeeName = registerList.getEmployeeName();
        this.leaveType = registerList.getLeaveType();
        this.leaveReason = registerList.getLeaveReason();
        this.shiftDate = dateUtil.getDateTOStringWk(registerList.getShiftDate());
        this.verifyByName = registerList.getVerifyByName();
        this.verifiedOn = dateUtil.getDateTOStringWk(registerList.getVerifiedOn());
        this.approvedByName = registerList.getApprovedByName();
        this.approvedOn = dateUtil.getDateTOStringWk(registerList.getApprovedOn());
        this.shiftName = registerList.getShiftName();
    }
}
