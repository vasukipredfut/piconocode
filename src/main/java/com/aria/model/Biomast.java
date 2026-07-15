package com.aria.model;

import java.time.LocalDateTime;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.aria.dto.BiomastDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "biomast")
public class Biomast {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long sno;

	@Column
	private String machineip;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "branchid", referencedColumnName = "id", nullable = false)
	private Branch branch;

	@Column
	private String location;

	@Column
	private Long interval;

	@Column
	private String gate;

	@Column
	private String istatus;

	@Column
	private Long instrutype;

	@Column
	private Long lupid;

	@Column
	private String cstatus;

	@Column
	private String creason;

	@Column
	private Date lastupdate;

	@Column
	private Date lupdt;

	@Column
	private Date updatedon;

	@Column(updatable = false)
	private Long createdBy;

	@Column
	private Long updatedBy;

	@Column(updatable = false)
	private LocalDateTime created_date;

	@Column
	private LocalDateTime updated_date;

	public Biomast(BiomastDto biomastDto) {

		this.sno = biomastDto.getSno();

		this.machineip = biomastDto.getMachineip();

//		this.branchid = biomastDto.getBranchid();

		if (biomastDto.getBranchid() != null) {
			Branch branch = new Branch();
			branch.setId(biomastDto.getBranchid());
			this.branch = branch;
		}

		this.location = biomastDto.getLocation();

		this.interval = biomastDto.getInterval();

		this.gate = biomastDto.getGate();

		this.istatus = biomastDto.getIstatus();

		this.instrutype = biomastDto.getInstrutype();

		this.lupid = biomastDto.getLupid();

		this.cstatus = biomastDto.getCstatus();

		this.creason = biomastDto.getCreason();

		this.lastupdate = biomastDto.getLastupdate();

		this.lupdt = biomastDto.getLupdt();

		this.updatedon = biomastDto.getUpdatedon();

		this.createdBy = biomastDto.getCreatedBy();

		this.updatedBy = biomastDto.getUpdatedBy();

		this.created_date = biomastDto.getCreated_date();

		this.updated_date = biomastDto.getUpdated_date();
	}
}
