package com.aria.dto;

import java.time.LocalDate;
import java.util.Date;

public interface LorryArrivalDataList {
	Long getBillId();
    Date getBilDate();
    String getBillNo();
    String getEWayBil();
    Date getLorryDate();
    Double getBillAmount();
    String getLorryNo();
    String getLorrySerialNo();
    String getVendorName();
    String getInwardBranch();
    String getDeliverdBranch();
    String getCamp();
    String getLot();
    String getUnitRate();
    String getUnitCost();
    Double getRcvQty();
    String getPacQuantity();
    String getFreightAmount();
    Date getBillInwarDate();
    String getPoNumber();
    String getVendorCity();
    String getStatus();
    Long getStockId();
    String getInwardStatus();
    Date getInwardCreatedDate();
    Date getStockInwardDate();
    Date getApprovedDate();   
    String getProductName();
    String getBatchNo();
    String getQcReportStatus();
    String getAudiStatus();
    
}
