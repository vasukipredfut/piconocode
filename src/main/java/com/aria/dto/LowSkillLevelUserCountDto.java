package com.aria.dto;

import com.aria.model.ILowSkillLevelUserCount;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LowSkillLevelUserCountDto {

	Long id;

	Integer userCount;

	public LowSkillLevelUserCountDto(ILowSkillLevelUserCount iLowSkillLevelUserCount) {

		this.id = iLowSkillLevelUserCount.getSkillId();
		this.userCount = iLowSkillLevelUserCount.getUserCount();

	}
}
