package com.aria.dto;

import com.aria.model.SkillDetails;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SkillDetailsDto {
	private Long id;

	private Long competencyId;

	private Long s_no;

	private String skills;

	private Long skillId;

	public SkillDetailsDto(SkillDetails skillDetails) {
		this.id = skillDetails.getId();
		this.competencyId = skillDetails.getCompetencyId();
		this.s_no = skillDetails.getS_no();
		this.skills = skillDetails.getSkills();
		this.skillId = skillDetails.getSkillId();
	}
}
