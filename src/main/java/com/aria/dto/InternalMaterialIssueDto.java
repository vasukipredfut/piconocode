package com.aria.dto;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;

import com.aria.model.InternalMaterialIssue;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class InternalMaterialIssueDto {

	private Long id;

	private String requestreferance;

	private String issuedepartment;

	private String issueperson;

	private Date issueddate;

	private Long createdBy;

	private Long updatedBy;

	private LocalDateTime created_date;

	private LocalDateTime updated_date;

	private List<ReceivedItemsDto> receivedItemsDetails;

	public InternalMaterialIssueDto(InternalMaterialIssue internalMaterialIssue) {
		this.id = internalMaterialIssue.getId();
		this.requestreferance = internalMaterialIssue.getRequestreferance();
		this.issuedepartment = internalMaterialIssue.getIssuedepartment();
		this.issueperson = internalMaterialIssue.getIssueperson();
		this.issueddate = internalMaterialIssue.getIssueddate();
		this.createdBy = internalMaterialIssue.getCreatedBy();
		this.updatedBy = internalMaterialIssue.getUpdatedBy();
		this.created_date = internalMaterialIssue.getCreated_date();
		if (internalMaterialIssue.getUpdated_date() != null) {
			this.updated_date = internalMaterialIssue.getUpdated_date();
		} else {
			this.updated_date = internalMaterialIssue.getCreated_date();
		}

	}

}