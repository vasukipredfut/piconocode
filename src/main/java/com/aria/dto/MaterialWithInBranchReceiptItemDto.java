package com.aria.dto;

import com.aria.model.MaterialWithInBranchReceiptItem;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
@NoArgsConstructor
public class MaterialWithInBranchReceiptItemDto {

    private Long id;

    private Long withinId;

    private String itemName;

    private Long itemId;

    private String quantity;

    private String packageType;

    private String packageQuantity;



    public MaterialWithInBranchReceiptItemDto(MaterialWithInBranchReceiptItem withInBranchReceiptItem) {
        this.id = withInBranchReceiptItem.getId();
        this.withinId = withInBranchReceiptItem.getWithinId();
        this.itemName = withInBranchReceiptItem.getItemName();
        this.itemId = withInBranchReceiptItem.getItemId();
        this.quantity = withInBranchReceiptItem.getQuantity();
        this.packageType = withInBranchReceiptItem.getPackageType();
        this.packageQuantity = withInBranchReceiptItem.getPackageQuantity();
    }
}
