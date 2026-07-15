package com.aria.dto;

import com.aria.model.BillInwardItemReceived;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class HoBillReceiveItem {

    private Long hoItemRecId;
    private Long hoPoId;
    private Double receivedQty;
    private String convFactor;
    private String uom;
    private String lot;

    public HoBillReceiveItem(BillInwardItemReceived itemReceived) {
        this.hoItemRecId = itemReceived.getHoItemRecId();
        this.hoPoId = itemReceived.getPo_id();
        this.receivedQty = itemReceived.getReceivedQuantity();
        this.convFactor = itemReceived.getPackageQuantity();
        this.uom = itemReceived.getPackageType();
        this.lot = itemReceived.getLot();
    }
}
