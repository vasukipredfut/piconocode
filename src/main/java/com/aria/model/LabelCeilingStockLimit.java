package com.aria.model;

import com.aria.dto.LabelCeilingStockLimitDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "labelceilingstocklimit")
public class LabelCeilingStockLimit {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne
	@JoinColumn(name = "branchid", referencedColumnName = "id")
	private LabelBranches branchId;

	@Column(name = "ceilinglimit")
	private Long ceilingLimit;

	@Column(name = "status")
	private String status;

	@Column(name = "date")
	private LocalDate date;

	@Column(name = "capacity")
	private String capacity;

	@Column(name = "productgroup")
	private Long productGroupId;

	@OneToOne
	@JoinColumn(name = "updatedby", referencedColumnName = "id")
	private User updatedBy;

	@Column(name = "ip")
	private String ip;

	public LabelCeilingStockLimit(LabelCeilingStockLimitDto dto) {
		this.id = dto.getId();
		if (dto.getBranchId() != null) {
			LabelBranches branch = new LabelBranches();
			branch.setId(dto.getBranchId());
			this.branchId = branch;
		}
		this.ceilingLimit = dto.getCeilingLimit();
		this.status = dto.getStatus();
		this.capacity = dto.getCapacity();
		this.productGroupId = dto.getProductGroupId();
		this.ip = dto.getIp();

	}
}
