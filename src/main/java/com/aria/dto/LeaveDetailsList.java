package com.aria.dto;


import java.util.Date;

public interface LeaveDetailsList {


    Long getId();

    Long getUserId();

    String getEmployeeName();

    String getDesignationName();

    String getLeaveType();

    Date getLeaveFrom();

    Date getLeaveTo();

    Integer getRemainingLeave();

    Integer getNoOfDays();

    String getLeaveReason();

    String getStatus();

    String getReasonForRejection();

    String getWeekOffDays();


}
