package com.aria.model;

import java.util.Date;

public interface InterviewerAssignList {

    Long getAssignId();

    Long getInterviewId();

    Long getBranchId();

    Long getInterviewerId();

    Long getInterviewerDesignationId();

    Long getExaminerId();

    Long getExaminerDesignationId();

    Date getInterviewDate();

    String getInterviewTime();

    String getCandidateName();

    String getInterviewerName();

    String getExaminerName();

    String getPlace();

    String getExaminerDesigName();

    String getInterviewerDesigName();
}
