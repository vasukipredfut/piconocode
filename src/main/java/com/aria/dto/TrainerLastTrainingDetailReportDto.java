package com.aria.dto;

import com.aria.model.ITrainerLastTrainingDetailReport;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TrainerLastTrainingDetailReportDto {

	private Long trainerDetailId;

	private Long lastScheduleTrainingId;

	private String lastTrainingDate;

	private Integer trainingCount;

	private Double lastTrainingscore;

	public TrainerLastTrainingDetailReportDto(ITrainerLastTrainingDetailReport iTrainerLastTrainingDetailReport) {

		this.trainerDetailId = iTrainerLastTrainingDetailReport.getTrainerDetailId();
		this.lastScheduleTrainingId = iTrainerLastTrainingDetailReport.getLastScheduleTrainingId();
//		this.lastTrainingDate = iTrainerLastTrainingDetailReport.getLastTrainingDate();
		this.lastTrainingscore = iTrainerLastTrainingDetailReport.getLastTrainingScore();
	}
}
