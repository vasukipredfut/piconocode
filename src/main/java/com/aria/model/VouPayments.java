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

import com.aria.dto.VouPaymentsDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "voupayments")
public class VouPayments {

	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    @Column
	    private Long trnid;

	    @Column
	    private String trnno;

	    @Column
	    @Temporal(TemporalType.DATE)
	    private Date trndate;

	    @Column
	    private Long trnweekid;

	    @Column
	    private LocalDate fmdate;

	    @Column
	    private LocalDate todate;

	    @Column
	    private Long vouid;

	    @Column
	    private Double amount;

	    @Column
	    private Long cby;

	    @Column
	    private String con;

	    @Column
	    private String verify;

	    @Column
	    private Long vby;

	    @Column
	    private String von;

	    @Column
	    private String approve;

	    @Column
	    private Long apby;

	    @Column
	    private String apon;

	    @Column
	    private String authenty;

	    @Column
	    private Long aby;

	    @Column
	    private String aon;

	    @Column
	    private Long did;

	    @Column
	    private Long bid;

	    @Column
	    private Long cid;

	    @Column
	    private String usecid;
	    
	    @Column
	    private String status;
	    @Column
	    private String reject;
	    @Column
	    private LocalDateTime rDate;
	    @Column
	    private Long rBy;
	    @Column
	    private LocalDateTime conNew;
	    @Column
	    private LocalDateTime vonNew;
	    @Column
	    private LocalDateTime aponNew;
	    @Column
	    private LocalDateTime aonNew;
	    
	    
	    
public VouPayments(VouPaymentsDto vouPayments) {
			
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
