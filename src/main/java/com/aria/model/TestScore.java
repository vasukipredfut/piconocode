package com.aria.model;

import com.aria.dto.TestScoreDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "test_score")
public class TestScore {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "score_id")
	private Long scoreId;

	@OneToOne
	@JoinColumn(name = "interview_id", referencedColumnName = "id")
	private Interview testDate;

	@OneToOne
	@JoinColumn(name = "interviewer_id", referencedColumnName = "id")
	private User employee;

	@OneToOne
	@JoinColumn(name = "branch_id", referencedColumnName = "id")
	private Branch testHeldPlace;

	@Column(name = "test_Score")
	private Long testScore;

	public TestScore(TestScoreDto testScore) {
		this.id = testScore.getId();
		this.scoreId = testScore.getScoreId();
		if (testScore.getInterviewId() != null) {
			Interview testDate = new Interview();
			testDate.setId(testScore.getInterviewId());
			this.testDate = testDate;
		}
		if (testScore.getInterviewerId() != null) {
			User user = new User();
			user.setId(testScore.getInterviewerId());
			this.employee = user;
		}

		if (testScore.getBranchId() != null) {
			Branch testHeldPlace = new Branch();
			testHeldPlace.setId(testScore.getBranchId());
			this.testHeldPlace = testHeldPlace;
		}

		this.testScore = testScore.getTestScore();

	}
}
