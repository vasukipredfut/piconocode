package com.aria.dto;



import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class journalTypeDTO {

	private Long workCenterId;
	private String workCenterName;
	private String producedProductName;
	private String journalName;
	private String batchNo;

	public Long getWorkCenterId() {
		return workCenterId;
	}

	public String getWorkCenterName() {
		return workCenterName;
	}

	public String getProducedProductName() {
		return producedProductName;
	}

	public String getJournalName() {
		return journalName;
	}

	public String getBatchNo() {
		return batchNo;
	}

	public List<WagesJournalWorkDTO>  journalEntryProducts;

	public journalTypeDTO(journalTypeList jList) {
		this.workCenterId = jList.getWorkCenterId();
		this.workCenterName = jList.getWorkCenterName();
		this.producedProductName = jList.getProducedProductName();
		this.journalName = jList.getJournalName();
		this.batchNo = jList.getBatchNo();
	}

}
