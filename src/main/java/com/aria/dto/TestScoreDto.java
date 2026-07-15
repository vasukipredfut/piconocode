package com.aria.dto;

import java.util.Date;

import com.aria.model.TestScore;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TestScoreDto {
	private Long id;

	private Long scoreId;

	private Long branchId;

	private Long interviewId;

	private Date testDate;

	private Long interviewerId;
	
	private String employee;

	private String branchName;

	private Long testScore;

	public TestScoreDto(TestScore testScore) {
		this.id = testScore.getId();
		this.scoreId = testScore.getScoreId();
		if (testScore.getTestDate() != null) {
			this.interviewId = testScore.getTestDate().getId();
			this.testDate = testScore.getTestDate().getInterviewDate();
		}
		if (testScore.getEmployee() != null) {
			this.interviewerId = testScore.getEmployee().getId();
			this.employee = testScore.getEmployee().getEmployeename();
		}
		if (testScore.getTestHeldPlace() != null) {
			this.branchId = testScore.getTestHeldPlace().getId();
			this.branchName = testScore.getTestHeldPlace().getBranchName();
		}
		this.testScore = testScore.getTestScore();

	}
}
