package com.aria.model;

import com.aria.dto.DutyAllotmentDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "duty_allotment")
public class DutyAllotment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transfer_id", updatable = false)
    private Long transferId;

    @Column(name = "shift_date")
    @Temporal(TemporalType.DATE)
    private Date shiftDate;

    @Column
    private String category;

    @Column
    private String shift; //shiftType

    @Column
    private Long shift_id;

    @OneToOne
    @JoinColumn(name = "org_id", referencedColumnName = "id")
    private Organisation organisation;

    @OneToOne
    @JoinColumn(name = "dept_id", referencedColumnName = "id")
    private DepartmentDetails departmentDetails;



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

    @OneToMany(mappedBy = "allotmentId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<AssignDuty> assignDuties;

    public DutyAllotment(DutyAllotmentDto dutyAllotment) {
        this.id = dutyAllotment.getId();
        this.category = dutyAllotment.getCategory();
        this.shift = dutyAllotment.getShift();
        this.shift_id = dutyAllotment.getShiftId();
        this.active = dutyAllotment.getActive();
        this.createdBy = dutyAllotment.getCreatedBy();
        this.updatedBy = dutyAllotment.getUpdatedBy();
        this.created_date = dutyAllotment.getCreated_date();
        this.updated_date = dutyAllotment.getUpdated_date();

    }

    public DutyAllotment(DutyTransfer dutyAllotment) {
        //this.id = dutyAllotment.getId();
        this.category = dutyAllotment.getCategory();
        this.shift = dutyAllotment.getShift();
        this.shift_id = dutyAllotment.getShift_id();
        this.shiftDate = dutyAllotment.getShiftDate();
        this.active = dutyAllotment.getActive();
        this.createdBy = dutyAllotment.getCreatedBy();
        this.updatedBy = dutyAllotment.getUpdatedBy();
        this.created_date = dutyAllotment.getCreated_date();
        this.updated_date = dutyAllotment.getUpdated_date();
    }


}
