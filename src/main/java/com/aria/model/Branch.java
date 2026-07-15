package com.aria.model;

import com.aria.dto.BranchDto;
import com.aria.dto.BranchReqDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "branch")
public class Branch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "org_id", referencedColumnName = "id")
    private Organisation organisation;

    @Column(name = "name")
    private String branchName;

    @Column
    private String search;

    @Column
    private String address;

    @Column
    private String city;

    @Column
    private String state;

    @Column
    private String phone;

    @Column
    private String fax;

    @Column
    private String email;

    @Column
    private String vattin;

    @Column
    private String dbname;

    @Column
    private String hostname;

    @Column
    private String tinno;

    @Column
    private String regn_2;

    @Column
    private String purpose;

    @Column
    private String workinst;

    @Column
    private String safetyinst;

    @Column
    private String trnprefix;

    @Column
    private String pv;

    @Column
    private String rmwh;

    @Column
    private String rmcolid;

    @Column
    private String pverify;

    @Column
    private String papprove;

    @Column
    private String pdgm;

    @Column
    private String podesig;

    @Column
    private String poname;

    @Column
    private String pcount;

    @Column
    private String acount;

    @Column
    private String dgmid;

    @Column
    private String bcontrol;

    @Column
    private String pmdesig;

    @Column
    private String stkcolid;

    @Column
    private String hubid;

    @Column
    private String costreport;

    @Column
    private String pincode;

    @Column
    private String registrationno;

    @Column
    private String manager;

    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(updatable = false)
    private LocalDateTime created_date;

    @Column
    private LocalDateTime updated_date;

    @Column
    private String production;

    public Branch(BranchDto branchDto) {
        this.id = branchDto.getId();
        this.branchName = branchDto.getBranchName();
    }

    public Branch(BranchReqDto branch) {
        this.id = branch.getId();
        if(branch.getOrgId()!= null) {
            Organisation organisation1 = new Organisation();
            organisation1.setId(branch.getOrgId());
            this.organisation = organisation1;
        }
        this.branchName = branch.getBranchName();
        this.search = branch.getSearch();
        this.address = branch.getAddress();
        this.city = branch.getCity();
        this.state = branch.getState();
        this.phone = branch.getPhone();
        this.fax = branch.getFax();
        this.email = branch.getEmail();
        this.vattin = branch.getVatTin();
        this.dbname = branch.getDbname();
        this.hostname = branch.getHostname();
        this.tinno = branch.getTinNo();
        this.registrationno = branch.getRegistrationNo();
        this.regn_2 = branch.getRegn_2();
        this.purpose = branch.getPurpose();
        this.workinst = branch.getWorkinst();
        this.safetyinst = branch.getSafetyinst();
        this.trnprefix = branch.getTrnprefix();
        this.pv = branch.getPv();
        this.rmwh = branch.getRmwh();
        this.rmcolid = branch.getRmcolid();
        this.pverify = branch.getPverify();
        this.papprove = branch.getPapprove();
        this.pdgm = branch.getPdgm();
        this.podesig = branch.getPodesig();
        this.poname = branch.getPoname();
        this.pcount = branch.getPcount();
        this.acount = branch.getAcount();
        this.dgmid = branch.getDgmid();
        this.bcontrol = branch.getBcontrol();
        this.pmdesig = branch.getPmdesig();
        this.stkcolid = branch.getStkcolid();
        this.hubid = branch.getHubid();
        this.costreport = branch.getCostreport();
        this.pincode = branch.getPincode();
        this.manager = branch.getManager();
        this.production = branch.getProduction();
    }
}
