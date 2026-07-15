package com.aria.model;

import com.aria.dto.BlendingAdviceBinDetailsDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "blending_advice_bin_details")
public class BlendingAdviceBinDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "blending_advice_stock_id")
    private Long blendingAdviceStockId;

    @Column(name = "branch_id")
    private Long branchId;


    @Column(name = "product_id")
    private Long productId;

    @Column
    private String type;

    @Column(name = "bin_no")
    private Long binNo;

    @Column(name = "batch_no")
    private String batchNo;

    @Column
    private Double quantity;

    @Column
    private Double lot;


    @Column
    private Double mainlot;

    public BlendingAdviceBinDetails(BlendingAdviceBinDetailsDto binDetailsDto) {
        this.id = binDetailsDto.getId();
        this.branchId = binDetailsDto.getBranchId();
        this.blendingAdviceStockId = binDetailsDto.getBlendingAdviceStockId();
        this.type = binDetailsDto.getType();
        this.batchNo = binDetailsDto.getBatchNo();
        this.binNo = binDetailsDto.getBinNo();
        this.quantity = binDetailsDto.getQuantity();
        this.productId = binDetailsDto.getProductId();
        this.lot = binDetailsDto.getLot();
        this.mainlot = binDetailsDto.getMainlot();
    }
}
