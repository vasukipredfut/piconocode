package com.aria.model;

import com.aria.dto.WithInBranchReceiveItemDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.List;



@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "within_branch_receive_items")
public class WithInBranchReceiveItems {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

	@Column(name = "within_id")
    private Long withinId;

	@Column(name = "item_Name")
    private String itemName;

	@Column(name = "issued_From_Bin")
	private String issuedFromBin;

	@Column(name = "issued_From_Batch")
	private String issuedFromBatch;

	@Column(name = "item_Id")
    private Long itemId;

    @Column
    private String quantity;

	@Column(name = "package_Type")
    private String packageType;

	@Column(name = "package_Quantity")
    private String packageQuantity;

	@Column(name = "issue_Branch_Id")
	private Long issueBranchId;

	@Column(name = "issue_Dept_Id")
	private Long issueDeptId;

	@Column(name = "receive_Branch_Id")
	private Long receiveBranchId;

	@Column(name = "receive_Dept_Id")
	private Long receiveDeptId;

	@Column(name = "select_Lot")
	private String selectLot;
	
	@OneToMany(mappedBy = "withinItemId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<MaterialWithinBranchReceiptStoredBinDetails> binDetails;

	@Column(name = "sal_status")
	private String salStatus;

	@Column(name = "bin_stock_id", updatable = false)
	private Long binStockId;


    public WithInBranchReceiveItems(IssueItems issueItems) {
        this.itemName = issueItems.getItemName();
        this.itemId = issueItems.getItemId();
        this.quantity = issueItems.getAllocatedQuantity();
		this.issuedFromBatch = issueItems.getIssuedFromBatch();
		this.issuedFromBin = issueItems.getIssuedFromBin();
		this.issueBranchId = issueItems.getBranchId();
		this.issueDeptId = issueItems.getDeptId();
		this.selectLot = issueItems.getSelectLot();
		this.binStockId = issueItems.getReceiveBinStockId();
    }

	public WithInBranchReceiveItems(WithInBranchReceiveItemDto issueItems) {
		this.id = issueItems.getId();
		this.itemName = issueItems.getItemName();
        this.itemId = issueItems.getItemId();
        this.quantity = issueItems.getQuantity();
		this.issuedFromBatch = issueItems.getIssuedFromBatch();
		this.issuedFromBin = issueItems.getIssuedFromBin();
		this.issueBranchId = issueItems.getBranchId();
		this.issueDeptId = issueItems.getDeptId();
		this.receiveBranchId = issueItems.getBranchId();
		this.receiveDeptId = issueItems.getBranchId();
		this.selectLot = issueItems.getSelectLot();
		this.binStockId = issueItems.getBinStockId();
	}



}
