package com.aria.model;

import com.aria.dto.BillForwardProductDto;
import com.aria.dto.BillInwardItemReceivedDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity

@NoArgsConstructor
@Table(name = "bill_forward_product")
@Getter
@Setter
public class BillForwardProduct {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "forward_id")
    private Long forwardId;

    @Column(name = "batchno")
    private String batchNo;

    @Column(name = "batchid")
    private Long batchId;

    @Column
    private String lot;

    //@Column
    //private String lorrySerialNo;

    @Column
    private String quantity;

    @Column(name = "item_id")
    private Long itemId;

    @Column(name = "item_type")
    private Long itemType;
    
    @Column(name = "item_name")
    private String itemName;

    @Column
    private Long po_id;

    @Column
    private String po_number;

    @Column
    private Long bill_ref_id;

    public BillForwardProduct(BillInwardItemReceivedDto billInwardProduct) {
        this.id = billInwardProduct.getId();
        this.itemId=billInwardProduct.getItemId();
        this.itemName=billInwardProduct.getItemName();
        this.itemType=billInwardProduct.getItemType();
        this.batchNo = billInwardProduct.getBatchNo();
        this.batchId = billInwardProduct.getBatchId();
        this.lot = billInwardProduct.getLot();
        //this.lorrySerialNo = billInwardProduct.getLorrySerialNo();
        this.quantity = billInwardProduct.getReceivedQuantity();
        this.po_id=billInwardProduct.getPoId();
        this.po_number=billInwardProduct.getPoNumber();
        this.bill_ref_id=billInwardProduct.getInwardId();
    }

    public BillForwardProduct(BillForwardProductDto billInwardProduct) {
        this.id = billInwardProduct.getId();
        this.itemId=billInwardProduct.getItemId();
        this.itemName=billInwardProduct.getItemName();
        this.itemType=billInwardProduct.getItemType();
        this.batchNo = billInwardProduct.getBatchNo();
        this.batchId = billInwardProduct.getBatchId();
        this.lot = billInwardProduct.getLot();
        //this.lorrySerialNo = billInwardProduct.getLorrySerialNo();
        this.quantity = billInwardProduct.getQuantity();
        this.po_id=billInwardProduct.getPoId();
        this.po_number=billInwardProduct.getPoNumber();
        this.bill_ref_id=billInwardProduct.getBillRefId();
    }
}
