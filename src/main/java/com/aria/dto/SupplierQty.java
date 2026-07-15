package com.aria.dto;

public interface SupplierQty {

    Long getPoId();

    String getPoNo();

    String getPoDate();

    Long getItemId();

    String getItemName();

    String getLot();

    Double getOrderQty();

    Double getPendingQty();

    Double getUnitRate();

    String getTypeFlag();

}
