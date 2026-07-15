package com.aria.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.aria.model.DpEntryStoredProduct;
import com.aria.utils.DateUtil;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DpEntryStoredProductDto {

    private Long id;

    public Long entryId;

    public String itemName;

    public String description;

    public Long colorId;

    public String colorName;

    private String batchNo;

    private Long batchId;

    private String packageType;

    private String perBagWeight;

    private String totalQuantity;

    private String selectQuantity;

    private Long itemType;

    private Long itemId;

    private Double sgst;

    private Double cgst;

    private Double igst;

    private Double billamount;

    private String poNumber;
    private String deliveryduedate;
    private Long poId;
    private Double poItemQty;


    private Long proposalId;

    private Long billRefId;

    private String lot;
    private List<DpEntryStoredBinDetailsDto> binDetails;

    private String warranty_Date;

    private String warranty_certificate;

    private String unitCost;

    private String unitRate;

    public Double freightCharges;

    public Double packingAndForwardingCharges;

    public String dueDate;

    public Double score;

    private Double pendingQty;

    public String payType;

    public QualityInspectionDTO qualityInspection;

    public Long branchId;
    public Long departmentId;
    public String category;

    public List<ConsumptionDetailDto> consumptionDetails;




    public DpEntryStoredProductDto(DpEntryStoredProduct storedProduct) {

        DateUtil dateUtil = new DateUtil();
        this.id = storedProduct.getId();
        this.entryId = storedProduct.getEntryId();

        if (storedProduct.getColor() != null) {
            this.colorId = storedProduct.getColor().getId();
            this.colorName = storedProduct.getColor().getName();
        }
        this.batchNo = storedProduct.getBatchNo();
        this.batchId = storedProduct.getBatchId();
        this.packageType = storedProduct.getPackageType();
        this.perBagWeight = storedProduct.getPerBagWeight();
        this.totalQuantity = storedProduct.getTotalQuantity();
        this.itemId = storedProduct.getItemId();
        this.itemType = storedProduct.getItemType();
        this.sgst = storedProduct.getSgst();
        this.cgst = storedProduct.getCgst();
        this.igst = storedProduct.getIgst();
        this.billamount = storedProduct.getBillamount();
        this.itemName = storedProduct.getItemName();
        this.poId = storedProduct.getPo_id();
        this.poNumber = storedProduct.getPo_number();
        this.billRefId = storedProduct.getBill_ref_id();
        this.lot = storedProduct.getLot();
        this.binDetails = storedProduct.getBinDetails() != null ? storedProduct.getBinDetails().stream()
                .map(field -> new DpEntryStoredBinDetailsDto(field)).collect(Collectors.toList()) : null;
        if (storedProduct.getWarranty_Date() != null) {
            this.warranty_Date = dateUtil.getDateTOStringWk(storedProduct.getWarranty_Date());
        }
        this.warranty_certificate = storedProduct.getWarranty_certificate();
        this.unitCost = storedProduct.getUnitCost();
        this.unitRate = storedProduct.getUnitRate();

        this.consumptionDetails = storedProduct.getConsumptionDetails() != null ? storedProduct.getConsumptionDetails().stream()
                .map(field -> new ConsumptionDetailDto(field)).collect(Collectors.toList()) : null;
        
    }


    public DpEntryStoredProductDto(EntryProductList storedProduct) {

        DateUtil dateUtil = new DateUtil();
        this.id = storedProduct.getId();
        this.entryId = storedProduct.getEntryId();
        this.colorId = storedProduct.getColorId();
        this.colorName = storedProduct.getColorName();
        this.batchNo = storedProduct.getBatchNo();
        this.batchId = storedProduct.getBatchId();
        this.packageType = storedProduct.getPackageType();
        this.perBagWeight = storedProduct.getPerBagWeight();
        this.totalQuantity = storedProduct.getTotalQuantity();
        this.itemId = storedProduct.getItemId();
        this.itemType = storedProduct.getItemType();
        this.sgst = storedProduct.getSgst();
        this.cgst = storedProduct.getCgst();
        this.igst = storedProduct.getIgst();
        this.billamount = storedProduct.getBillamount();
        this.itemName = storedProduct.getItemName();
        this.poId = storedProduct.getPoId();
        this.poNumber = storedProduct.getPoNumber();
        this.billRefId = storedProduct.getBillRefId();
        this.lot = storedProduct.getLot();
        if (storedProduct.getWarrantyDate() != null) {
            this.warranty_Date = dateUtil.getDateTOStringWk(storedProduct.getWarrantyDate());
        }
        this.warranty_certificate = storedProduct.getWarrantyCertificate();
        this.unitCost = storedProduct.getUnitCost();
        this.unitRate = storedProduct.getUnitRate();
        if (storedProduct.getPoItemQty() != null) {
            this.poItemQty = storedProduct.getPoItemQty();
        } else {
            this.poItemQty =  0.0;
        }
        this.proposalId = storedProduct.getProposalId();
        this.freightCharges = storedProduct.getFreightCharges();
        this.packingAndForwardingCharges = storedProduct.getPackingAndForwardingCharges();
        if (storedProduct.getDeliveryduedate() != null) {
            this.deliveryduedate = dateUtil.getDateTOStringWk(storedProduct.getDeliveryduedate());
            this.dueDate = dateUtil.getDateTOStringWk(storedProduct.getDeliveryduedate());
        }
        if (storedProduct.isAdvanceType()) {
            this.payType = "CreditORDebit";
        } else {
            this.payType = "ETransfer";
        }

		if (storedProduct.getSgst1() != null ) {
			this.sgst = storedProduct.getSgst1();
		} else {
			this.sgst = storedProduct.getSgst();
		}
		if (storedProduct.getCgst1() != null ) {
			this.cgst = storedProduct.getCgst1();
		} else {
			this.cgst = storedProduct.getCgst();
		}
		if (storedProduct.getIgst1() != null ) {
			this.igst = storedProduct.getIgst1();
		} else {
			this.igst = storedProduct.getIgst();
		}

    }
}
