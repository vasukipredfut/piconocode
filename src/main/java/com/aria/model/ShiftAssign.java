package com.aria.model;


import java.time.LocalDateTime;
import java.util.Date;

import jakarta.persistence.*;

import com.aria.dto.ShiftAssignDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "shift_assign")
public class ShiftAssign {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "org_id", referencedColumnName = "id")
    private Organisation organisation;

    private Long department_id;

    
    private Long branch_id;

    @OneToOne
    @JoinColumn(name = "shift_id", referencedColumnName = "id")
    private Shifts shifts;

   
    private Long emp_id;
    
   
    private Long designation_id;

 
    private Long week_day_id;
    
   
    private Long rotation_id;

    @Column
    @Temporal(TemporalType.DATE)
    private Date from_date;
    
    @Column
    @Temporal(TemporalType.DATE)
    private Date to_date;

    @Column
    @Temporal(TemporalType.DATE)
    private Date shift_date;

    @Column
    private String shift_value;

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


    public ShiftAssign(ShiftAssignDto shiftAssign) {
        this.id = shiftAssign.getId();
        if (shiftAssign.getOrgId()!= null) {
        	Organisation org = new Organisation();
        	org.setId(shiftAssign.getOrgId());
        	this.organisation = org;
        }
        
       this.department_id = shiftAssign.getDepartment_id();
       this.branch_id = shiftAssign.getBranch_id();
        if (shiftAssign.getShiftId()!= null) {
            Shifts shifts_details = new Shifts();
            shifts.setId(shiftAssign.getShiftId());
            this.shifts = shifts_details;
        }
        
        this.emp_id = shiftAssign.getEmpId();
    
        this.rotation_id = shiftAssign.getShift_rotation_id();
        this.createdBy = shiftAssign.getCreatedBy();
        this.updatedBy = shiftAssign.getUpdatedBy();
        this.created_date = shiftAssign.getCreated_date();
        this.updated_date = shiftAssign.getUpdated_date();
    }
}
