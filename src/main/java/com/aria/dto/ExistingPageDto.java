package com.aria.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.aria.model.ExistingPageDetails;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ExistingPageDto {

	private Long id;

	private String pageName;

	private String pageType;

	private Long pageCreatedBy;
	
	private String pageCreatedByName;

	private String pageCreatedDate;

	private Long createdBy;

	private Long updatedBy;

	private LocalDateTime created_date;

	private LocalDateTime updated_date;

	private List<FieldsInExistingPageDto> fieldsList;

	public ExistingPageDto(ExistingPageDetails page) {
		this.id = page.getId();
		this.pageName = page.getPageName();
		this.pageType = page.getPageType();

//        this.userLevelAccess = page.getUserLevelAccess();
//        this.active = page.getActive();
//		this.pageCreatedBy = page.getPageCreatedBy();
//		this.pageCreatedDate = page.getPageCreatedDate();
		this.createdBy = page.getCreatedBy();
		this.updatedBy = page.getUpdatedBy();
		this.created_date = page.getCreated_date();
		this.updated_date = page.getUpdated_date();
		this.fieldsList = page.getFieldList() != null ? page.getFieldList().stream()
				.map(field -> new FieldsInExistingPageDto(field)).collect(Collectors.toList()) : null;
	}
	
	public ExistingPageDto(ExistingPageDetails page, String pageDetailsAlone) {
		this.id = page.getId();
		this.pageName = page.getPageName();
		this.pageType = page.getPageType();
//		this.pageCreatedBy = page.getPageCreatedBy();
//		this.pageCreatedDate = page.getPageCreatedDate();
		if(page.getPageCreated()!=null) {
			this.pageCreatedByName = page.getPageCreated().getEmployeename();
		}
		this.createdBy = page.getCreatedBy();
		this.updatedBy = page.getUpdatedBy();
		this.created_date = page.getCreated_date();
		this.updated_date = page.getUpdated_date();
	}

}
