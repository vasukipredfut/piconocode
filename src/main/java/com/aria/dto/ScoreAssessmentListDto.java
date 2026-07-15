package com.aria.dto;

import com.aria.model.ScoreAssessmentList;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ScoreAssessmentListDto {


    private Long scoreId;

    private String candidateName;

    private String interviewDate;

    private String phoneNumber;

    private String industrialScore;

    private String foodSafteyScore;

    private String interviewScore;

    private String overallScore;


    public ScoreAssessmentListDto(ScoreAssessmentList scoreAssessmentList) {
        DateUtil dateUtil = new DateUtil();
        this.scoreId = scoreAssessmentList.getScoreId();
        this.candidateName = scoreAssessmentList.getCandidateName();
        this.interviewDate = scoreAssessmentList.getInterviewDate()!=null ? dateUtil.getDateTOStringWk(scoreAssessmentList.getInterviewDate()) : null;
        this.phoneNumber = scoreAssessmentList.getPhoneNumber();
        this.industrialScore = scoreAssessmentList.getIndustrialScore();
        this.foodSafteyScore = scoreAssessmentList.getFoodSafteyScore();
        this.interviewScore = scoreAssessmentList.getInterviewScore();
        this.overallScore = scoreAssessmentList.getOverallScore();
    }
}
