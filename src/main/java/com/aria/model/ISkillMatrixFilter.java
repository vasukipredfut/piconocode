package com.aria.model;

import java.time.LocalDateTime;
import java.util.Date;

public interface ISkillMatrixFilter {

	Long getId();

	Date getDate();

	Long getBranchId();

	String getBranchName();

	Long getDepartmentId();

	String getDepartmentName();

	Long getDesignationId();

	String getDesignationName();

	Integer getSkillsCount();

	LocalDateTime getUpdatedDate();
}
