package com.aria.dto;

import com.aria.model.BlendingAdviceBinDetails;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class LinkSjListDto {

    private  Long productId;
    private  String productName;
    private  String category;
    private  String bomIntakeType;
    private  String batchProductType;
    private  String quantity;
    private  String cost;
    private  String activity;
    private  Double fixQty;
    private  String pridUnit;
    private  Boolean status;
    private  Boolean applicableForJcal;
    private  String bottleWeight;

    List<BlendingAdviceBinDetailsDto> binDetailsList;

    public LinkSjListDto(LinkSjList linkSjList) {
        this.productId = linkSjList.getProductId();
        this.productName = linkSjList.getProductName();
        this.category = linkSjList.getCategory();
        this.bomIntakeType = linkSjList.getBomTakeType();
        this.batchProductType = linkSjList.getBatchProductType();
        this.quantity = linkSjList.getQuantity();
        this.fixQty = linkSjList.getFixQty();
        this.pridUnit = linkSjList.getPridUnit();
        this.status = linkSjList.getStatus();
        this.cost = linkSjList.getCost();
        this.applicableForJcal = linkSjList.getApplicableForJcal();
        this.bottleWeight = linkSjList.getBottleWeight();
        //this.calculationType = linkSjList.getCalculationType();
    }
}
