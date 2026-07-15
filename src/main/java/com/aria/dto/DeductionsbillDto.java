package com.aria.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DeductionsbillDto {

	public Long id;

	public Date messvoucherDate;

	public String billno;

	public String party;

	public LocalDate billdate;

	public Double discount;

	public Double roundoff;

	public Double amount;

	public Long billstaff;

	public String trnno;

	public LocalDateTime createdTime;

	private Long createdBy;

	public String verify;

	public LocalDateTime verifyDate;

	public Long verifyStaff;

	public String approval;

	public LocalDateTime approvalDate;

	public Long approvalStaff;

	public String authenty;

	public LocalDateTime authentyDate;

	public Long authentyStaff;

	public String hoprocess;

	public Long branch;

	public Long dept;

	public Long company;

	public LocalDateTime entry;

	public String status;

	public String place;

	public String disc;

	public String batchno;

	public String bin;

	public String billitemsid;

	public Double handlingCharges;

	public String transportMode;

	public String vehicleNo;

	public Double tcsAmount;

	public Double tcsPercentage;

	public String ebillSend;

	public String ebillReceive;

	public String paymentType;

	public String totable;

	public String refnumber;

	public String messvoucher;

	public Long printedFlag;

	public Long schemeid;

}
