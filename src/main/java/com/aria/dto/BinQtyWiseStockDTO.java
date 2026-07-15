package com.aria.dto;

import com.aria.utils.DateUtil;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BinQtyWiseStockDTO {


    private Long binNo;
    private String binId;
    private Double qty;
    private Double cost;
    private Double avgCost;
    private Double lot;
    private Long productId;
    private String productName;
    private String productCategory;
    private Long branchId;
    private String branchName;
    private String lorryDate;
    private String camp;
    private String lorrySerialNo;
    private String place;
    private String state;
    private Long batchId;
    private String batchNo;
    private String lockType;
    private String lockStatus;
    private String mrnStatus;

    public BinQtyWiseStockDTO(BinQtyWiseStockList binQtyWiseStockList ) {
        DateUtil dateUtil = new DateUtil();
        this.binNo = binQtyWiseStockList.getBinNo();
        this.binId = binQtyWiseStockList .getBinId();
        this.qty = binQtyWiseStockList .getQty();
        this.cost = binQtyWiseStockList .getCost();
        this.avgCost = binQtyWiseStockList .getAvgCost();
        this.productId = binQtyWiseStockList .getProductId();
        this.productName = binQtyWiseStockList .getProductName();
        this.productCategory = binQtyWiseStockList .getProductCategory();
        this.lot = binQtyWiseStockList .getLot();
        this.branchId = binQtyWiseStockList .getBranchId();
        this.branchName = binQtyWiseStockList .getBranchName();
        this.lorryDate = dateUtil.getDateTOStringWk(binQtyWiseStockList .getLorryDate());
        this.camp = binQtyWiseStockList.getCamp();
        this.lorrySerialNo = binQtyWiseStockList.getLorrySerialNo();
        this.place = binQtyWiseStockList.getPlace();
        this.state = binQtyWiseStockList.getState();
        this.batchId = binQtyWiseStockList.getBatchId();
        this.batchNo = binQtyWiseStockList.getBatchNo();
        this.lockType = binQtyWiseStockList.getLockType();
        this.lockStatus = binQtyWiseStockList.getLockStatus();
        this.mrnStatus = binQtyWiseStockList.getMrnStatus();
    }
}
