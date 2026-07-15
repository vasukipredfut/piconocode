package com.aria.model;

import com.aria.dto.ShiftAllocateMappingDto;
import com.aria.utils.Hashing;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;


@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "shift_allotment")
public class ShiftAllotment {
	
	@Id
	private String id;

    @Column(name = "org_Id")
    private Long orgId;

    @Column(name = "dept_Id")
    private Long deptId;

    @Column(name = "branch_Id")
    private Long branchId;

	@Column(name = "shift_id")
    private Long shiftId;
	
	@Column(name = "emp_id")
    private Long empId;
	
	@Column(name = "staffemployee")
    private Boolean staffemployee;


    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(updatable = false)
    private LocalDateTime created_date;

    @Column
    private LocalDateTime updated_date;

    @Column
    private int emp_type;

    public ShiftAllotment(ShiftAllocateMappingDto shiftAllotment) throws NoSuchAlgorithmException {
        this.deptId = shiftAllotment.getDeptId();
        this.branchId=shiftAllotment.getBranchId();
        this.empId=shiftAllotment.getEmpId();
        this.shiftId=shiftAllotment.getShiftId();
        this.staffemployee=shiftAllotment.getStaffEmployee();
        this.id=Hashing.hash("Employee:"+empId, "shift"+shiftId, staffemployee);
        this.orgId=shiftAllotment.getOrgId();
        this.createdBy = shiftAllotment.getCreatedBy();
        this.updatedBy = shiftAllotment.getUpdatedBy();
        this.created_date = shiftAllotment.getCreated_date();
        this.updated_date = shiftAllotment.getUpdated_date();
        this.emp_type = shiftAllotment.getEmp_type();
    }
}
