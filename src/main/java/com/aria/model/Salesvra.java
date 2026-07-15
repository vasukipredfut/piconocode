package com.aria.model;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import com.aria.dto.DeductionDetails;
import com.aria.dto.SalesvraReqDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "salesvra")
public class Salesvra {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "mess_vou_date")
	private LocalDate messvoucherDate;

	@Column
	private String billno;

	@Column
	private String party;

	@Column(name = "billdt")
	@Temporal(TemporalType.DATE)
	private Date billdate;

	@Column
	private Double discount;

	@Column
	private Double roundoff;

	@Column(name = "amt")
	private Double amount;

	@Column(name = "bstaff")
	private Long billstaff;

	@Column
	private String trnno;

	@Column(name = "idate")
	private LocalDateTime createdTime;

	@Column(name = "istaff")
	private Long createdBy;

	@Column
	private String verify;

	@Column(name = "vdate")
	private LocalDateTime verifyDate;

	@Column(name = "vstaff")
	private Long verifyStaff;

	@Column
	private String approval;

	@Column(name = "apdate")
	private LocalDateTime approvalDate;

	@Column(name = "apstaff")
	private Long approvalStaff;

	@Column
	private String authenty;

	@Column(name = "adate")
	private LocalDateTime authentyDate;

	@Column(name = "astaff")
	private Long authentyStaff;

	@Column
	private String hoprocess;

	@Column(name = "bid")
	private Long branch;

	@Column(name = "did")
	private Long dept;

	@Column(name = "cid")
	private Long company;

	@Column
	private LocalDateTime entry;

	@Column
	private String status;

	@Column
	private String place;

	@Column
	private String description;

	@Column
	private String batchno;

	@Column
	private String bin;

	@Column
	private String billitemsid;

	@Column
	private Double handlingCharges;

	@Column
	private String transportMode;

	@Column
	private String vehicleNo;

	@Column(name = "tcs_amount")
	private Double tcsAmount;

	@Column(name = "tcs_percentage")
	private Double tcsPercentage;

	@Column(name = "ebill_send")
	private String ebillSend;

	@Column(name = "ebill_receive")
	private String ebillReceive;

	@Column(name = "payment_type")
	private String paymentType;

	@Column
	private String totable;

	@Column
	private String refnumber;

	@Column(name = "mess_Voucher")
	private Long messvoucher;

	@Column(name = "printed_flag")
	private Long printedFlag;

	@Column(name = "scheme_id")
	private Long schemeid;
	
	@Column(name="bill_month")
	private Long month;
	
	@Column(name="bill_year")
	private Long year;
	

	public Salesvra(SalesvraReqDto deductinBillReqDto, DeductionDetails details) {
		this.amount = details.getAmount();
		this.month=deductinBillReqDto.getMonth();
		this.year=deductinBillReqDto.getYear();
		this.branch = deductinBillReqDto.getBranch();
		this.company = deductinBillReqDto.getCompany();

	}

}
