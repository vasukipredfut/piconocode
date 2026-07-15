package com.aria.model;

import java.util.Date;

public interface ScoreAssessmentList {

    Long getScoreId();

    Date getInterviewDate();

    String getCandidateName();

    String getPhoneNumber();

    String getTestScore();

    String getIndustrialScore();

    String getFoodSafteyScore();

    String getInterviewScore();

    String getOverallScore();
}
