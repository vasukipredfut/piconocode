package com.aria.dto;

import com.aria.model.InterviewerAssignList;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class InterviewerAssignListDto {

    private Long assignId;

    private String interviewDate;

    private String interviewTime;

    private String candidateName;

    private String interviewerName;

    private String examinerName;

    private String place;

    private Long interviewId;

    private Long interviewerId;

    private Long interviewerDesignationId;

    private String interviewerDesigName;

    private Long examinerId;

    private Long examinerDesignationId;

    private String examinerDesigName;

    private Long branchId;


    public InterviewerAssignListDto(InterviewerAssignList assignList) {
        DateUtil dateUtil = new DateUtil();
        this.assignId = assignList.getAssignId();
        if(assignList.getInterviewDate() != null) {
            this.interviewDate = dateUtil.getDateTOStringWk(assignList.getInterviewDate());
        }
        this.interviewTime = assignList.getInterviewTime();
        this.candidateName = assignList.getCandidateName();
        this.interviewerName = assignList.getInterviewerName();
        this.examinerName = assignList.getExaminerName();
        this.place = assignList.getPlace();
        this.interviewerId = assignList.getInterviewerId();
        this.interviewId = assignList.getInterviewId();
        this.examinerDesignationId = assignList.getExaminerDesignationId();
        this.examinerDesigName = assignList.getExaminerDesigName();
        this.interviewerDesigName = assignList.getInterviewerDesigName();
        this.interviewerDesignationId = assignList.getInterviewerDesignationId();
        this.branchId = assignList.getBranchId();
        this.examinerId = assignList.getExaminerId();
    }
}
