package com.aria.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.aria.dto.MultipleChoiceQuestionsDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "multiple_choice_question")
public class MultipleChoiceQuestion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "schedule_training_id")
	private Long scheduleTrainingId;

	@Column(name = "serial_no")
	private Integer serialNo;

	@Column
	private String question;

	@Column
	private String optionA;

	@Column
	private String optionB;

	@Column
	private String optionC;

	@Column
	private String optionD;

	@Column(name = "right_answer")
	private String rightAnswer;

	@Column
	private Float score;

	public MultipleChoiceQuestion(MultipleChoiceQuestionsDto multipleChoiceQuestionsDto) {

		this.id = multipleChoiceQuestionsDto.getId();
		this.scheduleTrainingId = multipleChoiceQuestionsDto.getScheduleTrainingId();
		this.serialNo = multipleChoiceQuestionsDto.getSerialNo();
		this.question = multipleChoiceQuestionsDto.getQuestion();
		this.optionA = multipleChoiceQuestionsDto.getOptionA();
		this.optionB = multipleChoiceQuestionsDto.getOptionB();
		this.optionC = multipleChoiceQuestionsDto.getOptionC();
		this.optionD = multipleChoiceQuestionsDto.getOptionC();
		this.rightAnswer = multipleChoiceQuestionsDto.getRightAnswer();
		this.score = multipleChoiceQuestionsDto.getScore();
	}
}
