package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "within_branch_receipt_items")
public class  MaterialWithInBranchReceiptItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "within_id")
    private Long withinId;

    @Column(name = "item_name")
    private String itemName;

    @Column(name = "item_id")
    private Long itemId;

    @Column
    private String quantity;

    @Column(name = "package_type")
    private String packageType;

    @Column(name = "package_quantity")
    private String packageQuantity;

    @Column(name = "sal_status")
    private String salStatus;

    public MaterialWithInBranchReceiptItem(IssueItems issueItems) {
        this.itemName = issueItems.getItemName();
        this.itemId = issueItems.getItemId();
        this.quantity = issueItems.getAllocatedQuantity();
    }
}
