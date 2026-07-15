package com.aria.model;

import java.time.LocalDateTime;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@Table(name = "training_user_registration_mapping")
public class TrainingUserRegistrationMapping {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "schedule_training_id", referencedColumnName = "id", nullable = false)
	private ScheduleTraining scheduleTraining;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "trainee_id", referencedColumnName = "id", nullable = false)
	private User user;

	@Column(name = "registration_Date")
	private Date registrationDate;

	@Column(name = "registration_Status")
	private String registrationStatus;

	@Column(name = "attendance_Status")
	private String attendanceStatus;

	@Column(name = "created_by", updatable = false)
	private Long createdBy;

	@Column(name = "updated_by")
	private Long updatedBy;

	@Column(updatable = false)
	private LocalDateTime created_date;

	@Column
	private LocalDateTime updated_date;

}
