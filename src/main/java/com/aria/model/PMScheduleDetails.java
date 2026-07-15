package com.aria.model;

import com.aria.dto.PMScheduleDto;
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
@Table(name = "pmscheduledetails")
public class PMScheduleDetails {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private Long scheduleid;


    @Column
    private String begintime;

    @Column
    private Long clusres;

    @Column
    private Long clusval;

    @Column
    private Long remind;

    @Column
    private Long ontime;

    @Column
    private Long delaymf;

    @Column
    private Date countdt;

    @Column(name = "last_completed_date")
    private Date lastCompletedDate;

    @Column(name = "ext")
    private String externalVendor;

    @Column(name = "remaind_me")
    private String remaindMe;

    @Column
    private String ppublic;

    @Column
    private String author;

    @Column
    private Long mailid;

    @Column
    private Long unlockres;

    @Column
    private Long responsibility;

    @Column
    private Long responsibility1;

    @Column
    private Long responsibility2;

    @Column
    private Long validator;
    @Column
    private Long approver;
    @Column
    private Long remain;
    @Column
    private String describe;
    @Column
    private String option;
    @Column
    private Long frequency;

    @Column (name = "esc1")
    private Long tier1;

    @Column (name = "esc2")
    private Long tier2;

    @Column (name = "esc3")
    private Long tier3;

    @Column (name = "esc4")
    private Long tier4;

    @Column(name = "taskcategory")
    private Long taskCategoryId;


    @Column(name = "task")
    private Long taskGroupId;


    @Column(name = "machineid")
    private Long machineId;


    @Column(name = "panelid")
    private Long panelId;

    @Column
    private String audit;

    @Column
    private String active;

    @Column
    private String pfinal;

    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(updatable = false)
    private LocalDateTime created_date;

    @Column
    private LocalDateTime updated_date;

    @Column
    private Long pm_schedule_id;

    @Column
    private int active_flag;

    @Column
    @Temporal(TemporalType.DATE)
    private Date schedule_date;


    @Column
    private String status;


    public PMScheduleDetails(PMSchedule field) {

        if (field.getMachine() != null) {
            this.machineId = field.getMachine().getId();
        }
        if (field.getPanel() != null) {
            this.panelId = field.getPanel().getId();
        }
        if (field.getTaskCategory() != null) {
            this.taskCategoryId = field.getTaskCategory().getId();
        }
        if (field.getTaskGroup() != null) {
            this.taskGroupId = field.getTaskGroup().getId();
        }
        this.pm_schedule_id = field.getId();
        this.scheduleid = field.getScheduleid();

        this.name = field.getName();
        this.option = field.getOption();
        this.frequency = field.getFrequency();
        this.remain = field.getRemain();
        this.remind = field.getRemind();
        this.remaindMe = field.getRemaindMe();
        this.externalVendor = field.getExternalVendor();
        this.responsibility = field.getResponsibility();
        this.mailid = field.getMailid();
        this.responsibility1 = field.getResponsibility1();
        this.responsibility2 = field.getResponsibility2();
        this.validator = field.getValidator();
        this.approver = field.getApprover();
        this.describe = field.getDescribe();
        this.author = field.getAuthor();
        this.ppublic = field.getPpublic();
        this.ontime = field.getOntime();
        this.delaymf = field.getDelaymf();
        this.unlockres = field.getUnlockres();
        this.begintime = field.getBegintime();
        this.clusres = field.getClusres();
        this.clusval = field.getClusval();
        this.audit = field.getAudit();
        this.pfinal = field.getPfinal();
        this.active = field.getActive();
        this.createdBy = field.getCreatedBy();
        this.updatedBy = field.getUpdatedBy();
        this.created_date = field.getCreated_date();
        this.updated_date = field.getUpdated_date();
        this.lastCompletedDate = field.getLastCompletedDate();
        this.active_flag = 1;
        this.schedule_date = field.getSchedule_date();
    }
}
