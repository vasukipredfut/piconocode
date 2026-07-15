package com.aria.dto;

import java.util.Date;

public interface TransactionList {

	Long getRefTrnId();

	String getTrnNo();

	String getTrnName();

	Long getIssueBranchId();

	Long getRequestBranchId();

	Long getIssueDeptId();

	Long getRequestDeptId();
	Long getRefId();

	String getRefIdProductName();

	Date getTrnDate();
	String getQty();

}