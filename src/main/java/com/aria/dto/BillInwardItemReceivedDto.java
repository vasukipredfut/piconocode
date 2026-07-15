package com.aria.dto;

import com.aria.model.BillInwardItemReceived;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
public class BillInwardItemReceivedDto {

    private Long id;

    private Long inwardId;

    public Long itemId;

    public Long itemType;

    public String itemName;
    private String productCategoryType;
    private String description;

    private String packageType;

    private String receivedQuantity;

    private String packageQuantity;

    private Double convPackageQuantity;
    private String convPackageQuantityValue;

    private String batchNo;
    
    private Long batchId;
    
    private String lot;
    
    private Double totalwtaxreceive;
    
    private Double subtotalreceive;
    
    private Double cgst;
    
    private Double sgst;
    
    private Double igst;
    
    private String uom;
    
    private Double final_order_price;

    private String poNumber;

    private Long poId;

    private Long hoItemRecId;
    private Long poItemId;
    private String color;
    private String unitCost;

    private String unitRate;

    //Others Needed
    private Double pendingQty;
    private Double freightCharges;
    private Double packingCharges;

    public BillInwardItemReceivedDto(BillInwardItemReceived billInwardProduct) {
        this.id = billInwardProduct.getId();
        this.inwardId = billInwardProduct.getInwardId();
        this.itemId=billInwardProduct.getItem_id();
        this.itemName=billInwardProduct.getItem_Name();
        this.itemType=billInwardProduct.getItem_type();
        this.description = billInwardProduct.getDescription();
        this.packageType = billInwardProduct.getPackageType();
        this.packageQuantity = billInwardProduct.getPackageQuantity();
        this.convPackageQuantity = billInwardProduct.getConv_package_quantity();
        this.convPackageQuantityValue = billInwardProduct.getConv_package_quantity_value();
        this.receivedQuantity = billInwardProduct.getReceivedQuantity().toString();
        this.batchNo=billInwardProduct.getBatchNo();
        this.batchId=billInwardProduct.getBatchId();
        this.lot = billInwardProduct.getLot();
        this.cgst=billInwardProduct.getCgst();
        this.sgst=billInwardProduct.getSgst();
        this.igst=billInwardProduct.getIgst();
        this.uom=billInwardProduct.getUom();
        this.final_order_price=billInwardProduct.getFinal_order_price();
        this.subtotalreceive=billInwardProduct.getSubtotalreceive();
        this.totalwtaxreceive=billInwardProduct.getTotalwtaxreceive();
        this.poId=billInwardProduct.getPo_id();
        this.poNumber=billInwardProduct.getPo_number();
        this.hoItemRecId=billInwardProduct.getHoItemRecId();
        this.poItemId=billInwardProduct.getPoItemId();
        this.color=billInwardProduct.getColor();
        this.unitCost=billInwardProduct.getUnitCost();
        this.unitRate=billInwardProduct.getUnitRate();
        this.freightCharges=billInwardProduct.getFreightCharges();
        this.packingCharges=billInwardProduct.getPackingCharges();
        this.pendingQty=billInwardProduct.getPendingQty();

    }
}
