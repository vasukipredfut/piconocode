package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class HOBinStock {


    private Long itemId;
    private String itemName;
    private Long binName;
    private String binId;
    private double lot;
    private double stkQty;
    private Long branchId;
    private String branchName;
    private String productionBatchNo;
    private Long productionBatchId;



    public HOBinStock(HOBinStockList binStock) {
        this.itemId = binStock.getItemId();
        this.itemName = binStock.getItemName();
        this.binName = binStock.getBinName();
        this.binId = binStock.getBinId();
        this.lot = binStock.getLot();
        this.stkQty = binStock.getStkQty();
        this.branchId = binStock.getBranchId();
        this.branchName = binStock.getBranchName();
        this.productionBatchNo = binStock.getProductionBatchNo();
        this.productionBatchId = binStock.getProductionBatchId();
    }
}
