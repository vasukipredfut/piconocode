package com.aria.dto;

public interface BillInwardItemReceives {



    Long getInwardId();

    
    Long getItemId();

    Long getItemType();

    String getItemName();

    String getQty();
    String getLot();

   /* Double getTotalwtaxreceive();

    Double getSubtotalreceive();

    Double getFinal_order_price();

    String getPoNumber();*/

    Long getPoId();

}
