package com.aria.model;

import com.aria.dto.ExcessStoredBinDetailsDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "purchase_return_stored_bin")
public class PurchaseReturnStoredBinDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "return_Product_Id")
    private Long returnProductId;

    @Column(name = "item_Id")
    private Long itemId;

    @Column(name = "item_Name")
    private String itemName;

    @Column(name = "bin_No")
    private Long binNo;

    @Column(name = "batch_No")
    private String batchNo;

    @Column(name = "current_Stock")
    private Double currentStock;

    @Column(name = "excess_Or_Shortage")
    private Double excessOrShortage;

    public PurchaseReturnStoredBinDetails(ExcessStoredBinDetailsDto binDetails) {
        this.id = binDetails.getId();
        this.returnProductId = binDetails.getExcessOrReturnProductId();
        this.itemId = binDetails.getItemId();
        this.itemName = binDetails.getItemName();
        this.binNo = binDetails.getBinNo();
        this.batchNo = binDetails.getBatchNo();
        this.currentStock = binDetails.getCurrentStock();
        this.excessOrShortage = binDetails.getExcessOrShortage();
    }
}
