package com.aria.dto;

import java.time.LocalDateTime;
import java.util.Date;

import com.aria.model.Biomast;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BiomastDto {

	private Long sno;

	private String machineip;

	private Long branchid;

	private String location;

	private Long interval;

	private String gate;

	private String istatus;

	private Long instrutype;

	private Long lupid;

	private String cstatus;

	private String creason;

	private Date lastupdate;

	private Date lupdt;

	private Date updatedon;

	private Long createdBy;

	private Long updatedBy;

	private LocalDateTime created_date;

	private LocalDateTime updated_date;

	public BiomastDto(Biomast biomast) {

		this.sno = biomast.getSno();

		this.machineip = biomast.getMachineip();

		if (biomast.getBranch() != null) {
			this.branchid = biomast.getBranch().getId();
		}

		this.location = biomast.getLocation();

		this.interval = biomast.getInterval();

		this.gate = biomast.getGate();

		this.istatus = biomast.getIstatus();

		this.instrutype = biomast.getInstrutype();

		this.lupid = biomast.getLupid();

		this.cstatus = biomast.getCstatus();

		this.creason = biomast.getCreason();

		this.lastupdate = biomast.getLastupdate();

		this.lupdt = biomast.getLupdt();

		this.updatedon = biomast.getUpdatedon();

		this.createdBy = biomast.getCreatedBy();

		this.updatedBy = biomast.getUpdatedBy();

		this.created_date = biomast.getCreated_date();

		this.updated_date = biomast.getUpdated_date();
	}
}
