package com.aria.dto;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.model.Award;
import com.aria.model.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
@Getter
@Setter
@NoArgsConstructor
public class AwardsDto {

    private Long id;

    private Long branchId;

    private String branchName;

    private Long empId;

    private String employeeName;
   
    private String awardDate;

    private String employeeCategory;

    private String designation;

    private String awardCategory;
   
    private String awardFor;

    private String achievements;
   
    private String project;

    private String awardStatus;

    private Long createdBy;

    private Long updatedBy;

    private LocalDateTime created_date;

    private LocalDateTime updated_date;

    private Map<String,Object> awardValue;

    private Long attendanceCount;

    private Long maintenanceCount;

    private Long productivityCount;

    private Long trainingCount;

    private Long invocationCount;




    public AwardsDto(Award award) {
        this.id = award.getId();
        if(award.getBranch() != null) {
            this.branchId = award.getBranch().getId();
            this.branchName = award.getBranch().getBranchName();
        }
        if(award.getUser() != null) {
            this.empId = award.getUser().getId();
            this.employeeName = award.getUser().getEmployeename();
        }
        this.employeeCategory = award.getEmployeeCategory();
        this.awardCategory = award.getAwardCategory();
        this.awardFor = award.getAwardFor();
        this.awardStatus = award.getAwardStatus();
        this.achievements = award.getAchievements();
        this.project = award.getProject();
        this.createdBy = award.getCreatedBy();
        this.updatedBy = award.getUpdatedBy();
        this.created_date = award.getCreated_date();
        this.updated_date = award.getUpdated_date();

        ObjectMapper mapper = new ObjectMapper();
        try {
            this.awardValue = mapper.readValue(award.getAwardValue(), HashMap.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public AwardsDto(User award, Boolean status) {

        if(award.getBranch_id() != null) {
            this.branchId = award.getBranch_id();
        }

        this.empId = award.getId();
        this.employeeName = award.getEmployeename();
        this.employeeCategory = award.getDesignation().getName();
    }
}
