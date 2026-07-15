package com.aria.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

public interface VoucherGroupPrintList {
	  Long getId();
	    String getGrpNo();
	    LocalDate getFmDate();
	    LocalDate getToDate();
	    Date getVouDate();
	    Long getCVouId();
	    String getCompanyName();
	    String getCAddress();
	    String getBranchName();
	    String getCreatedPerson();
	    String getCreatedDate();
	    String getVerifiedPerson();
	    String getVerifiedDate();
	    String getApprovedPerson();
	    String getApprovedDate();
	    String getRejectedPerson();
	    String getAuthorizedDate();
	    String getAuthorizedPerson();
	    LocalDateTime getRejectedDate();
	    LocalDateTime getNewCreatedDate();
	    LocalDateTime getNewApprovedDate();
	    LocalDateTime getNewVerifiedDate();
	    LocalDateTime getNewAuthorizedDate();
}
