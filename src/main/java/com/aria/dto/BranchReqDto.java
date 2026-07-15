package com.aria.dto;

import com.aria.model.Branch;
import com.aria.model.Organisation;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;
import java.time.LocalDateTime;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
public class BranchReqDto {


    private Long id;


    private Long orgId;
    private String orgName;


    private String branchName;


    private String search;


    private String address;


    private String city;


    private String state;


    private String phone;


    private String fax;


    private String email;


    private String vatTin;


    private String dbname;


    private String hostname;


    private String tinNo;


    private String registrationNo;

    private String regn_2;


    private String purpose;


    private String workinst;


    private String safetyinst;


    private String trnprefix;


    private String pv;


    private String rmwh;


    private String rmcolid;


    private String pverify;


    private String papprove;


    private String pdgm;


    private String podesig;


    private String poname;


    private String pcount;


    private String acount;


    private String dgmid;


    private String bcontrol;


    private String pmdesig;


    private String stkcolid;


    private String hubid;


    private String costreport;


    private String pincode;

    private String manager;

    private Long createdBy;
    private Long updatedBy;
    private String created_date;
    private String updated_date;

    private String firmIds;
    private String addFirmIds;
    private String removeFirmIds;


    private String production;



    public BranchReqDto(Branch branch) {
        DateUtil dateUtil = new DateUtil();
        this.id = branch.getId();
        if (branch.getOrganisation() != null) {
            this.orgId = branch.getOrganisation().getId();
            this.orgName = branch.getOrganisation().getName();
        }
        this.branchName = branch.getBranchName();
        this.search = branch.getSearch();
        this.address = branch.getAddress();
        this.city = branch.getCity();
        this.state = branch.getState();
        this.phone = branch.getPhone();
        this.fax = branch.getFax();
        this.email = branch.getEmail();
        this.vatTin = branch.getVattin();
        this.dbname = branch.getDbname();
        this.hostname = branch.getHostname();
        this.tinNo = branch.getTinno();
        this.registrationNo = branch.getRegistrationno();
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
        this.createdBy = branch.getCreatedBy();
        this.updatedBy = branch.getUpdatedBy();
        if (branch.getCreated_date() != null)
            this.created_date = dateUtil.getLocalDateTimeToDateTimeString(branch.getCreated_date());

        if (branch.getUpdated_date() != null)
            this.updated_date = dateUtil.getLocalDateTimeToDateTimeString(branch.getCreated_date());

    }
}
