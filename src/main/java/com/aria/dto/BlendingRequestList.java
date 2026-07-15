package com.aria.dto;

public interface BlendingRequestList {
   
	Long getProductId();

    Long getIssueBranch();

    Long getIssueDeptId();

    Double getQty();
    
    Double getLot();
    Double getMainLot();

    Long getBinNo();
    String getCasgroup();
    Long getBlendingStockId();

    String getBatchNo();


    Long getGodownId();
    String getGodownName();


}
