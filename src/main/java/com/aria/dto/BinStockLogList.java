package com.aria.dto;

public interface BinStockLogList {


    String getBinid();

    Long getBinname();

    Long getBatchid();

    String getBatchno();

    String getType();

    Double getStkqty();

    Double getLot();
    Double getCost();
    Double getAvgcost();

    Long getGodownId();

    String getGodownName();
    String getProductName();
    Long getProductid();


}
