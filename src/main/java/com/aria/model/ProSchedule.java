package com.aria.model;

import com.aria.dto.ProScheduleDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "product_schedule") //proschedule
public class ProSchedule {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column(name = "task_Category_Id")
    private Long taskCategoryId;

    @Column(name = "task_Id")
    private Long taskId;

    @Column(name = "machine_Id")
    private Long machineId;

    @Column
    private String option;

    @Column
    private int frequency;

    @Column
    private int remind;

    @Column
    private String ext;

    @Column(name = "mail_Id")
    private Long mailId;

    @Column
    private Long responsibility;

    @Column
    private Long responsibility1;

    @Column
    private Long validator;

    @Column
    private Long approver;

    @Column(name = "count_Date")
    @Temporal(TemporalType.DATE)
    private Date countDate;

    @Column
    private int remain;

    @Column
    private String describe;

    @Column
    private String author;

    @Column(name = "created_On")
    private Date createdOn;

    @Column
    private String fpublic;

    @Column
    private Long esc1;

    @Column
    private Long esc2;

    @Column
    private Long esc3;

    @Column
    private Long esc4;

    @Column
    private int ontime;

    @Column
    private int delaymf;

    @Column
    private Long unlockres;

    @Column
    @Temporal(TemporalType.TIME)
    private Date begintime;

    @Column
    private Long clusres;

    @Column
    private Long clusval;

    @Column
    private Long responsibility2;

    @Column
    private String audit;

    @Column
    private String finals;

    public ProSchedule(ProScheduleDto proSchedule) {
        this.id = proSchedule.getId();
        this.name = proSchedule.getName();
        this.taskCategoryId = proSchedule.getTaskCategoryId();
        this.taskId = proSchedule.getTaskId();
        this.machineId = proSchedule.getMachineId();
        this.option = proSchedule.getOption();
        this.frequency = proSchedule.getFrequency();
        this.remind = proSchedule.getRemind();
        this.ext = proSchedule.getExt();
        this.mailId = proSchedule.getMailId();
        this.responsibility = proSchedule.getResponsibility();
        this.responsibility1 = proSchedule.getResponsibility1();
        this.validator = proSchedule.getValidator();
        this.approver = proSchedule.getApprover();
        this.countDate = proSchedule.getCountDate();
        this.remain = proSchedule.getRemain();
        this.describe = proSchedule.getDescribe();
        this.author = proSchedule.getAuthor();
        this.createdOn = proSchedule.getCreatedOn();
        this.fpublic = proSchedule.getFpublic();
        this.esc1 = proSchedule.getEsc1();
        this.esc2 = proSchedule.getEsc2();
        this.esc3 = proSchedule.getEsc3();
        this.esc4 = proSchedule.getEsc4();
        this.ontime = proSchedule.getOntime();
        this.delaymf = proSchedule.getDelaymf();
        this.unlockres = proSchedule.getUnlockres();
        this.begintime = proSchedule.getBegintime();
        this.clusres = proSchedule.getClusres();
        this.clusval = proSchedule.getClusval();
        this.responsibility2 = proSchedule.getResponsibility2();
        this.audit = proSchedule.getAudit();
        this.finals = proSchedule.getFinals();
    }

}
