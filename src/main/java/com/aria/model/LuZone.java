package com.aria.model;


import com.aria.dto.LuZoneDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "luzone")
public class LuZone {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private Long sjid;

    @Column
    private Long wcid;

    @Column
    private String wctable;  
    
    @Column
    private Long dept;
    
    @Column
    private Long  branch;
    
    @Column
    private Long  male;
    
    @Column
    private Long female;
    
    @Column
    private String t;  
    
    @Column
    private String status;  
    
    @Column(name = "cost_group_id")
    private Long costGroupId;

    @Column(name = "dacat_id")
    private Long  dacatId;

    @Column(name = "cost_report")
    private String costReport; 
    
    @Column
    private String  wagestype;

    @Column(name = "modified_time_stamp")
    private Date modifiedTimeStamp;

    public LuZone(LuZoneDto luZone) {
        this.id = luZone.getId();
        this.name = luZone.getName();
        this.sjid = luZone.getSjid();
        this.wcid = luZone.getWcid();
        this.wctable = luZone.getWctable();
        this.dept = luZone.getDept();
        this.branch = luZone.getBranch();
        this.male = luZone.getMale();
        this.female = luZone.getFemale();
        this.t = luZone.getT();
        this.status = luZone.getStatus();
        this.costGroupId = luZone.getCostGroupId();
        this.dacatId = luZone.getDacatId();
        this.costReport = luZone.getCostReport();
        this.wagestype = luZone.getWagestype();
        this.modifiedTimeStamp = luZone.getModifiedTimeStamp();
    }
}
