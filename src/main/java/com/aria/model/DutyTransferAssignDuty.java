package com.aria.model;


import com.aria.dto.DutyTransferAssignDutyDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;


@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "duty_transfer_assign_duty")
public class DutyTransferAssignDuty {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transfer_id")
    private Long transferId;

    @OneToOne
    @JoinColumn(name = "org_id", referencedColumnName = "id")
    private Organisation organisation;

    @OneToOne
    @JoinColumn(name = "current_dept_id", referencedColumnName = "id")
    private DepartmentDetails currentDepartment;

    @OneToOne
    @JoinColumn(name = "current_branch_id", referencedColumnName = "id")
    private Branch currentBranch;

    @OneToOne
    @JoinColumn(name = "assign_dept_id", referencedColumnName = "id")
    private DepartmentDetails assignDepartment;

    @OneToOne
    @JoinColumn(name = "assign_branch_id", referencedColumnName = "id")
    private Branch assignBranch;

    /*@OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    public User staff;*/

    @Column
    public Long user_id;

    @Column(name = "created_by",updatable = false)
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(updatable = false)
    private LocalDateTime created_date;

    @Column
    private LocalDateTime updated_date;

    @Column
    private int emp_type;

    public DutyTransferAssignDuty(DutyTransferAssignDutyDto assignDutyDetails) {
        this.id = assignDutyDetails.getId();
        this.transferId = assignDutyDetails.getTransferId();
        if(assignDutyDetails.getCurrentBranchId() != null){
            Branch branch = new Branch();
            branch.setId(assignDutyDetails.getCurrentBranchId());
            this.currentBranch = branch;
        }
        if(assignDutyDetails.getCurrentDeptId() != null){
            DepartmentDetails details = new DepartmentDetails();
            details.setId(assignDutyDetails.getCurrentDeptId());
            this.currentDepartment = details;
        }
        if(assignDutyDetails.getAssignBranchId() != null){
            Branch branch = new Branch();
            branch.setId(assignDutyDetails.getAssignBranchId());
            this.assignBranch = branch;
        }
        if(assignDutyDetails.getAssignDeptId() != null){
            DepartmentDetails details = new DepartmentDetails();
            details.setId(assignDutyDetails.getAssignDeptId());
            this.assignDepartment = details;
        }
       /* if(assignDutyDetails.getStaffId() != null){
            User details = new User();
            details.setId(assignDutyDetails.getStaffId());
            this.staff = details;
        }*/
        this.user_id = assignDutyDetails.getStaffId();
        this.createdBy = assignDutyDetails.getCreatedBy();
        this.updatedBy = assignDutyDetails.getUpdatedBy();
        this.created_date = assignDutyDetails.getCreated_date();
        this.updated_date = assignDutyDetails.getUpdated_date();
        this.emp_type = assignDutyDetails.getEmp_type();
    }
}
