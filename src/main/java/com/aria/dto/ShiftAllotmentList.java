package com.aria.dto;


import java.util.Date;

public interface ShiftAllotmentList {


     String getId();

     Long getEmpId();
     String getEmpName();

     String getEmpCategory();

     Long getDeptID();

     String getDeptName();

     Long getBranchId();

     String getBranchName();

     String getExemptedShiftName();

     String getShiftCategory();

     Long getShiftId();
     Date getExemptedShiftDate();

}
