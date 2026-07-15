package com.aria.model;

import com.aria.dto.BillInwardItemReceivedDto;
import com.aria.dto.GRNProducts;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@NoArgsConstructor
@Table(name = "bill_inward_product_item_received")
@Getter
@Setter
public class BillInwardItemReceived {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "inward_id")
    private Long inwardId;

    @Column
    private String description;

    @Column(name = "package_type")
    private String packageType;

    @Column(name = "package_quantity", updatable = false)
    private String packageQuantity;

    @Column
    private Double conv_package_quantity;

    @Column(name = "conv_package_quantity_value", updatable = false)
    private String conv_package_quantity_value;

    @Column(name = "received_quantity")
    private Double receivedQuantity;

    @Column(name = "batch_no")
    private String batchNo;

    @Column(name = "batch_id")
    private Long batchId;


    @Column
    private String lot;

    @Column
    private Double totalwtaxreceive;

    @Column
    private Double subtotalreceive;

    @Column
    private Double cgst;

    @Column
    private Double sgst;

    @Column
    private Double igst;

    @Column
    private Double final_order_price;

    @Column
    private String uom;

    @Column(name = "item_id")
    private Long item_id;

    @Column
    private Long item_type;

    @Column
    private String item_Name;

    @Column
    private Long po_id;

    @Column
    private String po_number;

    @Column
    private String inspection_status;

    @Column(updatable = false)
    @Temporal(TemporalType.DATE)
    private Date check_lorry_serial_date;


    @Column(name = "ho_item_rec_id", updatable = false)
    private Long hoItemRecId;

    @Column(name = "po_item_id", updatable = false)
    private Long poItemId;
    @Column
    private String color;

    @Column(name = "unit_cost", updatable = false)
    private String unitCost;

    @Column(name = "unit_rate")
    private String unitRate;

    //Others Needed
    @Column(name = "pending_qty")
    private Double pendingQty;

    @Column(name = "freight_charges")
    private Double freightCharges;

    @Column(name = "packing_charges")
    private Double packingCharges;


    public BillInwardItemReceived(BillInwardItemReceivedDto billInwardProduct) {
        this.id = billInwardProduct.getId();
        this.inwardId = billInwardProduct.getInwardId();
        this.description = billInwardProduct.getDescription();
        this.packageType = billInwardProduct.getPackageType();
        this.packageQuantity = billInwardProduct.getPackageQuantity();
        this.receivedQuantity = Double.valueOf(billInwardProduct.getReceivedQuantity());
        this.batchId = billInwardProduct.getBatchId();
        this.batchNo = billInwardProduct.getBatchNo();
        this.lot = billInwardProduct.getLot();
        this.cgst = billInwardProduct.getCgst();
        this.sgst = billInwardProduct.getSgst();
        this.igst = billInwardProduct.getIgst();
        this.uom = billInwardProduct.getUom();
        this.totalwtaxreceive = billInwardProduct.getTotalwtaxreceive();
        this.subtotalreceive = billInwardProduct.getSubtotalreceive();
        this.final_order_price = billInwardProduct.getFinal_order_price();
        this.item_id = billInwardProduct.getItemId();
        this.item_Name = billInwardProduct.getItemName();
        this.item_type = billInwardProduct.getItemType();
        this.po_id = billInwardProduct.getPoId();
        this.po_number = billInwardProduct.getPoNumber();
        this.hoItemRecId = billInwardProduct.getHoItemRecId();
        this.conv_package_quantity = billInwardProduct.getConvPackageQuantity();
        this.color = billInwardProduct.getColor();
        this.unitCost = billInwardProduct.getUnitCost();
        this.unitRate = billInwardProduct.getUnitRate();
        this.freightCharges=billInwardProduct.getFreightCharges();
        this.packingCharges=billInwardProduct.getPackingCharges();
        this.pendingQty=billInwardProduct.getPendingQty();
    }

    public BillInwardItemReceived(GRNProducts billInwardProduct) {

        this.inwardId = billInwardProduct.getInwardId();
        this.description = billInwardProduct.getDescription();
        this.packageType = billInwardProduct.getPackageType();
        this.packageQuantity = billInwardProduct.getPackageQuantity();
        this.receivedQuantity = Double.valueOf(billInwardProduct.getReceivedQuantity());
        this.lot = billInwardProduct.getLot();
        this.uom = billInwardProduct.getUom();
        this.totalwtaxreceive = billInwardProduct.getTotalwtaxreceive();
        this.subtotalreceive = billInwardProduct.getSubtotalreceive();
        this.final_order_price = billInwardProduct.getFinal_order_price();
        this.item_id = billInwardProduct.getItemId();
        this.item_Name = billInwardProduct.getItemName();
        this.item_type = billInwardProduct.getItemType();
        this.po_id = billInwardProduct.getPoId();
        this.hoItemRecId = billInwardProduct.getHoItemRecId();
        this.poItemId = billInwardProduct.getPoItemId();
        this.color = billInwardProduct.getColor();
        this.unitCost = billInwardProduct.getUnitCost();
        this.unitRate = billInwardProduct.getUnitRate();
    }
}
