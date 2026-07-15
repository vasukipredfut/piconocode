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
@Table(name = "excess_stored_bin")
public class ExcessStoredBinDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "excess_product_id")
    private Long excessProductId;

    @Column(name = "item_id")
    private Long itemId;

    @Column(name = "item_name")
    private String itemName;

    @Column(name = "bin_no")
    private Long binNo;

    @Column(name = "batch_no")
    private String batchNo;

    @Column(name = "current_stock")
    private Double currentStock;

    @Column(name = "excess_or_shortage")
    private Double excessOrShortage;


    public ExcessStoredBinDetails(ExcessStoredBinDetailsDto binDetails) {
        this.id = binDetails.getId();
        this.excessProductId = binDetails.getExcessOrReturnProductId();
        this.itemId = binDetails.getItemId();
        this.itemName = binDetails.getItemName();
        this.binNo = binDetails.getBinNo();
        this.batchNo = binDetails.getBatchNo();
        this.currentStock = binDetails.getCurrentStock();
        this.excessOrShortage = binDetails.getExcessOrShortage();
    }
}
