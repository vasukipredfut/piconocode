package com.aria.model;

import com.aria.dto.OutputProductsDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "outputProducts ")
public class OutputProducts {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private String productname;

	@Column
	private String godown;

	@Column
	private String binno;

	@Column
	private String batchno;

	@Column
	private String outputquantity;

	@Column
	private String outputlot;

	@Column
	private String inventorytype;

	@Column
	private String totalinputquantity;

	@Column
	private String totaloutputquantity;

	@Column
	private int active;

	@Column(name = "created_by", updatable = false)
	private Long createdBy;

	@Column(name = "updated_by")
	private Long updatedBy;

	@Column(updatable = false)
	private LocalDateTime created_date;

	@Column
	private LocalDateTime updated_date;

	public OutputProducts(OutputProductsDto outputProductsDetails) {
		this.id = outputProductsDetails.getId();
		this.productname = outputProductsDetails.getProductname();
		this.godown = outputProductsDetails.getGodown();
		this.binno = outputProductsDetails.getBinno();
		this.batchno = outputProductsDetails.getBatchno();
		this.outputquantity = outputProductsDetails.getOutputquantity();
		this.outputlot = outputProductsDetails.getOutputlot();
		this.inventorytype = outputProductsDetails.getInventorytype();
		this.totalinputquantity = outputProductsDetails.getTotalinputquantity();
		this.totaloutputquantity = outputProductsDetails.getTotaloutputquantity();
		this.active = outputProductsDetails.getActive();
		this.createdBy = outputProductsDetails.getCreatedBy();
		this.updatedBy = outputProductsDetails.getUpdatedBy();
		this.created_date = outputProductsDetails.getCreated_date();
		this.updated_date = outputProductsDetails.getUpdated_date();

	}
}
