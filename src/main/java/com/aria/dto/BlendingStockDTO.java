package com.aria.dto;

import com.aria.model.BlendingStock;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@NoArgsConstructor
@Getter
@Setter
public class BlendingStockDTO {

    protected Double qty;
    protected Long branch;
    protected String branchName;
    protected String lockType;
    protected String category;
    protected String batchNo;
    protected Long batchId;
    protected String binNo;
    protected Long binName;
    protected String inwardDate;
    protected String location;
    protected String campName;
    protected String seedcolor;
    protected String seedMoisture;
    protected Double oilFFA;
    protected Double lot;
    protected Double mainlot;
    protected String avgFFA;
    protected Boolean blendingLock;
    protected Boolean preprocess;

    protected Boolean lock;

    protected Long productId;
    protected String godownType;
    protected String siloBin;

    public BlendingStockDTO(BlendingStock blendingStock) {
        DateUtil dateUtil = new DateUtil();
        this.qty = blendingStock.getQty();
        this.branch = blendingStock.getBranch();
        if (blendingStock.getBranchName() != null) {
            this.branchName = blendingStock.getBranchName();
        } else {
            this.branchName = "";
        }
        this.lockType = blendingStock.getLockType();
        if (blendingStock.getCategory() != null) {
            this.category = blendingStock.getCategory();
        } else {
            this.category = "";
        }
        this.batchNo = blendingStock.getBatchNo();
        this.batchId = blendingStock.getBatchId();
        this.binNo = blendingStock.getBinNo();
        this.binName = blendingStock.getBinName();
        this.location = blendingStock.getLocation();
        this.campName = blendingStock.getCampName();
        this.seedcolor = blendingStock.getSeedcolor();
        if (blendingStock.getInwardDate() != null) {
            this.inwardDate = dateUtil.getDateToStringDDMMYYYY(blendingStock.getInwardDate());
        }
        this.oilFFA = blendingStock.getFFA();
        this.lot = blendingStock.getLot();
        this.seedMoisture = blendingStock.getSeedMoisture();
        this.productId = blendingStock.getProductId();
        if (blendingStock.getSilo() != null && blendingStock.getSilo() == 1) {
            siloBin = "Y";
        } else {
            siloBin = "N";
        }
        if (blendingStock.getBlendingLock() != null) {
            this.blendingLock = blendingStock.getBlendingLock();
        } else {
            this.blendingLock = false;
        }
        this.preprocess = blendingStock.getPreprocess();
        if( blendingStock.getGodownType() != null && !blendingStock.getGodownType().isEmpty() && blendingStock.getGodownType().equalsIgnoreCase("Y")) {
            this.godownType = "ExternalGodown";
        }

    }
}
