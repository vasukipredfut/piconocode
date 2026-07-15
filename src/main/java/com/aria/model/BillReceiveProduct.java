package com.aria.model;


import com.aria.dto.BillExchangeProductDto;
import com.aria.dto.BillForwardProductDto;
import com.aria.dto.BillReceiveProductDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@NoArgsConstructor
@Table(name = "bill_receive_product")
@Getter
@Setter
public class BillReceiveProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long receiveId;

    @OneToOne
    @JoinColumn(name = "product_master_id", referencedColumnName = "id")
    public ProductMaster product;

    @Column(name = "batchno")
    private String batchNo;

    @Column
    private String lot;

    @Column
    private String lorrySerialNo;

    @Column
    private String quantity;

    public BillReceiveProduct(BillForwardProductDto billInwardProduct) {
        this.id = billInwardProduct.getId();
      
        this.batchNo = billInwardProduct.getBatchNo();
        this.lot = billInwardProduct.getLot();
        //this.lorrySerialNo = billInwardProduct.getLorrySerialNo();
        this.quantity = billInwardProduct.getQuantity();
    }

    public BillReceiveProduct(BillReceiveProductDto billInwardProduct) {
        this.id = billInwardProduct.getId();
        if (billInwardProduct.getProductId() != null) {
            ProductMaster productMaster = new ProductMaster();
            productMaster.setId(billInwardProduct.getProductId());
            this.product = productMaster;
        }
        this.batchNo = billInwardProduct.getBatchNo();
        this.lot = billInwardProduct.getLot();
        this.lorrySerialNo = billInwardProduct.getLorrySerialNo();
        this.quantity = billInwardProduct.getQuantity();
    }

    public BillReceiveProduct(BillExchangeProductDto billInwardProduct) {
        this.id = billInwardProduct.getId();
        if (billInwardProduct.getProductId() != null) {
            ProductMaster productMaster = new ProductMaster();
            productMaster.setId(billInwardProduct.getProductId());
            this.product = productMaster;
        }
        this.batchNo = billInwardProduct.getBatchNo();
        this.lot = billInwardProduct.getLot();
        //this.lorrySerialNo = billInwardProduct.getLorrySerialNo();
        this.quantity = billInwardProduct.getQuantity();
    }
}
