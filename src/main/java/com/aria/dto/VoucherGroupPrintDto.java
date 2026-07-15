package com.aria.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor

public class VoucherGroupPrintDto {
	
	    private Long id;
	    private String grpNo;
	    private LocalDate fmDate;
	    private LocalDate toDate;
	    private Date vouDate;
	    private Long cVouId;
	    private String companyName;
	    private String cAddress;
	    private String branchName;
	    private String cVouName;
	    private String createdPerson;
	    private String createdDate;
	    private String verifiedPerson;
	    private String verifiedDate;
	    private String approvedPerson;
	    private String approvedDate;
	    private String rejectedPerson;
	    private String authorizedDate;
	    private String authorizedPerson;
	    private LocalDateTime rejectedDate;
	    private LocalDateTime newCreatedDate;
	    private LocalDateTime newApprovedDate;
	    private LocalDateTime newVerifiedDate;
	    private LocalDateTime newAuthorizedDate;
	    
	    List<GroupNameListDto> list;
	public VoucherGroupPrintDto(VoucherGroupPrintList voucherGroupPrintList) {	
	
		this.id = voucherGroupPrintList.getId();
		this.grpNo = voucherGroupPrintList.getGrpNo();
		this.fmDate = voucherGroupPrintList.getFmDate();
		this.toDate = voucherGroupPrintList.getToDate();
		this.vouDate = voucherGroupPrintList.getVouDate();
		this.cVouId = voucherGroupPrintList.getCVouId();
		this.companyName = voucherGroupPrintList.getCompanyName();
		this.cAddress = voucherGroupPrintList.getCAddress();
		this.branchName = voucherGroupPrintList.getBranchName();	
		this.createdDate=voucherGroupPrintList.getCreatedDate();
		this.createdPerson=voucherGroupPrintList.getCreatedPerson();
		 this.verifiedPerson = voucherGroupPrintList.getVerifiedPerson();
	     this.verifiedDate = voucherGroupPrintList.getVerifiedDate();
	      this.approvedPerson = voucherGroupPrintList.getApprovedPerson();
	      this.approvedDate = voucherGroupPrintList.getApprovedDate();
	     this.rejectedPerson = voucherGroupPrintList.getRejectedPerson();
	     this.rejectedDate = voucherGroupPrintList.getRejectedDate();
	     this.authorizedPerson = voucherGroupPrintList.getAuthorizedPerson();
	     this.authorizedDate = voucherGroupPrintList.getAuthorizedDate();
	     this.newCreatedDate = voucherGroupPrintList.getNewCreatedDate();
	     this.newApprovedDate = voucherGroupPrintList.getNewApprovedDate();
	     this.newVerifiedDate = voucherGroupPrintList.getNewVerifiedDate();
	     this.newAuthorizedDate = voucherGroupPrintList.getNewAuthorizedDate();
	}
    
    
    

}
