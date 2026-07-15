package com.aria.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.aria.dto.VoucherSharedMemberDetailsDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "vexgen")
public class VoucherSharedMemberDetails {
	
	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    @Column
	    private String trnno;

	    @Column
	    private Long billid;

	    @Column
	    private Long bid;

	    @Column
	    private String bno;

	    @Column
	    private LocalDate bdt;

	    @Column
	    private String batchno;

	    @Column
	    private Long refid;

	    @Column
	    private String reftype;

	    @Column
	    private Double urate;

	    @Column
	    private Double ltotal;

	    @Column
	    private Double fre;

	    @Column
	    private Double oth;

	    @Column
	    private Double avgcost;

	    @Column
	    private Double cost;

	    @Column
	    private Long aby;

	    @Column
	    private Long vby;

	    @Column
	    private Long authenty;

	    @Column
	    private Long conid;

	    @Column
	    private String contype;

	    @Column
	    private String conname;

	    @Column
	    private Long condept;

	    @Column
	    private Long conbranch;

	    @Column
	    private String extype;

	    @Column
	    private Long conzoneid;

	    @Column
	    private String conzone;

	    @Column
	    private String bankacno;

	    @Column
	    private String bankcode;

	    @Column
	    private Long company;
public VoucherSharedMemberDetails( VoucherSharedMemberDetailsDto voucherSharedMemberDetails) {
			
			this.id = voucherSharedMemberDetails.getId();
			this.trnno = voucherSharedMemberDetails.getTrnno();
			this.bid = voucherSharedMemberDetails.getBid();
			this.batchno = voucherSharedMemberDetails.getBatchno();
			this.urate = voucherSharedMemberDetails.getUrate();
			this.conid = voucherSharedMemberDetails.getConid();
			this.contype = voucherSharedMemberDetails.getContype();
			this.conname = voucherSharedMemberDetails.getConname();
			this.condept = voucherSharedMemberDetails.getCondept();
			this.conbranch = voucherSharedMemberDetails.getConbranch();
		}
}
