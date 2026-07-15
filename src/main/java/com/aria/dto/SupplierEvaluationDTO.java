package com.aria.dto;

import com.aria.model.SupplierEvaluation;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



@Getter
@Setter
@NoArgsConstructor
public class SupplierEvaluationDTO {



    private Long vendorId;

    private Long id;

    private Long branch;

    private Long did;

    private Long company;
  
    private String fromDate;

    private String toDate;
   
    private Long vid;
   
    private String trnno;
   
    private String created;
  
    private String createdDate;
   
    private Long createdBy;
   
    private String verify;
  
    private String verifyDate;
   
    private Long verifyStaff;

   
    private String approval;

  
    private String approvedDate;

   
    private Long approvalStaff;

   
    private String authenty;

  
    private String authorisedDae;
   
    private Long authoriseStaff;
   
    private String blocklist;
   
    private String entry;
   
    private String mailsent;
    private String status;


    public SupplierEvaluationDTO(SupplierEvaluation supplierEvaluation) {
        DateUtil dateUtil = new DateUtil();
        this.id = supplierEvaluation.getId();
        this.branch = supplierEvaluation.getBranch();
        this.did = supplierEvaluation.getDid();
        this.company = supplierEvaluation.getCid();
        this.fromDate = dateUtil.getDateTOStringWk(supplierEvaluation.getFromdate());
        this.toDate = dateUtil.getDateTOStringWk(supplierEvaluation.getTodate());
        this.vid = supplierEvaluation.getVid();
        this.trnno = supplierEvaluation.getTrnno();
        this.created = supplierEvaluation.getCreated();
        this.createdDate = dateUtil.getDateTOStringWk(supplierEvaluation.getCreated_date());
        this.createdBy = supplierEvaluation.getCreated_by();
        this.verify = supplierEvaluation.getVerify();
        this.verifyDate = dateUtil.getDateTOStringWk(supplierEvaluation.getVerify_date());
        this.verifyStaff = supplierEvaluation.getVerify_staff();
        this.approval = supplierEvaluation.getApproval();
        this.approvedDate = dateUtil.getDateTOStringWk(supplierEvaluation.getApproved_date());
        this.approvalStaff = supplierEvaluation.getApproval_staff();
        this.authenty = supplierEvaluation.getAuthenty();
        this.authorisedDae =dateUtil.getDateTOStringWk(supplierEvaluation.getAuthorised_date());
        this.authoriseStaff = supplierEvaluation.getAuthorise_staff();
        this.blocklist = supplierEvaluation.getBlocklist();
        this.entry = dateUtil.getDateTime(supplierEvaluation.getEntry());
        this.mailsent = supplierEvaluation.getMailsent();
        this.status = supplierEvaluation.getStatus();
    }
}
