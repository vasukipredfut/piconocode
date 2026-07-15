package com.aria.dto;


import java.time.LocalDateTime;
import java.util.Date;
import com.aria.model.ProductionEntry;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductionEntryDto {

	private Long id;

	private String dcType;

	private Long tsId;

	private String tsName;

	private String tsTable;

	private String trnNo;

	private Long dept;

	private Long branch;

	private Long shiftId;

	private String shiftDate;

	private Long pshift;

	private String psStatus;

	private Long batchId;

	private String batchNo;

	private String pplan;

	private Long journalId;

	private Long mmcId;

	private String mmcTable;

	private Long mcauxId;

	private String mcauxTable;

	private String dispName;

	private Long colid;

	private String colTable;

	private String colType;

	private String colCate;

	private Long dispUnit;

	private String dataType;

	private String dataResValues;

	private String c1;

	private String c2;

	private String c3;

	private String c4;

	private String c5;

	private String c6;

	private String c7;

	private String c8;

	private String c9;

	private String c10;

	private String c11;

	private String c12;

	private String c13;

	private double CTOTAL;

	private double CAVG;

	private String total;

	private String avg;

	private String usecid;

	private String upstatus;

	private int disporder;

	private String orderNumber;

	private Long godownId;

	private String godown;

	private String temperature;

	private String humidity;

	private Long createdBy;

	private Long updatedBy;

	private LocalDateTime created_date;

	private LocalDateTime updated_date;

	private String status;
	private String pType;

	public ProductionEntryDto(ProductionEntry productionEntry) {

		this.id = productionEntry.getId();
		this.dcType = productionEntry.getDcType();
		this.tsId = productionEntry.getTsId();
		this.tsName = productionEntry.getTsName();
		this.tsTable = productionEntry.getTsTable();
		this.trnNo = productionEntry.getTrnNo();
		this.dept = productionEntry.getDept();
		this.branch = productionEntry.getBranch();
		this.shiftId = productionEntry.getShiftId();
		//this.shiftDate = productionEntry.getShiftDate();
		this.pshift = productionEntry.getPshift();
		//this.psStatus = productionEntry.getPsStatus();
		this.batchId = productionEntry.getBatchId();
		this.batchNo = productionEntry.getBatchNo();
		//this.pplan = productionEntry.getPplan();
		this.journalId = productionEntry.getJournalId();
		this.mmcId = productionEntry.getMmcId();
		this.mmcTable = productionEntry.getMmcTable();
		this.mcauxId = productionEntry.getMcauxId();
		this.mcauxTable = productionEntry.getMcauxTable();
		this.dispName = productionEntry.getDispName();
		this.colid = productionEntry.getColid();
		this.colTable = productionEntry.getColTable();
		this.colType = productionEntry.getColType();
		this.colCate = productionEntry.getColCate();
		this.dispUnit = productionEntry.getDispUnit();
		this.dataType = productionEntry.getDataType();
		this.dataResValues = productionEntry.getDataResValues();
		this.c1 = productionEntry.getC1();
		this.c2 = productionEntry.getC2();
		this.c3 = productionEntry.getC3();
		this.c4 = productionEntry.getC4();
		this.c5 = productionEntry.getC5();
		this.c6 = productionEntry.getC6();
		this.c7 = productionEntry.getC7();
		this.c8 = productionEntry.getC8();
		this.c9 = productionEntry.getC9();
		this.c10 = productionEntry.getC10();
		this.c11 = productionEntry.getC11();
		this.c12 = productionEntry.getC12();
		this.c13 = productionEntry.getC13();
		this.CTOTAL = productionEntry.getCTOTAL();
		this.CAVG = productionEntry.getCAVG();
		this.total = productionEntry.getTotal();
		this.avg = productionEntry.getAvg();
		this.usecid = productionEntry.getUsecid();
		this.upstatus = productionEntry.getUpstatus();
		this.disporder = productionEntry.getDisporder();
		this.orderNumber = productionEntry.getOrderNumber();
		this.godown = productionEntry.getGodown();
		this.temperature = productionEntry.getTemperature();
		this.humidity = productionEntry.getHumidity();

		this.createdBy = productionEntry.getCreatedBy();
		this.updatedBy = productionEntry.getUpdatedBy();
		this.created_date = productionEntry.getCreated_date();
		if (productionEntry.getUpdated_date() != null) {
			this.updated_date = productionEntry.getUpdated_date();
		} else {
			this.updated_date = productionEntry.getCreated_date();
		}

	}
}
