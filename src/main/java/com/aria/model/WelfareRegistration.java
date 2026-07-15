
package com.aria.model;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

import com.aria.dto.WelfareRegistrationDto;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "welfare_registration")
public class WelfareRegistration {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "dept_id", referencedColumnName = "id")
    private DepartmentDetails departmentDetails;

    @OneToOne
    @JoinColumn(name = "branch_id", referencedColumnName = "id")
    private Branch branch;


    @Column(name = "welfare_Name")
    private String welfareName;

    @Column(name = "from_Date")
    private Date fromDate;

    @Column(name = "to_Date")
    private Date toDate;

    @Column(name = "employee_Type")
    private String employeeType;

    @Column(name = "select_Employee")
    private String selectEmployee;

    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(updatable = false)
    private LocalDateTime created_date;

    @Column
    private LocalDateTime updated_date;

    @OneToMany(mappedBy = "empId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<WelfareEmployee> welfareEmployee;

    public WelfareRegistration(WelfareRegistrationDto welfareRegistration) {
        this.id = welfareRegistration.getId();
        if (welfareRegistration.getDeptID() != null) {
            DepartmentDetails departmentDetails = new DepartmentDetails();
            departmentDetails.setId(welfareRegistration.getDeptID());
            this.departmentDetails = departmentDetails;
        }
        if (welfareRegistration.getDeptID() != null) {
            Branch branch = new Branch();
            branch.setId(welfareRegistration.getBranchId());
            this.branch = branch;
        }
        
        this.welfareName = welfareRegistration.getWelfareName();
        this.employeeType = welfareRegistration.getEmployeeType();
        this.selectEmployee = welfareRegistration.getSelectEmployee();
        this.createdBy = welfareRegistration.getCreatedBy();
        this.updatedBy = welfareRegistration.getUpdatedBy();
        this.created_date = welfareRegistration.getCreated_date();
        this.updated_date = welfareRegistration.getUpdated_date();
    }
}
