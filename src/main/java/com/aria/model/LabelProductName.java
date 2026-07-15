package com.aria.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.aria.dto.LabelProductNameDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "labelproductname")
public class LabelProductName {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "salesproductgroup")
	private Long productGroupId;

	@Column(name = "productname")
	private String productName;

	@Column(name = "type")
	private String type;

	@Column(name = "capacity")
	private String capacity;

	@Column(name = "displayorder")
	private Long displayOrder;

	@Column(name = "ip")
	private String ip;

	@OneToOne
	@JoinColumn(name = "createdby", referencedColumnName = "id")
	private User createdBy;

	@Column(name = "flag")
	private Long flag;

	public LabelProductName(LabelProductNameDto dto) {
		this.id = dto.getId();
		this.productGroupId = dto.getProductGroupId();
		this.productName = dto.getProductName();
		this.type = dto.getType();
		this.capacity = dto.getCapacity();
		this.displayOrder = dto.getDisplayOrder();
		this.ip = dto.getIp();
		this.flag = dto.getFlag();
	}

}
