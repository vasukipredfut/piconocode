package com.aria.model;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

import com.aria.dto.DutyTransferDto;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "duty_transfer")
public class DutyTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shift_date")
    private Date shiftDate;

    @Column
    private String category;

    @Column
    private String shift;

    @Column
    private Long shift_id;

    @Column
    private String sex;

    @OneToOne
    @JoinColumn(name = "org_id", referencedColumnName = "id")
    private Organisation organisation;

    @OneToOne
    @JoinColumn(name = "dept_id", referencedColumnName = "id")
    private DepartmentDetails departmentDetails;


    @Column(name = "branch_id", updatable = false)
    private Long branchId;

    @Column
    private int active;

    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(updatable = false)
    private LocalDateTime created_date;

    @Column
    private LocalDateTime updated_date;

    @OneToMany(mappedBy = "transferId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<DutyTransferAssignDuty> assignDutyDetails;

    public DutyTransfer(DutyTransferDto dutyAllotment) {
        this.id = dutyAllotment.getId();
        this.branchId = dutyAllotment.getBranchId();
        this.category = dutyAllotment.getCategory();
        this.shift = dutyAllotment.getShift();
        this.shift_id = dutyAllotment.getShiftId();
        this.sex = dutyAllotment.getSex();
        this.active = dutyAllotment.getActive();
        this.createdBy = dutyAllotment.getCreatedBy();
        this.updatedBy = dutyAllotment.getUpdatedBy();
        this.created_date = dutyAllotment.getCreated_date();
        this.updated_date = dutyAllotment.getUpdated_date();
    }
}
