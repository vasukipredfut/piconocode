package com.aria.dto;

import com.aria.model.SubModules;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SubModulesDto {

	private Long id;

	private String name;

	private Long moduleId;

	public SubModulesDto(SubModules model) {
		this.id = model.getId();
		this.name = model.getSubmoduleName();
		this.moduleId = model.getModuleId().getId();
	}

//    private String pageName;
//
//    private String pageType;
//
//    private String parentMenu;
//
//    private String deptName;
//
//    private String userLevelAccess;
//
//    private int active;
//
//    private Long createdBy;
//
//    private Long updatedBy;
//
//    private String createdByName;
//
//    private String updatedByName;
//
//    private LocalDateTime created_date;
//
//    private LocalDateTime updated_date;
//
//    private List<PageFieldDto> fieldList;
//
//    public SubModulesDto(PageDetails page) {
//        this.id = page.getId();
//        this.pageName = page.getPageName();
//        this.pageType = page.getPageType();
//        this.parentMenu = page.getParentMenu();
//        if (page.getDepartmentDetails() != null) {
//            this.deptName = page.getDepartmentDetails().getName();
//        }
//        this.userLevelAccess = page.getUserLevelAccess();
//        this.active = page.getActive();
//        this.createdBy = page.getCreatedBy();
//        this.updatedBy = page.getUpdatedBy();
//        this.created_date = page.getCreated_date();
//        if (page.getUpdated_date() != null) {
//            this.updated_date = page.getUpdated_date();
//        } else {
//            this.updated_date = page.getCreated_date();
//        }
//        this.fieldList = page.getFieldList() != null
//                ? page.getFieldList().stream().map(field -> new PageFieldDto(field)).collect(Collectors.toList()) : null;
//    }
}
