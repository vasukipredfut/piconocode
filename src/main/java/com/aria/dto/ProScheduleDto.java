package com.aria.dto;


import com.aria.model.ProSchedule;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class ProScheduleDto {


    private Long id;

    
    private String name;

    
    private Long taskCategoryId;

    
    private Long taskId;

    
    private Long machineId;

    
    private String option;

    
    private int frequency;

    
    private int remind;

    
    private String ext;

    
    private Long mailId;

    
    private Long responsibility;

    
    private Long responsibility1;

    
    private Long validator;

    
    private Long approver;


    private Date countDate;

    
    private int remain;

    
    private String describe;

    
    private String author;

    
    private Date createdOn;

    
    private String fpublic;

    
    private Long esc1;

    
    private Long esc2;

    
    private Long esc3;

    
    private Long esc4;

    
    private int ontime;

    
    private int delaymf;

    
    private Long unlockres;

    

    private Date begintime;

    
    private Long clusres;

    
    private Long clusval;

    
    private Long responsibility2;

    
    private String audit;

    
    private String finals;


    public ProScheduleDto(ProSchedule proSchedule) {
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
