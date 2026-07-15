package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "leave_summary")
public class LeaveSummary {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String year;
    @Column
    private String month;
    @Column
    private Long empcode;
    @Column
    private String name;
    @Column
    private String present;
    @Column
    private String absent;
    @Column
    private String nonil;
    @Column
    private String late;
    @Column
    private String nolate;
    @Column
    private String perm;
    @Column
    private String noperm;
    @Column
    private String leave;
    @Column
    private String noleave;
    @Column
    private Date updatedon;
    @Column
    private double nodays;
    @Column
    private double presents;
    @Column
    private double onduty;
    @Column
    private double leaves;
    @Column
    private double absents;
    @Column
    private double noshifts;
    @Column
    private double misupdate;
    @Column
    private Long missign;
    @Column
    private Long odapprove;
    @Column
    private double tsaldays;
    @Column
    private Date lupdate;
    @Column
    private String Machineip;
    @Column
    private String Location;
    @Column
    private double dleave;
    @Column
    private Long C1;
    @Column
    private Long C2;
    @Column
    private Long C3;
    @Column
    private Long C4;
    @Column
    private Long C5;
    @Column
    private Long C6;
    @Column
    private double C7;
    @Column
    private Long C8;
    @Column
    private Long C9;
    @Column
    private Long C10;
    @Column
    private Long C11;
    @Column
    private Long C12;
    @Column
    private Long C13;
    @Column
    private Long C14;
    @Column
    private Long C15;
    @Column
    private Long C16;
    @Column
    private Long C17;
    @Column
    private Long C18;
    @Column
    private Long C19;
    @Column
    private Long C20;
    @Column
    private Long C21;
    @Column
    private Long C22;
    @Column
    private Long C23;
    @Column
    private Long C24;
    @Column
    private Long C25;
    @Column
    private Long C26;
    @Column
    private Long C27;
    @Column
    private Long C28;
    @Column
    private Long C29;
    @Column
    private Long C30;
    @Column
    private Long did;
    @Column
    private Long bid;
    @Column
    @Temporal(TemporalType.DATE)
    private Date jdate;
    @Column
    private double expyears;
    @Column
    private String status;
    @Column
    private String C23trnno;
    @Column
    private String designation;
    @Column
    private Long strmy;
    @Column
    private String bcontrol;
    @Column
    private double bleave;
    @Column
    private double bleaves;
    @Column
    private String cstatus;
    @Column
    private Long S1;
    @Column
    private Long S2;
    @Column
    private Long S3;
    @Column
    private Long S4;
    @Column
    private Long S5;
    @Column
    private Long S6;
    @Column
    private Long S7;
    @Column
    private Long S8;
    @Column
    private Long S9;
    @Column
    private Long S10;
    @Column
    private Long S11;
    @Column
    private Long S12;
    @Column
    private Long S13;
    @Column
    private Long S14;
    @Column
    private Long S15;
    @Column
    private Long S16;
    @Column
    private Long S17;
    @Column
    private Long S18;
    @Column
    private Long S19;
    @Column
    private Long S20;
    @Column
    private Long S21;
    @Column
    private Long S22;
    @Column
    private Long S23;
    @Column
    private Long S24;
    @Column
    private Long S25;
    @Column
    private Long S26;
    @Column
    private Long S27;
    @Column
    private Long S28;
    @Column
    private Long S29;
    @Column
    private Long S30;
    @Column
    private Long tpresents;
    @Column
    private String ayear;
    @Column
    private double pleaves;
    @Column
    private String chstatus;
    @Column
    private Long cby;
    @Column
    private Date changedon;
    @Column
    private String upstatus;
    @Column
    private Long C31;
    @Column
    private Long C32;
    @Column
    private Long C33;
    @Column
    private Long C34;
    @Column
    private Long C35;
    @Column
    private double earned_Leave;
    @Column
    private double earned_Cumulative_Leave;
    @Column
    private double leave_Taken;
    @Column
    private double cumulative_Leave_taken;
    @Column
    private double deducted_Leave;
    @Column
    private double cumulative_Deducted_Leave;
    @Column
    private double dutyAllocatedDays;
    @Column
    private double dutyAllocatedSalary;
    @Column
    private double overTimeWages;
    @Column
    private double workedDays;
    @Column
    private double workedDaysBasic;
    @Column
    private double workedDaysDA;
    @Column
    private double WA;
    @Column
    private double CON;
    @Column
    private double GWA;
    @Column
    private double HRA;
    @Column
    private double earnedLeaveDays;
    @Column
    private double earnedLeaveBasic;
    @Column
    private double earnedLeaveDA;
    @Column
    private double leaveHolidaySalaryDays;
    @Column
    private double leaveHolidayBasic;
    @Column
    private double leaveHolidayDA;
    @Column
    private Long leaveHolidayAttendedDays;
    @Column
    private double leaveHolidayAttendedBasic;
    @Column
    private double leaveHolidayAttendedDA;
    @Column
    private double lossOfPayDays;
    @Column
    private double LWOP;
}
