package com.aria.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.aria.dto.TrainingUserLevelMappingDto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@Table(name = "training_user_level_mapping")
public class TrainingUserLevelMapping {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_Level")
	private String userLevel;

	@Column(name = "schedule_Training_Id")
	private Long scheduleTrainingId;

	public TrainingUserLevelMapping(TrainingUserLevelMappingDto trainingUserLevelMappingDto) {

		this.id = trainingUserLevelMappingDto.getId();

		this.userLevel = trainingUserLevelMappingDto.getUserLevel();

		this.scheduleTrainingId = trainingUserLevelMappingDto.getScheduleTrainingId();
	}
}
