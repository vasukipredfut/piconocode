package com.aria.dto;

import java.util.List;

import org.apache.commons.text.StringEscapeUtils;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ExpGroupListDto {
	
	private Long id;
	private String expName;
	private String expType;
	private Double cost;
	private String associatedWith;
	private String unit;
	private String category;
	private String primaryExpName;
	private Long groupId;
   private  Long primaryExpId;
	
	private List<CustomVouNameByUserDeptDto> detalis;
	public ExpGroupListDto (ExpGroupList expGroupList)
	{
		this.id=expGroupList.getId();
		this.expName=StringEscapeUtils.unescapeHtml4(expGroupList.getExpName());
		this.expType=expGroupList.getExpType();
		this.cost=expGroupList.getCost();
		this.associatedWith=expGroupList.getAssociatedWith();
		this.unit=expGroupList.getUnit();
		this.category=expGroupList.getCategory();
		this.primaryExpId=expGroupList.getPrimaryExpId();
		this.primaryExpName=expGroupList.getPrimaryExpName();
		this.groupId=expGroupList.getGroupId();
	}

}
