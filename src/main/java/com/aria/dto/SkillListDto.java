package com.aria.dto;

import com.aria.model.ISkillList;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SkillListDto {

	private Long id;

	private String skillName;

	public SkillListDto(ISkillList iSkillList) {
		this.id = iSkillList.getId();
		this.skillName = iSkillList.getSkills();
	}
}
