package com.aria.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.dto.AwardsDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;


@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "award")
public class Award {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "branch_id", referencedColumnName = "id")
    private Branch branch;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(name = "award_date")
    private Date awardDate;

    @Column(name = "employee_category")
    private String employeeCategory;

    @Column(name = "award_category")
    private String awardCategory;

    @Column(name = "award_for")
    private String awardFor;

    @Column
    private String achievements;

    @Column
    private String project;

    @Column(name = "award_status")
    private String awardStatus;


    @Column(name = "created_by",updatable = false)
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(updatable = false)
    private LocalDateTime created_date;

    @Column
    private LocalDateTime updated_date;

    @Column(name = "award_value")
    private String awardValue;


    public Award(AwardsDto reward) {

        this.id = reward.getId();
        if(reward.getBranchId() != null) {
           Branch branch = new Branch();
           branch.setId(reward.getBranchId());
           this.branch = branch;
        }
        if(reward.getEmpId() != null) {
           User user = new User();
           user.setId(reward.getEmpId());
           this.user = user;
        }
        this.employeeCategory = reward.getEmployeeCategory();
        this.awardCategory = reward.getAwardCategory();
        this.awardFor = reward.getAwardFor();
        this.achievements = reward.getAchievements();
        this.project = reward.getProject();
        this.createdBy = reward.getCreatedBy();
        this.updatedBy = reward.getUpdatedBy();
        this.created_date = reward.getCreated_date();
        this.updated_date = reward.getUpdated_date();
        ObjectMapper mapper = new ObjectMapper();
        try {
            this.awardValue = mapper.writeValueAsString(reward.getAwardValue());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


}
