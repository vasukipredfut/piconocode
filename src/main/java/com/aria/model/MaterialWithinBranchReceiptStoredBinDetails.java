package com.aria.model;


import com.aria.dto.WithInBranchRecieveBinDetailsDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "within_branch_receipt_bin")
public class MaterialWithinBranchReceiptStoredBinDetails {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_id")
    private Long itemId;

    @Column(name = "within_item_id")
    private long withinItemId;

    @Column(name = "bin_no")
    private String binNo;

    @Column(name = "bin_id")
    private Long binId;

    @Column(name = "allotted_quantity")
    private Double allottedQuantity;


    public MaterialWithinBranchReceiptStoredBinDetails(WithInBranchRecieveBinDetailsDto dpEntryStoredBinDetails) {
        this.id = dpEntryStoredBinDetails.getId();
        this.itemId = dpEntryStoredBinDetails.getItemId();
        this.binNo = dpEntryStoredBinDetails.getBinNo();
        this.binId = dpEntryStoredBinDetails.getBinId();
        this.allottedQuantity = dpEntryStoredBinDetails.getAllottedQuantity();
    }
}
