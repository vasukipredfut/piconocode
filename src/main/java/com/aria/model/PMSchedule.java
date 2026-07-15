package com.aria.model;


import java.time.LocalDateTime;
import java.util.Date;

import jakarta.persistence.*;

import com.aria.dto.PMScheduleDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "pmschedule")
public class PMSchedule {

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

    @OneToOne
    @JoinColumn(name = "taskcategory", referencedColumnName = "id")
    private TaskCategory taskCategory;

    @OneToOne
    @JoinColumn(name = "task", referencedColumnName = "id")
    private TaskGroupMaster taskGroup;

    @OneToOne
    @JoinColumn(name = "machineid", referencedColumnName = "id")
    private Machine machine;

    @OneToOne
    @JoinColumn(name = "panelid", referencedColumnName = "id")
    private Panel panel;

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
    @Temporal(TemporalType.DATE)
    private Date schedule_date;


    public PMSchedule(PMScheduleDto field) {

        if (field.getMachineId() != null) {
            Machine machine = new Machine();
            machine.setId(field.getMachineId());
            this.machine = machine;
        }

        if (field.getPanelId() != null) {
            Panel panel = new Panel();
            panel.setId(field.getPanelId());
            this.panel = panel;
        }

        if (field.getTaskCategoryId() != null) {
            TaskCategory tc = new TaskCategory();
            tc.setId(field.getTaskCategoryId());
            this.taskCategory = tc;
        }

        this.id = field.getId();
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


    }
}
