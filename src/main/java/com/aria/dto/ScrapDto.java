package com.aria.dto;

import java.time.LocalDateTime;
import com.aria.model.Scrap;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ScrapDto {
	
	private Long id;

	private String scrapItem;
	
	private String storedOn;

	private Long deptId;

	private String departmentName;
	
	private String fromProcess;

	private String scrapType;

	private String selectStorageBin;
	
	
	
	private Long createdBy;

	private Long updatedBy;

	private LocalDateTime created_date;

	private LocalDateTime updated_date;

	public ScrapDto(Scrap scrap) {
		this.id = scrap.getId();
		this.scrapItem = scrap.getScrapItem();
		if (scrap.getFromDepartment() != null) {
			this.deptId = scrap.getFromDepartment().getId();
			this.departmentName = scrap.getFromDepartment().getName();
		}
		this.fromProcess = scrap.getFromProcess();
		this.scrapType = scrap.getScrapType();
		this.selectStorageBin = scrap.getSelectStorageBin();
		this.createdBy = scrap.getCreatedBy();
		this.updatedBy = scrap.getUpdatedBy();
		this.created_date = scrap.getCreated_date();
		this.updated_date = scrap.getUpdated_date();
	}
}
