package com.aria.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.aria.dto.TrainerDetailDto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@Table(name = "trainer_detail")
public class TrainerDetail {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne
	@JoinColumn(name = "department_id", referencedColumnName = "id")
	private DepartmentDetails department;

	@OneToOne
	@JoinColumn(name = "trainer_id", referencedColumnName = "id")
	private User trainer;

	@Column(name = "external_Employee_Name")
	private String externalEmployeeName;

	@Column(name = "external_Organisation_Name")
	private String externalOrganisationName;


	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "schedule_training_id", nullable = false)
	private ScheduleTraining scheduleTraining;

	@Column(name = "average_Score")
	private Float averageScore;


	public TrainerDetail(TrainerDetailDto trainerDetailDto) {

		this.id = trainerDetailDto.getId();

		if (trainerDetailDto.getDepartmentId() != null) {
			DepartmentDetails departmentDetails = new DepartmentDetails();
			departmentDetails.setId(trainerDetailDto.getDepartmentId());
			this.department = departmentDetails;
		}

		if (trainerDetailDto.getTrainerId() != null) {
			User user = new User();
			user.setId(trainerDetailDto.getTrainerId());
			this.trainer = user;
		}

		this.externalEmployeeName = trainerDetailDto.getExternalEmployeeName();

		this.externalOrganisationName = trainerDetailDto.getExternalOrganisationName();


		if (trainerDetailDto.getScheduleTrainingId() != null) {
			ScheduleTraining scheduleTraining = new ScheduleTraining();
			scheduleTraining.setId(trainerDetailDto.getScheduleTrainingId());
			this.scheduleTraining = scheduleTraining;
		}

		this.averageScore = trainerDetailDto.getAverageScore();
	}

}
