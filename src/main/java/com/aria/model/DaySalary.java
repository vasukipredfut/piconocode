package com.aria.model;

import com.aria.dto.*;
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
@Table(name = "day_salary")
public class DaySalary {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dept")
    private Long fromDept;
    @Column(name = "branch")
    private Long fromBranch;
    @Column(name = "emp_id")
    private Long empId;

    @Column(name = "employee_name")
    private String employeeName;

    @Column(name = "joining_date")
    private String joiningDate;

    @Column(name = "experience")
    private String experience;

    @Column(name = "sex")
    private String sex;

    @Column(name = "contract")
    private String contract;

    @Column(name = "dacatid")
    private Long daCatId;

    @Column(name = "luzone")
    private Long luzone;

    @Column(name = "designation_id")
    private Long designationId;
    @Column(name = "designation_name")
    private String designationName;

    @Column(name = "shift_date")
    @Temporal(TemporalType.DATE)
    private Date shift_date;

    @Column(name = "shift_id")
    private Long shiftId;


    @Column(name = "shift_type")
    private String shiftType;

    @Column(name = "basic")
    private double basic;

    @Column(name = "da")
    private double da;

    @Column(name = "allowance")
    private double allowance;

    @Column(name = "per_day_total")
    private double per_day_total;

    @Column(name = "pshift")
    private Long pShiftId;

    @Column( name = "created_by", updatable = false)
    private Long createdBy;


    @Column(updatable = false)
    private LocalDateTime created_date;

    @Column(name = "worked_dept")
    private Long workedDept;
    @Column(name = "worked_branch")
    private Long workedBranch;
    @Column(name = "check_in")
    private LocalDateTime checkIn;

    @Column(name = "check_out")
    private LocalDateTime checkOut;

    @Column(name = "total_working_hrs")
    private String totalworkingdays;

    public DaySalary(DWagesSalarySummaryDTO salarySummaryDTO) {
        DateUtil dateUtil = new DateUtil();
        this.fromDept = salarySummaryDTO.getFromDept();
        this.fromBranch = salarySummaryDTO.getFromBranch();
        this.workedDept = salarySummaryDTO.getWorkedDept();
        this.workedBranch = salarySummaryDTO.getWorkedBranch();
        this.empId = salarySummaryDTO.getEmpId();
        this.employeeName = salarySummaryDTO.getEmployeeName();
        this.joiningDate = salarySummaryDTO.getJoiningDate();
        this.experience = salarySummaryDTO.getExperience();
        this.sex = salarySummaryDTO.getSex();
        this.contract = salarySummaryDTO.getContract();
        this.daCatId = salarySummaryDTO.getDaCatId();
        this.luzone = salarySummaryDTO.getLuzone();
        this.designationId = salarySummaryDTO.getDesignationId();
        this.designationName = salarySummaryDTO.getDesignationName();
        if(salarySummaryDTO.getShiftDate() != null) {
            this.shift_date = dateUtil.getStringTODateWk(salarySummaryDTO.getShiftDate());
        }
        this.shiftId = salarySummaryDTO.getShiftId();
        this.shiftType = salarySummaryDTO.getShiftType();

        if( salarySummaryDTO.getBasic() != null) {
            this.basic = Double.valueOf(salarySummaryDTO.getBasic());
        }
        if( salarySummaryDTO.getDa() != null) {
            this.da = Double.valueOf(salarySummaryDTO.getDa());
        }
        if( salarySummaryDTO.getAllowance() != null) {
            this.allowance = Double.valueOf(salarySummaryDTO.getAllowance());
        }
        if( salarySummaryDTO.getTotal() != null) {
            this.per_day_total = Double.valueOf(salarySummaryDTO.getTotal());
        }

        this.pShiftId = salarySummaryDTO.getPShiftId();
        
        this.checkIn = salarySummaryDTO.getCheckIn();
        this.checkOut = salarySummaryDTO.getCheckOut();
        this.totalworkingdays = salarySummaryDTO.getTotalworkingdays();

    }


    public DaySalary(DaySalary daySalary , DWagesSalarySummaryDTO salarySummaryDTO) {
        DateUtil dateUtil = new DateUtil();
        this.id = daySalary.getId();
        this.fromBranch = daySalary.getFromBranch();
        this.fromDept = daySalary.getFromDept();
        this.workedBranch = daySalary.getWorkedBranch();
        this.workedDept = daySalary.getWorkedDept();
        this.empId = daySalary.getEmpId();
        this.employeeName = salarySummaryDTO.getEmployeeName();
        this.joiningDate = salarySummaryDTO.getJoiningDate();
        this.experience = salarySummaryDTO.getExperience();
        this.sex = salarySummaryDTO.getSex();
        this.contract = salarySummaryDTO.getContract();
        this.daCatId = salarySummaryDTO.getDaCatId();
        this.luzone = salarySummaryDTO.getLuzone();
        this.designationId = salarySummaryDTO.getDesignationId();
        this.designationName = salarySummaryDTO.getDesignationName();
        if(salarySummaryDTO.getShiftDate() != null) {
            this.shift_date = dateUtil.getStringTODateWk(salarySummaryDTO.getShiftDate());
        }
        this.shiftId = salarySummaryDTO.getShiftId();
        this.shiftType = salarySummaryDTO.getShiftType();
        if( salarySummaryDTO.getBasic() != null) {
            this.basic = Double.valueOf(salarySummaryDTO.getBasic());
        }
        if( salarySummaryDTO.getDa() != null) {
            this.da = Double.valueOf(salarySummaryDTO.getDa());
        }
        if( salarySummaryDTO.getAllowance() != null) {
            this.allowance = Double.valueOf(salarySummaryDTO.getAllowance());
        }
        if( salarySummaryDTO.getTotal() != null) {
            this.per_day_total = Double.valueOf(salarySummaryDTO.getTotal());
        }

        this.pShiftId = salarySummaryDTO.getPShiftId();
        this.checkIn = salarySummaryDTO.getCheckIn();
        this.checkOut = salarySummaryDTO.getCheckOut();
        this.totalworkingdays = salarySummaryDTO.getTotalworkingdays();

    }
}
