package com.aria.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.aria.dto.SalesCeilingStockLimitDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "salesceilingstocklimit")
public class SalesCeilingStockLimit {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "branchid")
	private Long branchId;

	@Column(name = "ceilinglimit")
	private Long ceilingLimit;

	@Column(name = "status")
	private String status;

	@Column(name = "sdate")
	private LocalDate createdDate;

	@Column(name = "capacity")
	private String capacity;

	@Column(name = "productgroup")
	private Long productGroup;

	@OneToOne
	@JoinColumn(name = "updatedby", referencedColumnName = "id")
	private User updatedBy;

	@Column(name = "ip")
	private String ip;

	public SalesCeilingStockLimit(SalesCeilingStockLimitDto dto) {
		this.id = dto.getId();
		this.branchId = dto.getPlantId();
		this.productGroup = dto.getProductGroupId();
		this.ceilingLimit = dto.getCeilingLimit();
		this.capacity = dto.getCapacity();
		this.status = "A";
	}

}
