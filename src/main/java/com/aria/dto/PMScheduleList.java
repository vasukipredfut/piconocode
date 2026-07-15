package com.aria.dto;



import java.util.Date;

public interface PMScheduleList {


     Long getId() ;
     String getName();
     Long getScheduleId();
     String getScheduleName();
     Long getTaskCategoryId();
     String getTaskCategoryName();
     Long getTaskGroupId();
     String getTaskGroupName();
     Long getMachineId() ;
     String getMachineName();
     Long getPanelId();
     String getPanelName();
     Date getLastCompleteDate();
     Date getScheduleDate();
     String getStatus();
}
