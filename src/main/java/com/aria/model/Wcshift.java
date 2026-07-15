package com.aria.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import com.aria.dto.WcShiftsDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "wcshift")
public class Wcshift {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "shiftdate")
	@Temporal(TemporalType.DATE)
	private Date shiftDate;

	@Column(name = "pshiftid")
	private Long pShiftId;

	@Column(name = "pshift")
	private String pShift;

	@Column(name = "wcid")
	private Long wcId;

	@Column(name = "wctable")
	private String wcTable;

	@Column(name = "shrs")
	private String shrs;

	@Column(name = "begin")
	private String begin;

	@Column(name = "e_end")
	private String eEnd;

	@Column(name = "strbegin")
	private String strbegin;

	@Column(name = "strend")
	private String strend;

	@Column(name = "sjid")
	private Long journalId;

	@Column(name = "proid")
	private Long proId;

	@Column(name = "batchno")
	private String batchNo;

	@Column(name = "batchid")
	private Long batchId;

	@Column(name = "bstatus")
	private String bStatus;

	@Column(name = "estatus")
	private String eStatus;

	@Column(name = "trnno")
	private String trnNo;

	@Column(name = "dept")
	private Long dept;

	@Column(name = "branch")
	private Long branch;

	@Column(name = "bby")
	private Long bby;

	@Column(name = "bon")
	private String bon;

	@Column(name = "eby")
	private Long eby;

	@Column(name = "eon")
	private String eon;

	@Column(name = "jentryby")
	private Long jEntryBy;

	@Column(name = "jentryon")
	private String jEntryOn;

	@Column(name = "jentry")
	private String jEntry;

	@Column(name = "astatus")
	private String aStatus;

	@Column(name = "acby")
	private Long acBy;

	@Column(name = "acon")
	private String acon;

	@Column(name = "reversedby")
	private String reversedBy;

	@Column(name = "reversedon")
	private Date reversedOn;

	@Column(name = "imagelocation")
	private String imageLocation;

	@Column(name = "salesorderid")
	private Long salesOrderId;

	@Column(name = "mrp")
	private Double MRP;

	@Column(name = "dominoCount")
	private Long dominoCount;



	public Wcshift(WcShiftsDto wcShiftsDto) {
		this.id = wcShiftsDto.getId();
		this.shiftDate = wcShiftsDto.getShiftDate();
		this.pShift = wcShiftsDto.getPShift();
		this.pShiftId = wcShiftsDto.getPShiftId();
		this.wcId = wcShiftsDto.getWcId();
		this.wcTable = wcShiftsDto.getWcTable();
		this.shrs = wcShiftsDto.getShrs();
		this.begin = wcShiftsDto.getBegin();
		this.strbegin = wcShiftsDto.getStrbegin();
		this.journalId = wcShiftsDto.getJournalId();
		this.proId = wcShiftsDto.getProId();
		this.batchNo = wcShiftsDto.getBatchNo();
		this.batchId = wcShiftsDto.getBatchId();
		this.bStatus = wcShiftsDto.getBStatus();
		this.eStatus = wcShiftsDto.getEStatus();
		this.trnNo = wcShiftsDto.getTrnNo();
		this.dept = wcShiftsDto.getDept();
		this.branch = wcShiftsDto.getBranch();
		this.bby = wcShiftsDto.getBby();
		this.bon = wcShiftsDto.getBon();
		this.eby = wcShiftsDto.getEby();
		this.eon = wcShiftsDto.getEon();
		this.jEntryBy = wcShiftsDto.getJEntryBy();
		this.jEntryOn = wcShiftsDto.getJEntryOn();
		this.jEntry = wcShiftsDto.getJEntry();
		this.aStatus = wcShiftsDto.getAStatus();
		this.acBy = wcShiftsDto.getAcBy();
		this.acon = wcShiftsDto.getAcon();
		this.reversedBy = wcShiftsDto.getReversedBy();
		this.reversedOn = wcShiftsDto.getReversedOn();
		this.imageLocation = wcShiftsDto.getImageLocation();
		this.salesOrderId = wcShiftsDto.getSalesOrderId();
		this.MRP = wcShiftsDto.getMRP();
	}
}
