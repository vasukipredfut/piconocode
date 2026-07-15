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

import com.aria.dto.SalesProductFilmDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "salesproductfilm")
public class SalesProductFilm {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name")
	private String name;

	@Column(name = "refid")
	private Long productId;

	@Column(name = "productgroupid")
	private Long productGroupID;

	@Column
	private Long capacity;

	@OneToOne
	@JoinColumn(name = "branchid", referencedColumnName = "id")
	private SalesProductBranchesEntity branchID;

	@Column(name = "productnameid")
	private Long finalProductID;

	@OneToOne
	@JoinColumn(name = "updatedby", referencedColumnName = "id")
	private User updatedBy;

	@Column(name = "filmweightinkg")
	private Double filmweightinkg = 0.0;

	@Column(name = "ip")
	private String ip = "";

	@Column(name = "qty")
	private String qty = "";

	@Column(name = "created_date", updatable = false)
	private LocalDate createdDate;

	public SalesProductFilm(SalesProductFilmDto dto) {
		this.id = dto.getId();
		this.name = dto.getName();
		this.productId = dto.getProductId();
		this.productGroupID = dto.getProductGroupID();
		this.capacity = dto.getCapacity();
		if (dto.getBranchID() != null) {
			SalesProductBranchesEntity branch = new SalesProductBranchesEntity();
			branch.setId(dto.getBranchID());
			this.branchID = branch;
		}
		this.finalProductID = dto.getFinalProductNameID();
		this.createdDate = dto.getCreatedDate();

	}

}
