package com.aria.dao;


import com.aria.model.MedicalCheckup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface MedicalCheckupRepository  extends JpaRepository<MedicalCheckup, Long> {

    @Query(value = "SELECT mc  FROM MedicalCheckup mc "
            + "WHERE (:employeeName is null or (lower(mc.staff.employeename) LIKE ('%' || lower(:employeeName) || '%')) ) "
            + "AND (:category is null or (lower(mc.staffCategory) LIKE ('%' || lower(:category) || '%')) ) "
            + "AND  (:firm is null or (lower(mc.organisation.name) LIKE ('%' || lower(:firm) || '%')) ) "
            + "AND  (:branch is null or (lower(mc.branch.branchName) LIKE ('%' || lower(:branch) || '%')) ) "
            + "AND  (:deptName is null or (lower(mc.departmentDetails.name) LIKE ('%' || lower(:deptName) || '%')) ) "
            /*+ "AND (cast(:dateFrom as timestamp) is null or (mc.checkupDate >= :dateFrom)) "
            + "AND (cast(:dateTo as timestamp) is null or (mc.checkupDate <= :dateTo))")*/
           + "AND (mc.checkupDate BETWEEN  cast(:dateFrom  as date)  AND cast(:dateTo  as date) OR (cast(:dateFrom as date) is null OR cast(:dateTo as date) is null) )")
    Page<MedicalCheckup> findAll(@Param("employeeName") String employeeName, @Param("firm") String firm, @Param("branch") String branch, @Param("deptName") String deptName, @Param("category") String category, @Param("dateFrom") Date dateFrom, @Param("dateTo") Date dateTo, Pageable pageable);



}
