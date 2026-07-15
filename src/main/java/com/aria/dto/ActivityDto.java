package com.aria.dto;


import com.aria.model.Activity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
@Getter
@Setter
@NoArgsConstructor
public class ActivityDto {


  
    private Long id;

    private Long parent_id;

  
    private Long root_id;

  
    private String name;

  
    private String activityDesc;

  
    private Long fid;

  
    private String fname;
  
    private String dfname;

  
    private String aremind;


    private Date beginDate;


    private Date endDate;


    private Date remainingDate;

    private Date amandDate;

  
    private Long assignDept;

  
    private Long assignBranch;

  
    private Long assignTo;
  
    private Long assignBy;

  
    private Long dept;
  
    private Long branch;

  
    private Long acat;
  
    private String status;

  
    private String updatedOn;
  
    private String verifiedOn;

  
    private Double bcost;
  
    private Double cost;

  
    private Double dwcost;

  
    private Double staffcost;
  
    private int disporder;

  
    private String dispfont;
  
    private Double dwtotal;
  
    private Double stotal;

  
    private Long rfid;
  
    private String rfname;

  
    private String rdfname;

  
    private String gnote;

  
    private int fmulevel;

  
    private int toulevel;
  
    private String dnotify;

    private Date notifyupto;

  
    private int duedays;

  
    private String verify;

  
    private int flowFlag;

  
    private int viewFlag;

  
    private String instruction ;

    public ActivityDto(Activity activityDto) {
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
        this.updatedOn = activityDto.getUpdated_on();
        this.verifiedOn = activityDto.getVerified_on();
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
