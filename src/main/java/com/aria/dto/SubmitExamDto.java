package com.aria.dto;

import java.util.Set;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SubmitExamDto {

	private Long scheduleTrainingId;

//	private Long traineeId;

	private Set<TraineeAnswerMultipleChoiceQuestionDto> traineeAnswerMultipleChoiceQuestionDtos;

	private Set<TraineeAnswerOpenEndedQuestionDto> traineeAnswerOpenEndedQuestionDtos;

//	public TraineeAnswerMultipleChoiceQuestionDto(
//			TraineeAnswerMultipleChoiceQuestionDto traineeAnswerMultipleChoiceQuestionDto) {
//		 
//
//	}
}
