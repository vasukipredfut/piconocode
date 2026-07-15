package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class SendExternalItemDto {

    private Long materialFlowSendExternalItemsId;
    private Long sendExternalRefId;
    private Long sendExternalItemId;
    private String sendExternalItemName;
    private Double sendExternalItemQuantity;
    private Double sendExternalItemCost;
    private String sendExternalItemUnit;
    private Double sendExternalItemLot;
    private String sendExternalItemHsncode;
    private String packingType;
    private Long packingQuantity;
    private String materialReturn;
}
