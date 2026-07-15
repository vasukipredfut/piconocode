package com.aria.model;

import java.util.Date;


public interface ListUser {

	  String getEmployeeName();

	  Long getId();
	  
	  Date getJoiningDate();
	  
	  String getDesignationName();
	  
	  Long getDesignationId();
	  
	  String getStatus();
	  
	  Long getBranchId();
	  
	  Long getDepartmentId();
	  
	  String getBranchName();
	  
	  String getDepartmentName();
	  
	  Long getWeekOffDay();
}