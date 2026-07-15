package com.aria.dto;


import java.util.Date;

public interface EntryProductList {


    Long getId();

    Long getEntryId();

    String getItemName();

    String getDescription();

    Long getColorId();

    String getColorName();

    String getBatchNo();

    Long getBatchId();

    String getPackageType();

    String getPerBagWeight();

    String getTotalQuantity();

    String getSelectQuantity();

    Long getItemType();

    Long getItemId();

    Double getSgst();

    Double getCgst();

    Double getIgst();

    Double getSgst1(); // billInwarditemReceived

    Double getCgst1();  // billInwarditemReceived

    Double getIgst1();  // billInwarditemReceived

    Double getBillamount();

    String getPoNumber();

    Date getDeliveryduedate();

    Long getPoId();

    Double getPoItemQty();

    Long getProposalId();

    Long getBillRefId();

    String getLot();

    Date getWarrantyDate();

    String getWarrantyCertificate();

    String getUnitCost();

    String getUnitRate();

    Double getFreightCharges();

    Double getPackingAndForwardingCharges();

    String getDueDate();

    Double getScore();

    Double getPendingQty();

    String getPayType();

    QualityInspectionDTO getQualityInspection();

    boolean isAdvanceType();


}
