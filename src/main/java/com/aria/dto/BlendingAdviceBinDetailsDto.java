package com.aria.dto;

import com.aria.model.BlendingAdviceBinDetails;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



@Setter
@Getter
@NoArgsConstructor
public class BlendingAdviceBinDetailsDto {


    private Long id;

    private String type;
    private String batchNo;

    private Long binNo;

    private Double quantity;
    private Double lot;
    private Double mainlot;

    private Long blendingAdviceStockId;
    private Long branchId;
    private Long deptId;
    private Long productId;

    protected String avgFFA;
    protected String binId;
    protected Double binStockFFA;
    protected String branchName;
    protected String seedMoisture;
    protected String inwardDate;
    protected String category;
    protected String seedColour;
    protected String state;
    protected String location;



    public BlendingAdviceBinDetailsDto(BlendingAdviceBinDetails binDetailsDto) {
        this.id = binDetailsDto.getId();
        this.branchId = binDetailsDto.getBranchId();
        this.blendingAdviceStockId = binDetailsDto.getBlendingAdviceStockId();
        this.type = binDetailsDto.getType();
        this.batchNo = binDetailsDto.getBatchNo();
        this.binNo = binDetailsDto.getBinNo();
        this.quantity = binDetailsDto.getQuantity();
        this.productId = binDetailsDto.getProductId();
        this.lot = binDetailsDto.getLot();
    }


    public BlendingAdviceBinDetailsDto(BlendingBinList binDetailsDto) {
        this.id = binDetailsDto.getId();
        this.branchId = binDetailsDto.getBranch_id();
        this.blendingAdviceStockId = binDetailsDto.getBlending_advice_stock_id();
        this.type = binDetailsDto.getType();
        this.batchNo = binDetailsDto.getBatch_no();
        this.binNo = binDetailsDto.getBin_no();
        this.quantity = binDetailsDto.getQuantity();
        this.productId = binDetailsDto.getProduct_id();
    }
}
