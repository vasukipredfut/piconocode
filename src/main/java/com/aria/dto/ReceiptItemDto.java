package com.aria.dto;

import com.aria.dao.BinStockRepository;
import com.aria.dao.IssueItemsRepository;
import com.aria.dao.MaterialIssueRepository;
import com.aria.dao.ProductMasterRepository;
import com.aria.model.BinStock;
import com.aria.model.IssueItems;
import com.aria.model.ProductMaster;
import com.aria.model.RequestItem;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
public class ReceiptItemDto {

    private Long id;

    private Long requestId;

    private Long itemId;

    private String itemName;

    private String selectLot;

    private Long issueDepartmentId;

    private String issueDepartment;

    private Long issueBranchId;

    private String issueBranch;

    private String stockQuantity;

    private String stockRequired;

    private int active;

    private Long createdBy;

    private Long updatedBy;

    private Date created_date;

    private Date updated_date;

    private Double amount;

    private Double allocatedQuantity;

    private String hsbn;

    private String unit;

    public ReceiptItemDto(RequestItem requestItem, ProductMasterRepository productMasterRepository, IssueItemsRepository materialIssueRepository, Long issueId, BinStockRepository binStockRepository) {
        this.id = requestItem.getId();
        this.itemId = requestItem.getItemId();

        Optional<ProductMaster> optional = productMasterRepository.findById(this.itemId);
        if (optional.isPresent()) {
            ProductMaster productMaster = optional.get();
            this.hsbn = productMaster.getHsnRefId();
            if (requestItem.getSelectLot().equalsIgnoreCase("1")) {
                this.unit = productMaster.getUnit();
            } else {
                this.unit = productMaster.getAltunit();
            }
        }

        Double amount = 0.0;
        Double allocatedQuantity = 0.0;
        List<IssueItems> issueItems = materialIssueRepository.findAllByIssueItemIdAndItem(issueId, this.itemId);
        for (IssueItems items : issueItems) {
            try {
                allocatedQuantity = allocatedQuantity + Double.valueOf(items.getAllocatedQuantity());
                Long binId = Long.valueOf(items.getIssuedFromBin());
                BinStock binStock = binStockRepository.findByProductBatchAndBinId(items.getItemId(), items.getIssuedFromBatch(), binId);
                if (binStock != null) {
                    amount = amount + (binStock.getAvgCost() * Long.valueOf(items.getAllocatedQuantity()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        this.amount = amount;
        this.allocatedQuantity = allocatedQuantity;
        this.itemName = requestItem.getItemName();
        this.selectLot = requestItem.getSelectLot();
        this.requestId = requestItem.getRequestId();
        if (requestItem.getIssueBranch() != null) {
            this.issueBranchId = requestItem.getIssueBranch().getId();
            this.issueBranch = requestItem.getIssueBranch().getBranchName();
        }
        if (requestItem.getIssueDepartment() != null) {
            this.issueDepartmentId = requestItem.getIssueDepartment().getId();
            this.issueDepartment = requestItem.getIssueDepartment().getName();
        }
        this.stockQuantity = requestItem.getStockQuantity();
        this.stockRequired = requestItem.getStockRequired();
        this.active = requestItem.getActive();
        this.createdBy = requestItem.getCreatedBy();
        this.updatedBy = requestItem.getUpdatedBy();
        this.created_date = requestItem.getCreated_date();
        if (requestItem.getUpdated_date() != null) {
            this.updated_date = requestItem.getUpdated_date();
        } else {
            this.updated_date = requestItem.getCreated_date();
        }
    }
}
