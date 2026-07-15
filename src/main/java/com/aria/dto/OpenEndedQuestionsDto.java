package com.aria.dto;

import com.aria.model.OpenEndedQuestion;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OpenEndedQuestionsDto {

	private Long id;

	private Long scheduleTrainingId;

	private Integer serialNo;

	private String question;

	private String rightAnswer;

	private Float score;

	public OpenEndedQuestionsDto(OpenEndedQuestion openEndedQuestion) {

		this.id = openEndedQuestion.getId();
		this.scheduleTrainingId = openEndedQuestion.getScheduleTrainingId();
		this.serialNo = openEndedQuestion.getSerialNo();
		this.question = openEndedQuestion.getQuestion();
		this.rightAnswer = openEndedQuestion.getRightAnswer();
		this.score = openEndedQuestion.getScore();
	}
}
