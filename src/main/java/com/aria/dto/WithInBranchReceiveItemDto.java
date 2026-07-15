package com.aria.dto;


import com.aria.model.BinDetails;
import com.aria.model.MaterialWithinBranchReceiptStoredBinDetails;
import com.aria.model.WithInBranchReceiveItems;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;



@Getter
@Setter
@NoArgsConstructor
public class WithInBranchReceiveItemDto {

    private Long id;

    private Long withinId;

    private String itemName;

    private Long itemId;

    private String quantity;

    private String packageType;

    private String packageQuantity;
    
    private String issuedFromBin;
    
	private String issuedFromBatch;
	
	private Long branchId;

	private Long deptId;

    private Long receiveBranchId;

    private Long receiveDeptId;
	
	private String selectLot;
	private Long binStockId;

	private List<WithInBranchRecieveBinDetailsDto> binDetails;

    public WithInBranchReceiveItemDto(WithInBranchReceiveItems withInBranchReceiptItem) {
        this.id = withInBranchReceiptItem.getId();
        this.withinId = withInBranchReceiptItem.getWithinId();
        this.itemId = withInBranchReceiptItem.getItemId();
        this.itemName = withInBranchReceiptItem.getItemName();
        this.quantity = withInBranchReceiptItem.getQuantity();
        this.packageType = withInBranchReceiptItem.getPackageType();
        this.packageQuantity = withInBranchReceiptItem.getPackageQuantity();
        this.issuedFromBin = withInBranchReceiptItem.getIssuedFromBin();
        this.issuedFromBatch = withInBranchReceiptItem.getIssuedFromBatch();
        this.branchId = withInBranchReceiptItem.getIssueBranchId();
        this.deptId = withInBranchReceiptItem.getIssueDeptId();
        this.receiveBranchId = withInBranchReceiptItem.getReceiveBranchId();
        this.receiveDeptId = withInBranchReceiptItem.getReceiveDeptId();
        this.selectLot = withInBranchReceiptItem.getSelectLot();
        this.binStockId = withInBranchReceiptItem.getBinStockId();
    }
}
