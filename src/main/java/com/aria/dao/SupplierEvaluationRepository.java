package com.aria.dao;

import com.aria.dto.SupplierEvalValidUsers;
import com.aria.model.SupplierEvaluation;
import com.aria.model.SupplierEvaluationReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface SupplierEvaluationRepository extends JpaRepository<SupplierEvaluation, Long> {

    @Query(value = "SELECT * FROM supeval s WHERE  s.trnno =:trnNo ", nativeQuery = true)
    Optional<SupplierEvaluation> findBy(@Param("trnNo") String trnNo);


    @Query(value = "select s.trnno as trnNo , s.status as status , s.created_date as createdDate, cr.id as createdStaff , cr.employeename as createdStaffName , " +
            "s.approved_date as approvedDate, ap.id as approvalStaff , ap.employeename as approvalStaffName , v.id as verifiedStaff , " +
            "v.employeename as verifiedStaffName , s.verify_date as verifiedDate, au.id as authorisedStaff , au.employeename as authorisedStaffName, " +
            "s.authorised_date as authorisedDate from supeval s left join employee_details cr on cr.id = s.created_by " +
            "left join employee_details ap on ap.id = s.approval_staff left join employee_details v on v.id  = s.verify_staff " +
            "left join employee_details au on au.id  = s.authorise_staff  WHERE  s.trnno =:trnNo LIMIT 1", nativeQuery = true)
    SupplierEvalValidUsers findByUsers(@Param("trnNo") String trnNo);


    @Query(value = "SELECT * FROM supeval s WHERE  s.cid =:company AND s.branch =:branchId AND s.vid =:vendorId AND  s.fromdate =:fromDate AND  s.todate =:toDate", nativeQuery = true)
    Optional<SupplierEvaluation> findBy(@Param("company") Long company ,
                                        @Param("branchId") Long branchId,
                                        @Param("vendorId") Long vendorId,
                                        @Param("fromDate") Date fromDate,
                                        @Param("toDate") Date toDate);
}
