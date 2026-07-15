package com.aria.dao;

import com.aria.dto.IntraBranchReceiptList;
import com.aria.model.InProcessAudit;
import com.aria.model.IntraBranchReceiptEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface IntraBranchRecRepository extends JpaRepository<IntraBranchReceiptEntity, Long>{

        @Query(value = "select r from IntraBranchReceiptEntity r ")
        Page<IntraBranchReceiptEntity> intraBranchReceiptList(Pageable pageable);

        @Query(value = "select ibr.* from intra_branch_rec ibr "
                + " LEFT JOIN branch rb ON rb.id = ibr.issue_branch_id "
                + " WHERE (:hupId is null or (lower(ibr.hup_id) LIKE ('%' || lower(:hupId) || '%')) )"
                + " AND (:issueBranchName is null or (lower(rb.name) LIKE ('%' || lower(:issueBranchName) || '%')) ) "
                + " AND (:firmId is null or ibr.org_id=:firmId ) "
                + " AND (:requestBranchId is null or ibr.request_branch_id=:requestBranchId ) "
                + " AND (:deptId is null or ibr.request_dept_id=:deptId ) "
                + " AND (ibr.dispatch_on BETWEEN  cast(:dateFrom  as date)  AND cast(:dateTo  as date) OR (cast(:dateFrom as date) is null OR cast(:dateTo as date) is null) ) "
                + "AND (:status is null or (lower(ibr.status) LIKE ('%' || lower(:status) || '%'))) ORDER BY ibr.id desc", nativeQuery = true)
        Page<IntraBranchReceiptEntity> intraBranchReceiptList(@Param("firmId") Long firmId,
                                                              @Param("requestBranchId") Long requestBranchId, @Param("deptId") Long deptId,
                                                              @Param("hupId") String hupId,  @Param("issueBranchName") String issueBranchName,
                                                              @Param("dateFrom") Date dateFrom, @Param("dateTo") Date dateTo,
                                                              @Param("status") String status, Pageable pageable );

}
