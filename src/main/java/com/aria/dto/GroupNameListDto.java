package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GroupNameListDto {

	private Long id;
	private String name;
	private Double amount;
	public GroupNameListDto(GroupNameList groupNameList) {
		
		this.id = groupNameList.getId();
		this.name = groupNameList.getName();
		this.amount = groupNameList.getTotalCost();
	}
	
	
}
