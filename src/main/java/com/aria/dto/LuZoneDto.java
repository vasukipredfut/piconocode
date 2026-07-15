package com.aria.dto;

import com.aria.model.LuZone;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class LuZoneDto {

    private Long id;

   
    private String name;

   
    private Long sjid;

   
    private Long wcid;

   
    private String wctable;

   
    private Long dept;

   
    private Long  branch;

   
    private Long  male;

   
    private Long female;

   
    private String t;

   
    private String status;

   
    private Long costGroupId;

   
    private Long  dacatId;

   
    private String costReport;

   
    private String  wagestype;

   
    private Date modifiedTimeStamp;

    public LuZoneDto(LuZone luZone) {
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
