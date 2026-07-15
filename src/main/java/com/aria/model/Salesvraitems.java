package com.aria.model;

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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name="salesvraitems")
public class Salesvraitems {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	@Column
	private String trnno;
	
	@Column
	private Long vid;
	
	@Column
	private Long billid;
	
	@Column
	private String bno;
	
	@Column
	@Temporal(TemporalType.DATE)
	private Date bdt;
	
	@Column
	private String reftype;
	
	@Column
	private Long refid;
	
	@Column(name="taxper")
	private Double taxper;
	
	@Column
	private Double urate;
	
	@Column
	private Long qty;
	
	@Column
	private Double taxamt;
	
	@Column
	private Double ltotal;
	
	@Column
	private Long bid;
	
	@Column
	private Long did;
	
	@Column
	private Long cid;
	
	@Column
	private Long enteredby;
	
	@Column
	private LocalDateTime enteredon;
	
	@Column
	private String batchno;
	
	@Column
	private Double cgst;
	
	@Column
	private Double sgst;
	
	@Column
	private Double igst;
	
	@Column
	private Double tcs;
	
	@Column(name="ltotal_withoutgst")
	private Double ltotalwithoutGST;
	
	@Column
	private Double discountamt;

	public Salesvraitems(Salesvra deductionBill) {
		this.bno = deductionBill.getBillno();
		this.trnno=deductionBill.getTrnno();
		this.bdt = deductionBill.getBilldate();
		this.urate = deductionBill.getAmount();
		this.ltotal = deductionBill.getAmount();
		this.bid = deductionBill.getBranch();
		this.did = deductionBill.getDept();
		this.cid = deductionBill.getCompany();
		this.enteredby = deductionBill.getCreatedBy();
		this.enteredon = deductionBill.getCreatedTime();
		
	}
	
	
	
}
