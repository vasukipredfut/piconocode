package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GRNProducts {

    private Long inwardId;
    private Long poId;
    private Long poItemId;
    private Long hoItemRecId;

    public Long itemId;

    public String itemName;

    public Long itemType;
    private String color;
    private String uom;
    private String description;
    private String unitCost;
    private Double final_order_price;
    private String lot;
    private String packageQuantity;
    private String packageType;
    private String receivedQuantity;
    private Double totalwtaxreceive;
    private Double subtotalreceive;
    private String unitRate;

}
