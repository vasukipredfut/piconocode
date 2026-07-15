package com.aria.utils;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class AwardUtil {


    public Map<String, Object> getAwardValue(String category) {
        if (category.equalsIgnoreCase("Attendance")) {
            return getAttendanceAward();
        } else if (category.equalsIgnoreCase("Training")) {
            return getTrainingAward();
        } else if (category.equalsIgnoreCase("Innovation")) {
            return new HashMap<>();
        } else if (category.equalsIgnoreCase("Productivity")) {
            return getProductDetailsAward();
        } else if (category.equalsIgnoreCase("Maintainance")) {
            return getMaintenanceDetailsAward();
        } else {
            return new HashMap<>();
        }
    }


    public Map<String, Object> getAttendanceAward() {
        Map<String, Object> stringObjectMap = new HashMap<>();
        stringObjectMap.put("EmployeeName", "ALAGU");
        stringObjectMap.put("EmployeeID", "1");
        stringObjectMap.put("Month", "Jan 2022");
        stringObjectMap.put("NoOfWorkingDays", "26");
        stringObjectMap.put("WorkedDays", "23");
        stringObjectMap.put("LeaveDays", "03");
        return stringObjectMap;
    }


    public Map<String, Object> getTrainingAward() {
        Map<String, Object> stringObjectMap = new HashMap<>();
        stringObjectMap.put("EmployeeName", "ALAGU");
        stringObjectMap.put("EmployeeID", "1");
        stringObjectMap.put("Month", "26");
        stringObjectMap.put("Training1", "23");
        stringObjectMap.put("LeaveDays", "03");
        return stringObjectMap;
    }


    public Map<String, Object> getProductDetailsAward() {
        Map<String, Object> stringObjectMap = new HashMap<>();
        stringObjectMap.put("EmployeeName", "ALAGU");
        stringObjectMap.put("EmployeeID", "1");
        stringObjectMap.put("Date", "26/10/2022");
        stringObjectMap.put("Project", "Production");
        stringObjectMap.put("OutputProduct", "Oil");
        stringObjectMap.put("TargetMin", "344");
        stringObjectMap.put("TargetMax", "456");
        stringObjectMap.put("Achieved", "400");
        return stringObjectMap;
    }


    public Map<String, Object> getMaintenanceDetailsAward() {
        Map<String, Object> stringObjectMap = new HashMap<>();
        stringObjectMap.put("EmployeeName", "ALAGURAJA");
        stringObjectMap.put("EmployeeID", "1");
        stringObjectMap.put("Department", "Production");
        stringObjectMap.put("MachineAlloted", "Crusher - 0987");
        stringObjectMap.put("MaintainanceReq", "4 Times/Mon");
        stringObjectMap.put("MaintainanceDone", "Daily Maintainance");
        stringObjectMap.put("MachineBreakdown", "1Times");
        return stringObjectMap;
    }
}
