package com.aria.model;

import com.aria.dto.BillExchangeProductDto;
import com.aria.dto.BillInwardItemReceivedDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@NoArgsConstructor
@Table(name = "bill_exchange_product")
@Getter
@Setter
public class BillExchangeProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "exchange_id")
    private Long exchangeId;

    @Column(name = "batchno")
    private String batchNo;

    @Column(name = "batchid")
    private Long batchId;

    @Column(name = "item_id")
    private Long itemId;
    
    @Column(name = "item_type")
    private Long itemType;
    
    @Column(name = "item_name")
    private String itemName;
    
    @Column
    private String lot;


   /* @Column
    private String lorrySerialNo;*/


    @Column
    private String quantity;


    @Column
    private Long po_id;

    @Column
    private String po_number;


    public BillExchangeProduct(BillInwardItemReceivedDto billInwardProduct) {
        this.id = billInwardProduct.getId();
        this.itemId=billInwardProduct.getItemId();
        this.itemType=billInwardProduct.getItemType();
        this.itemName=billInwardProduct.getItemName();
    //    this.product = billInwardProduct.getProductId();
        this.batchNo = billInwardProduct.getBatchNo();
        this.batchId = billInwardProduct.getBatchId();
        this.lot = billInwardProduct.getLot();
        //this.lorrySerialNo = billInwardProduct.getLorrySerialNo();
        this.quantity = billInwardProduct.getReceivedQuantity();
        this.po_id = billInwardProduct.getPoId();
        this.po_number = billInwardProduct.getPoNumber();
    }

    public BillExchangeProduct(BillExchangeProductDto billInwardProduct) {
        this.id = billInwardProduct.getId();
        this.exchangeId = billInwardProduct.getExchangeId();
        this.itemId=billInwardProduct.getItemId();
        this.itemName=billInwardProduct.getItemName();
        this.itemType=billInwardProduct.getItemType();
        this.batchNo = billInwardProduct.getBatchNo();
        this.batchId = billInwardProduct.getBatchId();
        this.lot = billInwardProduct.getLot();
       // this.lorrySerialNo = billInwardProduct.getLorrySerialNo();
        this.quantity = billInwardProduct.getQuantity();
        this.po_id = billInwardProduct.getPoId();
        this.po_number = billInwardProduct.getPoNumber();
    }
}
