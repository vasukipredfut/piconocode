
package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

import com.aria.dto.SkillDetailsDto;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "skill_details")
public class SkillDetails {


	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "competency_id")
	private Long competencyId;

	@Column
	private Long s_no;

	@Column
	private String skills;

	@Column(name = "skill_id")
	private Long skillId;

	
	public SkillDetails(SkillDetailsDto skillDetails) {
		this.id = skillDetails.getId();
		this.competencyId = skillDetails.getCompetencyId();
		this.s_no = skillDetails.getS_no();
		this.skills = skillDetails.getSkills();
	}
}
