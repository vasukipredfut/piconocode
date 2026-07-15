package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class IssueItemsValuesDto {

    private String itemName;

    private Long itemId;

    private String selectLot;
    private String requestedQuantity;

    private String allocatedQuantity;

    private String pendingQuantity;

    public IssueItemsValuesDto(IssueItemsValues issueItemsValues ) {
        this.itemName = issueItemsValues.getItem_name();
        this.itemId = issueItemsValues.getItem_id();
        this.selectLot = issueItemsValues.getSelect_lot();
        this.requestedQuantity = issueItemsValues.getRequested_quantity();
        this.allocatedQuantity = issueItemsValues.getAllocated_quantity();
        this.pendingQuantity = issueItemsValues.getPending_quantity();
    }
}
