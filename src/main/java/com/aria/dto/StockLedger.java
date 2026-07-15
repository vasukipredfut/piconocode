package com.aria.dto;

import java.util.Date;

 public interface StockLedger {

	 String getTypeName();
	 Long getId();

	 String getType();

	 String getTxnNo();

	 Date getTxnDate();

	 Date getAccountingDate();

	 String getNarration();

     Double getIssue();

     Double getReceipt();

	 String getBalance();

	 Double getQty();
	 Double getLot();

	 String getTrnType();
	 Long getBinNo();

}
