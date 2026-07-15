package com.aria.model;

import com.aria.dto.ActivityDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "arms")
public class Activity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long parent_id;

    @Column
    private Long root_id;  
    
    @Column
    private String name;

    @Column(name = "activity_Desc")
    private String activityDesc;
    
    @Column
    private Long fid;

    @Column
    private String fname;
    @Column
    private String dfname;

    @Column
    private String aremind;

    @Column(name = "begin_Date")
    @Temporal(TemporalType.DATE)
    private Date beginDate;

    @Column(name = "end_Date")
    @Temporal(TemporalType.DATE)
    private Date endDate;

    @Column(name = "remaining_Date")
    @Temporal(TemporalType.DATE)
    private Date remainingDate;

    @Column(name = "amand_Date")
    @Temporal(TemporalType.DATE)
    private Date amandDate;

    @Column(name = "assign_Dept")
    private Long assignDept;

    @Column(name = "assign_Branch")
    private Long assignBranch;

    @Column(name = "assign_To")
    private Long assignTo;
    @Column(name = "assign_By")
    private Long assignBy;

    @Column
    private Long dept;
    @Column
    private Long branch;

    @Column
    private Long acat;
    @Column
    private String status;
    
    @Column
    private String updated_on;
    @Column
    private String verified_on;
    
    @Column
    private Double bcost;
    @Column
    private Double cost;

    @Column
    private Double dwcost;

    @Column
    private Double staffcost;
    @Column
    private int disporder;

    @Column
    private String dispfont;
    @Column
    private Double dwtotal;
    @Column
    private Double stotal;

    @Column
    private Long rfid;
    @Column
    private String rfname;

    @Column
    private String rdfname;

    @Column
    private String gnote;
    @Column
    private int fmulevel;

    @Column
    private int toulevel;
    @Column
    private String dnotify;

    @Column
    private Date notifyupto;

    @Column
    private int duedays;

    @Column
    private String verify;

    @Column(name = "flow_Flag")
    private int flowFlag;

    @Column(name = "view_Flag")
    private int viewFlag;

    @Column
    private String instruction ;


    public Activity(ActivityDto activityDto) {
        this.id = activityDto.getId();
        this.parent_id = activityDto.getId();
        this.root_id = activityDto.getId();
        this.name = activityDto.getName();
        this.activityDesc = activityDto.getActivityDesc();
        this.fid = activityDto.getFid();
        this.fname = activityDto.getFname();
        this.dfname = activityDto.getDfname();
        this.aremind = activityDto.getAremind();
        this.beginDate = activityDto.getBeginDate();
        this.endDate = activityDto.getEndDate();
        this.remainingDate = activityDto.getRemainingDate();
        this.amandDate = activityDto.getAmandDate();
        this.assignDept = activityDto.getAssignDept();
        this.assignBranch = activityDto.getAssignBranch();
        this.assignTo = activityDto.getAssignTo();
        this.assignBy = activityDto.getAssignBy();
        this.dept = activityDto.getDept();
        this.branch = activityDto.getBranch();
        this.acat = activityDto.getAcat();
        this.status = activityDto.getStatus();
        this.updated_on = activityDto.getUpdatedOn();
        this.verified_on = activityDto.getVerifiedOn();
        this.bcost = activityDto.getBcost();
        this.cost = activityDto.getCost();
        this.dwcost = activityDto.getDwcost();
        this.staffcost = activityDto.getStaffcost();
        this.disporder = activityDto.getDisporder();
        this.dispfont = activityDto.getDispfont();
        this.dwtotal = activityDto.getDwtotal();
        this.stotal = activityDto.getStotal();
        this.rfid = activityDto.getRfid();
        this.rfname = activityDto.getRfname();
        this.rdfname = activityDto.getRdfname();
        this.gnote = activityDto.getGnote();
        this.fmulevel = activityDto.getFmulevel();
        this.toulevel = activityDto.getToulevel();
        this.dnotify = activityDto.getDnotify();
        this.notifyupto = activityDto.getNotifyupto();
        this.duedays = activityDto.getDuedays();
        this.verify = activityDto.getVerify();
        this.flowFlag = activityDto.getFlowFlag();
        this.viewFlag = activityDto.getViewFlag();
        this.instruction = activityDto.getInstruction();
    }
}
