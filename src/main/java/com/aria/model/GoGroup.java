package com.aria.model;


import com.aria.dto.GoGroupDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "gogroup")
public class GoGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "binprimaryid")
    private Long id;

    @Column(name = "gcode")
    private String gCode;

    @Column(name = "goname")
    private Long goName;

    @Column
    private Long under;

    @Column
    private Long cluster;

    @Column
    private Long dept;

    @Column
    private Long branch;

    @Column(name = "final")
    private String finale;

    @Column(name = "bintype")
    private String binType;

    @Column(name = "gid")
    private Long gid;

    @Column(name = "cid")
    private Long cid;

    @Column(name = "rbin")
    private String rBin;

    @Column
    private String export;

    @Column(name = "journal_bye")
    private int journalBye;

    @Column(name = "capacity")
    private String capacity;

    @Column(name = "packages")
    private String packages;

    @Column(name = "silo")
    private Integer silo;
    
  //18-08-2025 created by dhivya ,change suggested by alagu sir
    
    @Column(name="display") 
    private String display;
    
  
    

    public GoGroup(GoGroupDto goGroup) {
        this.id = goGroup.getId();
        this.gCode = goGroup.getGCode();
        this.goName = goGroup.getGoName();
        this.under = goGroup.getGid();
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
        this.silo = goGroup.getSilo();
        this.display=goGroup.getDisplay();
       
    }
    
}

