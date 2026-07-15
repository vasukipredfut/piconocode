package com.aria.dao;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.TraineeAnswerOpenEndedQuestion;

@Repository
public interface TraineeAnswerOpenEndedQuestionRepository extends JpaRepository<TraineeAnswerOpenEndedQuestion, Long> {

//	@Query(value = "DELETE FROM trainee_answer_open_ended_question taoqr "
//			+ "WHERE (taoqr.open_ended_question_id = :openEndedQuestionId)", nativeQuery = true)
//	void deleteByOpenEndedQuestionId(@Param("openEndedQuestionId") Long openEndedQuestionId);

	@Modifying
	@Query(value = "DELETE FROM TraineeAnswerOpenEndedQuestion taoqr "
			+ "WHERE (taoqr.openEndedQuestion.id = :openEndedQuestionId)")
	void deleteByOpenEndedQuestionId(@Param("openEndedQuestionId") Long openEndedQuestionId);

	@Modifying
	@Query(value = "DELETE FROM TraineeAnswerOpenEndedQuestion taoqr "
			+ "WHERE (taoqr.openEndedQuestion.id IN :openEndedQuestionIds)")
	void deleteAllByOpenEndedQuestionIdIn(@Param("openEndedQuestionIds") Set<Long> openEndedQuestionIds);
}
