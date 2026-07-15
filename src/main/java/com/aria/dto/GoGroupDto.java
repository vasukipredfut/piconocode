package com.aria.dto;

import com.aria.model.GoGroup;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

import jakarta.persistence.Column;

@Getter
@Setter
@NoArgsConstructor
public class GoGroupDto {

    private Long id;

    private String gCode;

    private Long goName;

    private Long under;

    private Long cluster;

    private Long dept;

    private Long branch;

    private String finale;

    private String binType;

    private Long gid;

    private Long cid;

    private String rBin;

    private String export;

    private int journalBye;

    private String capacity;

    private String packages;
    private Integer silo;
    //18-08-2025 created by dhivya ,change suggested by alagu sir
    private String display; 
    
   List<PalletDto> palletDetails;
    public GoGroupDto(GoGroup goGroup) {
        this.id = goGroup.getId();
        this.gCode = goGroup.getGCode();
        this.goName = goGroup.getGoName();
        this.under = goGroup.getUnder();
        this.cluster = goGroup.getCluster();
        this.dept = goGroup.getDept();
        this.branch = goGroup.getBranch();
        this.finale = goGroup.getFinale();
        this.binType = goGroup.getBinType();
        this.gid = goGroup.getGid();
        this.cid = goGroup.getCid();
        this.rBin = goGroup.getRBin();
        this.export = goGroup.getExport();
        this.journalBye = goGroup.getJournalBye();
        this.capacity = goGroup.getCapacity();
        this.packages = goGroup.getPackages();
        this.display=goGroup.getDisplay();
       
    }
}
