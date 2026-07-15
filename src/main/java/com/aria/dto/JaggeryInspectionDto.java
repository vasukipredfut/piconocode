package com.aria.dto;

import com.aria.model.JaggeryInspection;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;


@NoArgsConstructor
@Getter
@Setter
public class JaggeryInspectionDto {

    private Long id;


    private String jagDate;


    private Long inwardat;


    private String billtrnno;


    private String billNo;


    private String billDate;


    private Long vendorId;


    private String place;


    private Double qty;


    private Double unitRate;


    private Long branch;


    private Long dept;


    private Long vby;


    private String vDate;
    private String vDateTime;

    private String verify;
    private String verifyByName;


    private Long aby;

    private String aDate;
    private String aDateTime;


    private String approve;
    private String approveByName;


    private Long auby;


    private String auDate;
    private String auDateTime;

    
    private String authenty;
    private String authByName;

    
    private String uniqno;

    
    private String uniqueses;
    
    private String status;
    
    private String vendorName;
    private String vendorPlace;

    private String branchName;

    private Long rejectedBy;
    private String rejectedByName;
    private String rejectdate;


    private List<String> lots;


    private Long prepareby;
    private String preparebyName;
    private String prepareDate;
    private String prepareDateTime;


    public JaggeryInspectionDto(JaggeryInspection jaggeryInspection) {
        DateUtil dateUtil = new DateUtil();
        this.id = jaggeryInspection.getId();
        this.jagDate = dateUtil.getDateTOStringWk(jaggeryInspection.getJdate());
        this.inwardat = jaggeryInspection.getInwardat();
        this.billtrnno = jaggeryInspection.getBilltrnno();
        this.billNo = jaggeryInspection.getBillno();
        this.billDate =dateUtil.getDateTOStringWk(jaggeryInspection.getBilldt());
        this.vendorId = jaggeryInspection.getParty();
        this.place = jaggeryInspection.getPlace();
        this.qty = jaggeryInspection.getQty();
        this.unitRate = jaggeryInspection.getUrate();
        this.branch = jaggeryInspection.getBranch();
        this.dept = jaggeryInspection.getDept();
        this.vby = jaggeryInspection.getVby();
        if(jaggeryInspection.getVdate() != null) {
            this.vDate = dateUtil.getLocalDateTimeToStringDDMMYYYY1(jaggeryInspection.getVdate());
            this.vDateTime = dateUtil.getLocalDateTimeToDateTimeString(jaggeryInspection.getVdate());
        }
        this.verify = jaggeryInspection.getVerify();
        this.aby = jaggeryInspection.getAby();
        if(jaggeryInspection.getAdate() != null) {
            this.aDate = dateUtil.getLocalDateTimeToStringDDMMYYYY1(jaggeryInspection.getAdate());
            this.aDateTime = dateUtil.getLocalDateTimeToDateTimeString(jaggeryInspection.getAdate());
        }
        this.approve = jaggeryInspection.getApprove();
        this.auby = jaggeryInspection.getAuby();
        if(jaggeryInspection.getAudate() != null) {
            this.auDate = dateUtil.getLocalDateTimeToStringDDMMYYYY1(jaggeryInspection.getAudate());
            this.auDateTime = dateUtil.getLocalDateTimeToDateTimeString(jaggeryInspection.getAudate());
        }
        this.prepareby = jaggeryInspection.getPrepareby();
        if(jaggeryInspection.getPreparedate() != null) {
            this.prepareDate = dateUtil.getLocalDateTimeToStringDDMMYYYY1(jaggeryInspection.getPreparedate());
            this.prepareDateTime = dateUtil.getLocalDateTimeToDateTimeString(jaggeryInspection.getPreparedate());
        }
        this.authenty = jaggeryInspection.getAuthenty();
        this.uniqno = jaggeryInspection.getUniqno();
        this.uniqueses = jaggeryInspection.getUniqueses();
        this.status = jaggeryInspection.getStatus();
        this.rejectedBy = jaggeryInspection.getRejectedBy();
        if(jaggeryInspection.getRejectdate() != null) {
            this.rejectdate = dateUtil.getDateTOStringWk(jaggeryInspection.getRejectdate());
        }
    }
}
