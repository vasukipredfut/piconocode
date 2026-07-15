package com.aria.model;

import com.aria.dto.ShiftRotationDto;
import com.aria.utils.DateUtil;
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
@Table(name = "shift_rotation")
public class ShiftRotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "org_id", referencedColumnName = "id")
    private Organisation organisation;

    @OneToOne
    @JoinColumn(name = "dept_id", referencedColumnName = "id")
    private DepartmentDetails departmentDetails;

    @OneToOne
    @JoinColumn(name = "branch_id", referencedColumnName = "id")
    private Branch branch;

    @Column(name = "shift_name")
    private String shiftName;

    @Column(name = "from_Date")
    private Date fromDate;

    @Column(name = "to_Date")
    private Date toDate;

    @Column(name = "no_Of_Employees")
    private String noOfEmployees;

    @Column
    private Long shift1_id;

    @Column
    private Long shift2_id;
    
    @Column
    private Long shift3_id;


    @Column(name = "category")
    private String category;

    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(updatable = false)
    private LocalDateTime created_date;

    @Column
    private LocalDateTime updated_date;


    public ShiftRotation(ShiftRotationDto shiftRotation) {
        DateUtil dateUtil = new DateUtil();
        this.id = shiftRotation.getId();
        if (shiftRotation.getOrgId() != null) {
            Organisation organisation = new Organisation();
            organisation.setId(shiftRotation.getOrgId());
            this.organisation = organisation;
        }
        if (shiftRotation.getDeptID() != null) {
            DepartmentDetails departmentDetails = new DepartmentDetails();
            departmentDetails.setId(shiftRotation.getDeptID());
            this.departmentDetails = departmentDetails;
        }
        if (shiftRotation.getBranchId() != null) {
            Branch branch = new Branch();
            branch.setId(shiftRotation.getBranchId());
            this.branch = branch;
        }

        this.fromDate = dateUtil.getStringTODateWk(shiftRotation.getFromDate());

        this.toDate = dateUtil.getStringTODateWk(shiftRotation.getToDate());

        this.shiftName = shiftRotation.getShiftName();
        this.noOfEmployees = shiftRotation.getNoOfEmployees();
        this.category = shiftRotation.getCategory();

        this.createdBy = shiftRotation.getCreatedBy();
        this.updatedBy = shiftRotation.getUpdatedBy();
        this.created_date = shiftRotation.getCreated_date();
        this.updated_date = shiftRotation.getUpdated_date();
    }
}
