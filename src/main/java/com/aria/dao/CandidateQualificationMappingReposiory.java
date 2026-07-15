package com.aria.dao;

import com.aria.model.CandidateQualificationMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CandidateQualificationMappingReposiory extends JpaRepository<CandidateQualificationMapping,Long> {


    @Query(value = "SELECT *  FROM candidate_qualification_mapping u WHERE u.candidate_id =:empId", nativeQuery = true)
    List<CandidateQualificationMapping> findByEmpId(@Param("empId") Long empId);
}
