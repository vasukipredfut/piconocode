package com.aria.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VoucherPrintDto {
	private Long id;
    private String voucherNo;
    private Double amount;
    private String payTo;
    private String payMode;
    private String createdPerson;
    private LocalDateTime createdDate;
    private LocalDateTime verifiedDate;
    private String verifiedPerson;
    private LocalDateTime approvedDate;
    private String approvedEmployee;
    private LocalDateTime authorizedDate;
    private String authorizedPerson;   
    private String companyName;
    private String cAddress;
    private String branchName;
    private String vouType;
    private LocalDate voucherDate;
    private String description;
    private Long empId;
    private String bankName;
    private String bankAcNo;
    private String ifscCode;
    private String rejectedPerson;
    private LocalDateTime rejectedDate;
    
   
   List<VoucherItemsPrintDto> sitems;
	public VoucherPrintDto(VoucherPrintList voucherPrintList) {

		this.id = voucherPrintList.getId();
		this.voucherNo = voucherPrintList.getVoucherNo();
		this.amount = voucherPrintList.getAmount();
		this.payTo = voucherPrintList.getPayTo();
		this.payMode = voucherPrintList.getPayMode();
		this.createdPerson = voucherPrintList.getCreatedPerson();
		this.createdDate = voucherPrintList.getCreatedDate();
		this.verifiedDate = voucherPrintList.getVerifiedDate();
		this.verifiedPerson = voucherPrintList.getVerifiedPerson();
		this.approvedDate = voucherPrintList.getApprovedDate();
		this.approvedEmployee = voucherPrintList.getApprovedEmployee();
		this.authorizedDate = voucherPrintList.getAuthorizedDate();
		this.authorizedPerson = voucherPrintList.getAuthorizedPerson();
		this.companyName = voucherPrintList.getCompanyName();
		this.cAddress = voucherPrintList.getCAddress();
		this.branchName = voucherPrintList.getBranchName();
		this.voucherDate = voucherPrintList.getVoucherDate();
		this.description = voucherPrintList.getDescription();
		this.bankName = voucherPrintList.getBankName();
		this.bankAcNo = voucherPrintList.getBankAcNo();
		this.ifscCode = voucherPrintList.getIfscCode();
		this.vouType=voucherPrintList.getVouType();
		this.empId=voucherPrintList.getEmpId();
		this.rejectedDate=voucherPrintList.getRejectedDate();
		this.rejectedPerson=voucherPrintList.getRejectedPerson();
		}
    
    
}
