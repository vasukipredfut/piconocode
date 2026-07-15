package com.aria.model;

import java.time.LocalDate;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.aria.dto.SalesProductLabelDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "labelproduct")
public class SalesProductLabel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "productname")
	private String name;

	@Column(name = "refid")
	private Long productId;

	@Column(name = "productgroupid")
	private Long productGroupId;

	@Column(name = "capacity")
	private Long capacity;

	@Column(name = "branchid")
	private Long branchId;

	@Column(name = "productnameid")
	private Long finalProductId;

	@Column(name = "labelweightinkg")
	private Double labelweightinkg;

	@Column(name = "labelweightinkg1")
	private Double labelWeightinKg1;

	@OneToOne
	@JoinColumn(name = "updatedby", referencedColumnName = "id")
	private User updatedBy;

	@Column(name = "ip")
	private String ip;

	@Column(name = "locationid")
	private Long locationId;

	@Column(name = "displocation")
	private String dispLocation;

	@Column(name = "lableprice")
	private String price;

	@Column(name = "displocationorder")
	private Long dispLocationOrder;

	@Column(name = "dislabelname")
	private String disLabelName;

	@Column(name = "createddate", updatable = false)
	private Date createdDate;

	public SalesProductLabel(SalesProductLabelDto dto) {
		this.id = dto.getId();
		this.name = dto.getName();
		this.productId = dto.getProductId();
		this.productGroupId = dto.getProductGroupId();
		this.capacity = dto.getCapacity();
		this.branchId = dto.getBranchId();
		this.price = dto.getPrice();
		this.finalProductId = dto.getFinalProductId();
		this.labelweightinkg = dto.getLabelweightinkg();
		this.labelWeightinKg1 = dto.getLabelWeightinKg1();
		this.locationId = dto.getLocationId();
	}

}
