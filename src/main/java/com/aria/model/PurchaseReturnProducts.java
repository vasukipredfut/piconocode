package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

import com.aria.dto.PurchaseReturnProductsDto;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "purchase_return_products")
public class PurchaseReturnProducts {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "purchase_Return_Id")
	private Long purchaseReturnId;

	@OneToOne
	@JoinColumn(name = "product_master_id", referencedColumnName = "id")
	public ProductMaster product;

	/*@Column(name = "product_Code")
	private String productCode;*/

	@Column(name = "product_Type")
	private String productType;

	@Column(name = "batch_No")
	private String batchNo;

	@Column
	private String bin;

	@Column
	private String lot;

	@Column(name = "current_Stock")
	private String currentStock;

	@Column(name = "return_Quantity")
	private String returnQuantity;

	@Column
	private String reason;
	
	 @Column(name = "is_select", columnDefinition = "char(1) default 'N'")
	 private String isSelect;

	@OneToMany(mappedBy = "returnProductId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<PurchaseReturnStoredBinDetails> binDetails;

	public PurchaseReturnProducts(PurchaseReturnProductsDto purchaseReturnProducts) {
		this.id = purchaseReturnProducts.getId();
		this.purchaseReturnId = purchaseReturnProducts.getPurchaseReturnId();
		if (purchaseReturnProducts.getProductId() != null) {
			ProductMaster productMaster = new ProductMaster();
			productMaster.setId(purchaseReturnProducts.getProductId());
			this.product = productMaster;
		}
		//this.productCode = purchaseReturnProducts.getProductCode();
		this.productType = purchaseReturnProducts.getProductType();
		this.batchNo = purchaseReturnProducts.getBatchNo();
		this.lot = purchaseReturnProducts.getLot();
		this.bin = purchaseReturnProducts.getBin();
		this.currentStock = purchaseReturnProducts.getCurrentStock();
		this.returnQuantity = purchaseReturnProducts.getReturnQuantity();
		this.reason = purchaseReturnProducts.getReason();
		this.isSelect  = "N";

	}
}
