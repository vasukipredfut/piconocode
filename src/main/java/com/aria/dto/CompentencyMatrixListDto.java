package com.aria.dto;

import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CompentencyMatrixListDto {

    private Long id;

    private Long designationId;

    private String designation;

    private Long deptId;

    private String departmentName;

    private String date;

    private String skills;

    private Object skillCount;



    public CompentencyMatrixListDto(CompentencyMatrixList compentencyMatrixList) {
        DateUtil dateUtil = new DateUtil();
        this.id = compentencyMatrixList.getId();
        this.designationId = compentencyMatrixList.getDesignationId();
        this.designation = compentencyMatrixList.getDesignation();
        this.deptId = compentencyMatrixList.getDeptId();
        this.departmentName = compentencyMatrixList.getDepartmentName();
        this.date = dateUtil.getDateTOStringWk(compentencyMatrixList.getDate());
        this.skills = compentencyMatrixList.getSkills();
        this.skillCount = compentencyMatrixList.getSkillCount();
    }
}
