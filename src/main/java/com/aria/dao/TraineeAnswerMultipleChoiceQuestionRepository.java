package com.aria.dao;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.TraineeAnswerMultipleChoiceQuestion;

@Repository
public interface TraineeAnswerMultipleChoiceQuestionRepository
		extends JpaRepository<TraineeAnswerMultipleChoiceQuestion, Long> {

//	@Query(value = "DELETE FROM trainee_answer_multiple_choice_question tamcq "
//			+ "WHERE (tamcq.multiple_choice_question_id = :multipleChoiceQuestionId)", nativeQuery = true)
//	void deleteByMultipleChoicQuestionId(@Param("multipleChoiceQuestionId") Long multipleChoiceQuestionId);

	@Modifying
	@Query(value = "DELETE FROM TraineeAnswerMultipleChoiceQuestion tamcq "
			+ "WHERE (tamcq.multipleChoiceQuestion.id = :multipleChoiceQuestionId)")
	void deleteByMultipleChoicQuestionId(@Param("multipleChoiceQuestionId") Long multipleChoiceQuestionId);

	@Modifying
	@Query(value = "DELETE FROM TraineeAnswerMultipleChoiceQuestion tamcq "
			+ "WHERE (tamcq.multipleChoiceQuestion.id IN :multipleChoiceQuestionIds)")
	void deleteAllByMultipleChoiceQuestionsIdIn(
			@Param("multipleChoiceQuestionIds") Set<Long> multipleChoiceQuestionIds);
}
