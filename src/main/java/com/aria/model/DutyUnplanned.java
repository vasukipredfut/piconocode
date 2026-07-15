package com.aria.model;


import com.aria.dto.DutyUnplannedDto;

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
@Table(name = "duty_unplanned")
public class DutyUnplanned {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "dept_id", referencedColumnName = "id")
    private DepartmentDetails departmentDetails;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(name = "employee_category")
    private String employeeCategory;

    @Column(name = "duty_From")
    private Date dutyFrom;

    @Column(name = "dutyTo")
    private Date dutyTo;
    
    @Column
    private String dutyname;

    @Column
    private String dutydescription;

    @Column(name = "created_by",updatable = false)
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(updatable = false)
    private LocalDateTime created_date;

    @Column
    private LocalDateTime updated_date;

    public DutyUnplanned(DutyUnplannedDto dutyUnplanned) {
        this.id = dutyUnplanned.getId();
        if(dutyUnplanned.getDeptId() != null){
            DepartmentDetails departmentDetails = new DepartmentDetails();
            departmentDetails.setId(dutyUnplanned.getDeptId());
            this.departmentDetails = departmentDetails;
        }
        if(dutyUnplanned.getUserId() != null){
        	User user = new User();
            user.setId(dutyUnplanned.getUserId());
            this.user = user;
        }
		/*
		 * if(dutyUnplanned.getId() != null){ User user = new User();
		 * user.setId(dutyUnplanned.getId()); this.user = user; }
		 */
        this.employeeCategory= dutyUnplanned.getEmployeeCategory();
        this.dutyname = dutyUnplanned.getDutyName();
        this.dutydescription = dutyUnplanned.getDutyDescription();
        this.createdBy = dutyUnplanned.getCreatedBy();
        this.updatedBy = dutyUnplanned.getUpdatedBy();
        this.created_date = dutyUnplanned.getCreated_date();
        this.updated_date = dutyUnplanned.getUpdated_date();
    }
}