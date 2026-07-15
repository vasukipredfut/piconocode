package com.aria.dao;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.aria.model.InterviewerAssignList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.InterviewerAssign;

@Repository
public interface InterviewerAssignRepository extends JpaRepository<InterviewerAssign, Long> {

    @Query(value = "SELECT ia  FROM InterviewerAssign ia "
            + "WHERE (:candidateName is null or (lower(ia.interview.candidateName) LIKE ('%' || lower(:candidateName) || '%')) ) "
            + "AND  (:branch is null or (lower(ia.place.branchName) LIKE ('%' || lower(:branch) || '%')) ) "
            + "AND (:interviewerName is null or (lower(ia.interviewer.employeename) LIKE ('%' || lower(:interviewerName) || '%')) ) "
            + "AND (:examinerName is null or (lower(ia.examiner.employeename) LIKE ('%' || lower(:examinerName) || '%')) ) "
            + "AND (cast(:dateFrom as timestamp) is null or (ia.interview.interviewDate >= :dateFrom)) "
            + "AND (cast(:dateTo as timestamp) is null or (ia.interview.interviewDate <= :dateTo)) ORDER BY ia.id DESC")
    Page<InterviewerAssign> interviewerAssignfilterList(
            @Param("candidateName") String candidateName, @Param("branch") String branch,
            @Param("interviewerName") String interviewerName, @Param("examinerName") String examinerName,
            @Param("dateFrom") Date dateFrom, @Param("dateTo") Date dateTo, Pageable pageable);

    @Query(value = "select ia from InterviewerAssign ia where ia.interview.id=:id")
    Optional<InterviewerAssign> findByInterviewId(@Param("id") Long id);

    @Query(value = "select ia.id as assignId, iv.candidate_name as candidateName, exam.id as examinerId , exam.employeename as examinerName, " +
            "examdes.name as examinerDesigName, examdes.id as examinerDesignationId, inter.id as interviewerId ,inter.employeename as interviewerName,  " +
            "interdes.name as interviewerDesigName, interdes.id as interviewerDesignationId, iv.id as interviewId,   iv.interview_date as interviewDate, " +
            "iv.interview_time as interviewTime , br.id as branchId, br.name as place   from interviewer_assign ia LEFT JOIN interview iv ON  iv.id =ia.interview_id LEFT JOIN employee_details exam ON exam.id = ia.examiner_id LEFT JOIN designation examdes ON examdes.id = exam.desi_id LEFT JOIN employee_details inter ON inter.id = ia.interviewer_id LEFT JOIN designation interdes ON interdes.id = inter.desi_id LEFT JOIN branch br ON br.id = iv.branch_id "
            + "WHERE (:branchName is null or (lower(br.name) LIKE ('%' || lower(:branchName) || '%')) )"
            + "AND (:candidateName is null or (lower(iv.candidate_name) LIKE ('%' || lower(:candidateName) || '%')) )"
            + "AND (:interviewerName is null or (lower(inter.employeename) LIKE ('%' || lower(:interviewerName) || '%')) ) "
            + "AND (:examinerName is null or (lower(exam.employeename) LIKE ('%' || lower(:examinerName) || '%')) ) "
            + "AND (cast(:dateFrom as timestamp) is null or (iv.interview_date >= :dateFrom)) "
            + "AND (cast(:dateTo as timestamp) is null or (iv.interview_date <= :dateTo)) ORDER BY ia.id DESC", nativeQuery = true)
    Page<InterviewerAssignList> interviewerAssignFilterList(@Param("branchName") String branchName, @Param("candidateName") String candidateName,
															@Param("interviewerName") String interviewerName, @Param("examinerName") String examinerName,
															@Param("dateFrom") Date dateFrom, @Param("dateTo") Date dateTo, Pageable pageable);

}
