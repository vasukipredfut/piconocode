package com.aria.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface VoucherPrintList {
	    Long getId();
	    String getVoucherNo();
	    Double getAmount();
	    String getPayTo();
	    String getPayMode();
	    String getCreatedPerson();
	    LocalDateTime getCreatedDate();
	    LocalDateTime getVerifiedDate();
	    String getVerifiedPerson();
	    LocalDateTime getApprovedDate();
	    String getApprovedEmployee();
	    LocalDateTime getAuthorizedDate();
	    String getAuthorizedPerson();
	    String getCompanyName();
	    String getCAddress();
	    String getBranchName();
        String getVouType();
	    LocalDate getVoucherDate();
	    String getDescription();
        Long getEmpId();
	    String getBankName();
	    String getBankAcNo();
	    String getIfscCode();
	    LocalDateTime getRejectedDate();
	    String getRejectedPerson();
	    
	    
}
