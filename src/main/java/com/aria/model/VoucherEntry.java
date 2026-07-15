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

import com.fasterxml.jackson.annotation.JsonFormat;
import com.aria.dto.VoucherEnrtyDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "vouchers")
public class VoucherEntry {
	@Id
	@GeneratedValue(strategy =GenerationType.IDENTITY )
	 private Long id;

	    @Column
	    private Long company;
	    
	    @Column
	    private Long formid;
	    
	    @Column
	    private String vouno;
	    
	    @Column
	    @JsonFormat(pattern = "yyyy-MM-dd")
	    private LocalDate  voudt;
	    
	    @Column
	    private Double amt;
	    
	    @Column
	    private Long voushift;

	    @Column
	    private String trnno;
	    
	    @Column
	    private String inv;
	    
	    @Column	   
	    private LocalDateTime idate;

	    @Column
	    private Long istaff;
	    
	    @Column
	    private String verify;
	    
	    @Column	   
	    private LocalDateTime vdate;
	    
	    
	    @Column
	    private Long vstaff;
	    
	    @Column
	    private String approval;
	    
	    @Column	 
	    private LocalDateTime apdate;
	    
	    @Column
	    private Long apstaff;
	    
	    @Column
	    private String authenty;
	    
	    @Column	  
	    private LocalDateTime adate;

	    @Column
	    private Long astaff;
	    
	    @Column
	    private String hoprocess;
	    
	    @Column
	    @Temporal(TemporalType.DATE)
	    private Date hdate;

	    @Column
	    private Long hstaff;
	    
	    @Column
	    private String payment;
	    
	    @Column
	    @Temporal(TemporalType.DATE)
	    private Date pdate;

	    @Column
	    private Long pstaff;
	    
	    @Column
	    private String cform;
	    
	    @Column
	    @Temporal(TemporalType.DATE)
	    private Date cformdt;

	    @Column
	    private Long cfstaff;
	    
	    @Column
	    private String form17;
	    
	    @Column
	    @Temporal(TemporalType.DATE)
	    private Date f17dt;

	    @Column
	    private Long f17staff;
	    
	    @Column
	    private Long department;
	    
	    @Column
	    @Temporal(TemporalType.DATE)
	    private Date bentdt;

	    @Column
	    private String vtype;
	    
	    @Column
	    private Integer items;
	    
	    @Column
	    private String itemtype;

	    @Column
	    private LocalDateTime entry;

	    @Column
	    private String bendt;
	    
	    @Column
	    private String iendt;
	    @Column
	    private String vendt;
	    @Column
	    private String apendt;
	    @Column
	    private String aendt;
	    @Column
	    private Long branch;
	    @Column
	    private Long dept;
	    @Column
	    private String pmode;
	    @Column
	    private Long payto;
	    @Column
	    private String bankacno;
	    @Column
	    private String bankcode;
	    @Column
	    private String paytable;
	    @Column
	    private String usecid;
	    @Column
	    private String vousubmit;
	    @Column
	    private Long sourcebranch;
	    @Column
	    private Long destinbranch;
	    @Column
	    private Long vousubmstaff;
	    @Column
	    private String vousubmtable;
	    @Column
	    @Temporal(TemporalType.DATE)
	    private Date vousubmdate;
	    @Column
	    private String submenddt;
	    @Column
	    private String hofwd;
	    @Column
	    private Long hofwdstaff;
	    @Column
	    private String hofwdtable;
	    @Column
	    @Temporal(TemporalType.DATE)
	    private Date hofwddate;
	    @Column
	    private String hofwdenddt;
	    @Column
	    private String submittrnno;
	    @Column
	    private String vourecd;
	    @Column
	    private Long vourecdby;
	    @Column
	    @Temporal(TemporalType.DATE)
	    private Date vourecddate;
	    @Column
	    private String vourecdtrnno;
	    @Column
	    private String horeceived;
	    @Column
	    private Long horeceivedby;
	    
	    @Column
	    @Temporal(TemporalType.DATE)
	    private Date horecddate;
	    @Column
	    private String payableToName;
	    @Column
	    private String ifscCode;
	    @Column
	    private String vouType;
	    @Column
	    private String status;
	    @Column
	    private String grpNo;
	    @Column
	    private String reject;
	    @Column
	    private Long rBy;
	    @Column
	    private LocalDateTime rDate;
	  @Column
	  private String vouName;
	    
	        
		public VoucherEntry(VoucherEnrtyDto voucherEnrtyDto) {
			
			this.id =voucherEnrtyDto.getId();
			this.company = voucherEnrtyDto.getFirmId();
			this.formid = voucherEnrtyDto.getVouId();
			this.voudt = voucherEnrtyDto.getVouDt();
			this.vouType = voucherEnrtyDto.getVouType();
			
			this.pmode = voucherEnrtyDto.getModeOfPay();
			this.amt = voucherEnrtyDto.getAmt();
			this.bankacno = voucherEnrtyDto.getBankAcNo();
			this.bankcode = voucherEnrtyDto.getBankCode();
			this.payableToName = voucherEnrtyDto.getPayableToName();
			this.ifscCode = voucherEnrtyDto.getIfscCode();
			this.dept = voucherEnrtyDto.getDepartment();
			this.branch = voucherEnrtyDto.getBranch();
			this.grpNo=voucherEnrtyDto.getGrpNo();
		}
	    
	    

}
