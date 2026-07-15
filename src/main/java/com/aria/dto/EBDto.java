package com.aria.dto;

import java.time.LocalDateTime;

import com.aria.model.EB;
import com.aria.model.User;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EBDto {

	private Long id;
	private String serviceName;
	private String serviceCategory;
	private String serviceNo;
	private String status;
	private String branchId;
	private String branchName;
	private String firmId;
	private String firmName;
	private Long createdBy;

	private Long updatedBy;
	
	private String createdByName;

	private String updatedByName;

	private LocalDateTime created_date;

	private LocalDateTime updated_date;

	public EBDto(EB eb) {
		this.id = eb.getId();

		this.serviceName = eb.getServiceName();

		this.serviceCategory = eb.getServiceCategory();
		this.serviceNo = eb.getServiceNo();
		this.status = eb.getStatus();

		if(eb.getBranch() != null){
			this.branchId = String.valueOf(eb.getBranch().getId());
			this.branchName = eb.getBranch().getBranchName();
		}
		if(eb.getFirm() != null){
			this.firmId = String.valueOf(eb.getFirm().getId());
			this.firmName = eb.getFirm().getName();
		}
		if(eb.getCreatedBy()!=null) {
			this.createdBy = eb.getCreatedBy().getId();
			this.createdByName = eb.getCreatedBy().getEmployeename();
		}
		
		if(eb.getUpdatedBy()!=null) {
			this.updatedBy = eb.getUpdatedBy().getId();
			this.updatedByName = eb.getUpdatedBy().getEmployeename();
		}
//		this.createdBy = eb.getCreatedBy();
//		this.updatedBy = eb.getUpdatedBy();
		this.created_date = eb.getCreated_date();
		this.updated_date = eb.getUpdated_date();

	}

}
