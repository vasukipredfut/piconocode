package com.aria.dto;

import java.util.Random;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PatlogListDto {

	public String entryType;
	public String kpiName;
	public String inwardDate;
	public String status;
	public String statusType;
	public String batchno;
	public Long kpiNameId;
	public Long id;
	public String noOfBag;

	public PatlogListDto(PatlogList patlogList, Random random) {
		this.id = patlogList.getId();
		this.entryType = patlogList.getEntryType();
		this.kpiName = patlogList.getKpiName();
		this.inwardDate = patlogList.getInwardDate();
		if (patlogList.getStatus() != null) {
			this.status = patlogList.getStatus();
		} else {
			this.status = "Completed";
		}
		if (patlogList.getStatusType() != null) {
			this.statusType = patlogList.getStatusType();
		} else {
			this.statusType = "Close";
		}
		this.batchno = patlogList.getBatchno();
		this.noOfBag = patlogList.getNoOfBag();
		this.kpiNameId = patlogList.getKpiNameId();
	}
}
