package com.aria.dto;

import com.aria.model.HoSalesInvoiceItems;
import com.aria.model.HoSalesOrderItems;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class HoSalesOrderItemsDTO {

    private Long id;

    private Long orderId;

    private Long hoInvoiceItemId;

    private Long hoItemId;

    private String itemDescription;

    private String otherStateDescription;

    private String isFree;

    private String isSchemeGift;

    private String hsnCode;

    private Long hoitemGroupId;

    private String invQtyPcs;

    private String invUom;

    private Long packingFactor;

    private String invoiceQty;

    private String unitPrice;

    private String totalAmount;

    private String calcdiscountAmount;

    private String cessRate;

    private String cessAmount;

    private String netAmount;

    private Long cgst;

    private String cgstPercent;

    private String cgstValue;

    private Long sgst;

    private String sgstPercent;

    private String sgstValue;

    private Long igst;

    private String igstPercent;

    private String igstValue;

    private String totalVolume;

    private Long sortOrder;

    private String containerNo;

    private Long earmItemId;

    public HoSalesOrderItemsDTO(HoSalesOrderItems hoSalesInvoice) {
        this.id = hoSalesInvoice.getId();
        this.orderId = hoSalesInvoice.getOrderId();
        this.hoInvoiceItemId = hoSalesInvoice.getHoInvoiceItemId();
        this.hoItemId = hoSalesInvoice.getHoItemId();
        this.itemDescription = hoSalesInvoice.getItemDescription();
        this.otherStateDescription = hoSalesInvoice.getOtherStateDescription();
        this.isFree = hoSalesInvoice.getIsFree();
        this.isSchemeGift = hoSalesInvoice.getIsSchemeGift();
        this.hsnCode = hoSalesInvoice.getHsnCode();
        this.hoitemGroupId = hoSalesInvoice.getHoitemGroupId();
        this.invQtyPcs = hoSalesInvoice.getInvQtyPcs();
        this.invUom = hoSalesInvoice.getInvUom();
        this.packingFactor = hoSalesInvoice.getPackingFactor();
        this.invoiceQty = hoSalesInvoice.getInvoiceQty();
        this.unitPrice = hoSalesInvoice.getUnitPrice();
        this.totalAmount = hoSalesInvoice.getTotalAmount();
        this.calcdiscountAmount = hoSalesInvoice.getCalcdiscountAmount();
        this.cessRate = hoSalesInvoice.getCessRate();
        this.cessAmount = hoSalesInvoice.getCessAmount();
        this.netAmount = hoSalesInvoice.getNetAmount();
        this.cgst = hoSalesInvoice.getCgst();
        this.cgstPercent = hoSalesInvoice.getCgstPercent();
        this.cgstValue = hoSalesInvoice.getCgstValue();
        this.sgst = hoSalesInvoice.getSgst();
        this.sgstPercent = hoSalesInvoice.getSgstPercent();
        this.sgstValue = hoSalesInvoice.getSgstValue();
        this.igst = hoSalesInvoice.getIgst();
        this.igstPercent = hoSalesInvoice.getIgstPercent();
        this.igstValue = hoSalesInvoice.getIgstValue();
        this.totalVolume = hoSalesInvoice.getTotalVolume();
        this.sortOrder = hoSalesInvoice.getSortOrder();
        this.containerNo = hoSalesInvoice.getContainerNo();
        this.earmItemId = hoSalesInvoice.getEarmItemId();
    }
}
