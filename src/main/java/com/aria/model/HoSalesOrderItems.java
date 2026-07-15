package com.aria.model;

import com.aria.dto.HoSalesOrderItemsDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "Ho_Sales_Order_Items")
public class HoSalesOrderItems {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long orderId;

    @Column
    private Long hoInvoiceItemId;

    @Column
    private Long hoItemId;
    @Column
    private String itemDescription;
    @Column
    private String otherStateDescription;
    @Column
    private String isFree;
    @Column
    private String isSchemeGift;
    @Column
    private String hsnCode;
    @Column
    private Long hoitemGroupId;
    @Column
    private String invQtyPcs;
    @Column
    private String invUom;
    @Column
    private Long packingFactor;
    @Column
    private String invoiceQty;
    @Column
    private String unitPrice;
    @Column
    private String totalAmount;
    @Column
    private String calcdiscountAmount;
    @Column
    private String cessRate;
    @Column
    private String cessAmount;
    @Column
    private String netAmount;
    @Column
    private Long cgst;
    @Column
    private String cgstPercent;
    @Column
    private String cgstValue;
    @Column
    private Long sgst;
    @Column
    private String sgstPercent;
    @Column
    private String sgstValue;
    @Column
    private Long igst;
    @Column
    private String igstPercent;
    @Column
    private String igstValue;
    @Column
    private String totalVolume;
    @Column
    private Long sortOrder;
    @Column
    private String containerNo;
    @Column
    private Long earmItemId;

    public HoSalesOrderItems(HoSalesOrderItemsDTO hoSalesInvoice) {
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
