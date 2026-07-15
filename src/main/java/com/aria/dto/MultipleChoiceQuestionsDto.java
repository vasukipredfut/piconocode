package com.aria.dto;

import com.aria.model.MultipleChoiceQuestion;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MultipleChoiceQuestionsDto {

	private Long id;

//	private ScheduleTraining scheduleTraining;

	private Long scheduleTrainingId;

	private Integer serialNo;

	private String question;

	private String optionA;

	private String optionB;

	private String optionC;

	private String optionD;

	private String rightAnswer;

	private Float score;

	public MultipleChoiceQuestionsDto(MultipleChoiceQuestion multipleChoiceQuestion) {

		this.id = multipleChoiceQuestion.getId();
		this.scheduleTrainingId = multipleChoiceQuestion.getScheduleTrainingId();
		this.serialNo = multipleChoiceQuestion.getSerialNo();
		this.question = multipleChoiceQuestion.getQuestion();
		this.optionA = multipleChoiceQuestion.getOptionA();
		this.optionB = multipleChoiceQuestion.getOptionB();
		this.optionC = multipleChoiceQuestion.getOptionC();
		this.optionD = multipleChoiceQuestion.getOptionD();
		this.rightAnswer = multipleChoiceQuestion.getRightAnswer();
		this.score = multipleChoiceQuestion.getScore();
	}
}
