package com.aria.dto;



import com.aria.model.SpecificationDetails;
import com.aria.utils.DateUtil;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SpecificationDetailsDto {

	private Long id;
	
	private String name;
	
	private Long refId;
	
	private String  refName;
		
	private Long specType;

	private Long branch;
	
	private Long department;
	
	private Long org;
	
	private String createdDate;
	
	private String createdBy;

	private String standingOrder;
	private String standingOrderAuth;

	private String lotList;


	public SpecificationDetailsDto(SpecificationDetails field) {

		this.id =field.getId();
		this.name=field.getName();
		this.refId=field.getRefid();
		this.refName=field.getRefname();
		this.specType=field.getSpectype();
		this.branch=field.getBranch();
		this.department=field.getDepartment();
		this.org=field.getOrg();
		this.createdBy=field.getCreatedBy();
		DateUtil dateUtil  = new DateUtil();
		this.createdDate=dateUtil.getDateToStringDDMMYYYY(field.getCreatedDate());

		this.standingOrder=field.getStandingOrder();
		this.standingOrderAuth=field.getStandingOrderAuth();
	}
	
	
	
}
