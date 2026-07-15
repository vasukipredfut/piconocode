package com.aria.model;

import com.aria.dto.DpEntryStoredBinDetailsDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "direct_purchase_stored_bin")
public class DpEntryStoredBinDetails {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entry_product_id")
    private Long entryProductId;

    @Column(name = "bin_no")
    private String binNo;

    @Column(name = "bin_id")
    private Long binId;

    @Column(name = "allotted_quantity")
    private Double allottedQuantity;

    @Column
    private Long dept;


    public DpEntryStoredBinDetails(DpEntryStoredBinDetailsDto dpEntryStoredBinDetails) {
        this.id = dpEntryStoredBinDetails.getId();
        this.entryProductId = dpEntryStoredBinDetails.getEntryProductId();
        this.binNo = dpEntryStoredBinDetails.getBinNo();
        this.binId = dpEntryStoredBinDetails.getBinId();
        this.allottedQuantity = dpEntryStoredBinDetails.getAllottedQuantity();
        this.dept = dpEntryStoredBinDetails.getDept();
    }
}
