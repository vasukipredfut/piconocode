package com.aria.model;

import com.aria.dto.SalesReturnBinDetailsDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "sales_return_bin_details")
public class SalesReturnBinDetails {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "return_product_Id")
    private Long returnProductId;

    @Column(name = "item_id")
    private Long itemId;

    @Column(name = "item_name")
    private String itemName;

    @Column(name = "bin_no")
    private Long binNo;

    @Column(name = "allocated_qty")
    private String allocatedQty;

    @Column(name = "accounting_type")
    private String accountingType;


    public SalesReturnBinDetails(SalesReturnBinDetailsDto returnBinDetails) {
        this.id = returnBinDetails.getId();
        this.returnProductId = returnBinDetails.getReturnProductId();
        this.itemId = returnBinDetails.getItemId();
        this.itemName = returnBinDetails.getItemName();
        this.binNo = returnBinDetails.getBinNo();
        this.allocatedQty = returnBinDetails.getAllocatedQty();
    }
}
