package com.aria.dto;

public interface HOBinStockList {

    Long getItemId();

    String getItemName();

    Long getBinName();

    String getBinId();

    double getLot();

    double getStkQty();

    Long getBranchId();

    String getBranchName();

    String getProductionBatchNo();

    Long getProductionBatchId();
}
