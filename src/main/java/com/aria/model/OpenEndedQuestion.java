package com.aria.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.aria.dto.OpenEndedQuestionsDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "open_ended_question")
public class OpenEndedQuestion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "schedule_training_id")
	private Long scheduleTrainingId;

	@Column(name = "serial_no")
	private Integer serialNo;

	@Column
	private String question;

	@Column(name = "right_answer")
	private String rightAnswer;

	@Column
	private Float score;

	public OpenEndedQuestion(OpenEndedQuestionsDto openEndedQuestionsDto) {

		this.id = openEndedQuestionsDto.getId();
		this.scheduleTrainingId = openEndedQuestionsDto.getScheduleTrainingId();
		this.serialNo = openEndedQuestionsDto.getSerialNo();
		this.question = openEndedQuestionsDto.getQuestion();
		this.rightAnswer = openEndedQuestionsDto.getRightAnswer();
		this.score = openEndedQuestionsDto.getScore();
	}
}
