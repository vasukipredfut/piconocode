package com.aria.dto;

import com.aria.model.ITrainerTrainingHistory;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TrainerTrainingHistoryDto {

	private Long trainerDetailId;

	private Long departmentId;

	private String departmentName;

	private Long designationId;

	private String designationName;

	private Long skillId;

	private String skillName;

	private String trainingDate;

	private Double averageScore;

	public TrainerTrainingHistoryDto(ITrainerTrainingHistory iTrainerTrainingHistory) {
		this.trainerDetailId = iTrainerTrainingHistory.getTrainerDetailId();
		this.departmentId = iTrainerTrainingHistory.getDepartmentId();
		this.departmentName = iTrainerTrainingHistory.getDepartmentName();
		this.designationId = iTrainerTrainingHistory.getDesignationId();
		this.designationName = iTrainerTrainingHistory.getDesignationName();
		this.skillId = iTrainerTrainingHistory.getSkillId();
		this.skillName = iTrainerTrainingHistory.getSkillName();
//		this.trainingDate = iTrainerTrainingHistory.getTrainingDate();
		this.averageScore = iTrainerTrainingHistory.getAverageScore();
	}
}
