package com.aria.dto;

import java.time.LocalDateTime;
import com.aria.model.RodentMonitor;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RodentMonitorDto {

	private Long id;
	private String rodentDate;
	
	private Long godownId;
	private String goDownName;
	
	private String boxNo;
	
	private String reason;
	
	private Long createdBy;

	private Long updatedBy;

	private LocalDateTime created_date;

	private LocalDateTime updated_date;

	public RodentMonitorDto(RodentMonitor field) {
		this.id = field.getId();
		
		if (field.getGodown() != null) {
			this.godownId = field.getGodown().getId();
			this.goDownName = field.getGodown().getName();
		}
		
		this.boxNo = field.getBoxNo();
		this.reason = field.getReason();

		this.createdBy = field.getCreatedBy();
		this.updatedBy = field.getUpdatedBy();
		this.created_date = field.getCreated_date();
		this.updated_date = field.getUpdated_date();

	}

}
