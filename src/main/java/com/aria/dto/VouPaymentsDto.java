package com.aria.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import jakarta.persistence.Column;

import com.aria.model.VouPayments;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VouPaymentsDto {

	    private Long id;
	    private Long trnid;
	    private String trnno;
	    private Date trndate;
	    private Long trnweekid;
	    private LocalDate fmdate;
	    private LocalDate todate;
	    private Long vouid;
	    private Double amount;
	    private Long cby;
	    private String con;
	    private String verify;
	    private Long vby;
	    private String von;
	    private String approve;
	    private Long apby;
	    private String apon;
	    private String authenty;
	    private Long aby;
	    private String aon;
	    private Long did;
	    private Long bid;
	    private Long cid;
	    private String usecid;
	    private String status;
	    private String reject;	 
	    private LocalDateTime rDate;	
	    private Long rBy;	 
	    private LocalDateTime conNew;	
	    private LocalDateTime vonNew;	   
	    private LocalDateTime aponNew;	
	    private LocalDateTime aonNew;
	  private List<Long> singlevouid;
		public VouPaymentsDto(VouPayments vouPayments) {
			
			this.id = vouPayments.getId();
			this.trnid = vouPayments.getTrnid();
			this.trnno = vouPayments.getTrnno();
			this.trndate = vouPayments.getTrndate();
			this.trnweekid = vouPayments.getTrnweekid();
			this.fmdate = vouPayments.getFmdate();
			this.todate = vouPayments.getTodate();
			this.vouid = vouPayments.getVouid();
			this.amount = vouPayments.getAmount();
			this.cby = vouPayments.getCby();
			this.con = vouPayments.getCon();
			this.verify = vouPayments.getVerify();
			this.vby = vouPayments.getVby();
			this.von = vouPayments.getVon();
			this.approve = vouPayments.getApprove();
			this.apby = vouPayments.getAby();
			this.apon = vouPayments.getApon();
			this.authenty = vouPayments.getAuthenty();
			this.aby = vouPayments.getAby();
			this.aon = vouPayments.getAon();
			this.did = vouPayments.getDid();
			this.bid = vouPayments.getBid();
			this.cid = vouPayments.getCid();
			this.usecid = vouPayments.getUsecid();
			this.status=vouPayments.getStatus();
			this.reject=vouPayments.getReject();
			this.rBy=vouPayments.getRBy();
			this.rDate=vouPayments.getRDate();
			this.conNew=vouPayments.getConNew();
			this.aponNew=vouPayments.getAponNew();
			this.vonNew=vouPayments.getVonNew();
			this.aonNew=vouPayments.getAonNew();
		}
	    
	    
	    
	    
}
