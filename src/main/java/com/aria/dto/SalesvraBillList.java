package com.aria.dto;

import java.util.Date;

public interface SalesvraBillList {
	 Long getId();
	    String getBillno();
	    String getBilldate();
	    String getEmployeename();
	    String getDeductiontype();
	    String getCompanyname();
	    String getCaddress();
	    String getTrnno();
	    Double getAmt();
	    String getCcity();
	    String getPlant();
	    Double getCgst();
	    Double getSgst();
	    Double getIgst();
}
