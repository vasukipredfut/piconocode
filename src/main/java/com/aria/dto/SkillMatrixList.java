package com.aria.dto;

import java.util.Date;

public interface SkillMatrixList {

    Long getId();

    Long getDesignationId();

    String getDesignation();

    Long getDeptId();

    String getDepartmentName();

    Date getDate();
    Date getUpdatedDate();

    String getSkills();
    Object getSkillCount();
}
