package com.aria.dto;

import java.time.LocalDate;
import java.util.Date;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LorryArrivalDataDto {
	 private Long billId;
	    private Date bilDate;
	    private String billNo;
	    private String eWayBil;
	    private Date lorryDate;
	    private Double billAmount;
	    private String lorryNo;
	    private String lorrySerialNo;
	    private String vendorName;
	    private String inwardBranch;
	    private String deliverdBranch;
	    private String camp;
	    private String lot;
	    private String unitRate;
	    private String unitCost;
	    private Double rcvQty;
	    private String pacQuantity;
	    private String freightAmount;
	    private Date billInwarDate;
	    private String poNumber;
	    private String vendorCity;
	    private String stock_status;
	    private Long stockId;
	    private String productName;
	    private String verificationStatus;
	    private Date updatedOn;
	    private String qcReportStatus;
	    private String batchNo;
	   
	    
		public LorryArrivalDataDto(LorryArrivalDataList lorryArrivalDataList) {
			super();
			this.billId = lorryArrivalDataList.getBillId();
			this.bilDate = lorryArrivalDataList.getBilDate();
			this.billNo = lorryArrivalDataList.getBillNo();
			this.eWayBil = lorryArrivalDataList.getEWayBil();
			this.lorryDate = lorryArrivalDataList.getLorryDate();
			this.billAmount = lorryArrivalDataList.getBillAmount();
			this.lorryNo = lorryArrivalDataList.getLorryNo();
			this.lorrySerialNo = lorryArrivalDataList.getLorrySerialNo();
			this.vendorName = lorryArrivalDataList.getVendorName();
			this.inwardBranch = lorryArrivalDataList.getInwardBranch();
			this.deliverdBranch = lorryArrivalDataList.getDeliverdBranch();
			this.camp = lorryArrivalDataList.getCamp();
			this.lot = lorryArrivalDataList.getLot();
			this.unitRate = lorryArrivalDataList.getUnitRate();
			this.unitCost = lorryArrivalDataList.getUnitCost();
			this.rcvQty = lorryArrivalDataList.getRcvQty();
			this.pacQuantity = lorryArrivalDataList.getPacQuantity();
			this.freightAmount = lorryArrivalDataList.getFreightAmount();
			this.billInwarDate = lorryArrivalDataList.getBillInwarDate();
			this.poNumber = lorryArrivalDataList.getPoNumber();
			this.vendorCity=lorryArrivalDataList.getVendorCity();
			this.stock_status=lorryArrivalDataList.getStatus();
			this.stockId=lorryArrivalDataList.getStockId();
			this.productName=lorryArrivalDataList.getProductName();
			this.batchNo=lorryArrivalDataList.getBatchNo();
			
			
		}
	    
}
