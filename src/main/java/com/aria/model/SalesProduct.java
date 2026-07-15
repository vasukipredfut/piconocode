package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.aria.dto.SalesProductDto;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "sales_product")
public class SalesProduct {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private Long productId;
	
	@Column
	private Long oldPrice;
	
	@Column
	private Long newPrice;

	@OneToOne
	@JoinColumn(name = "vendor_id", referencedColumnName = "id")
	private Vendor vendor;
	
	@Column
	private Long assignedQuantity;
	
	@Column
	private String bin;
	
	@Column
	private String batch;
	
	@Column
	private String comparePercentage;

	@Column
	private int active;

	@Column(updatable = false)
	private Long createdBy;

	@Column
	private Long updatedBy;

	@Column(updatable = false)
	private LocalDateTime created_date;

	@Column
	private LocalDateTime updated_date;

	public SalesProduct(SalesProductDto salesProduct) {
		this.id = salesProduct.getId();
		this.productId= salesProduct.getProductId();
		if(salesProduct.getVendorId() != null) {
			Vendor vendor = new Vendor();
			vendor.setId(salesProduct.getVendorId());
			this.vendor = vendor;
		this.oldPrice = salesProduct.getOldPrice();
		this.newPrice = salesProduct.getNewPrice();
		
		this.assignedQuantity = salesProduct.getAssignedQuantity();
		this.bin = salesProduct.getBin();
		this.batch = salesProduct.getBatch();
		this.comparePercentage =salesProduct.getComparePercentage();
		
		this.active = salesProduct.getActive();
		this.createdBy = salesProduct.getCreatedBy();
		this.updatedBy = salesProduct.getUpdatedBy();
		this.created_date = salesProduct.getCreated_date();
		this.updated_date = salesProduct.getUpdated_date();
	}
}
}
