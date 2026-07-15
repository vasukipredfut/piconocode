package com.aria.dao;

import com.aria.dto.DWagesSalaryList;
import com.aria.dto.DWagesSalarySummary;
import com.aria.dto.PrintWagesSalaryVoucher;
import com.aria.model.CwagesSUM;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface CwagesSUMRepository extends JpaRepository<CwagesSUM, Long> {


    @Query(value = " SELECT c.branch as branchId,  c.dept as deptId, c.expref as trnName, c.pshift as pshiftId,  c.trnno as trnNo, "
            + " c.refid as itemId, c.reftrnid as refTrnId, c.reftrnno as refTrnNo,   c.reftrntable as refTrnTable, c.shiftdate as shiftDate, "
            + " max (p.name) as pShiftName   from cwagessum c left join  pshifts p on p.id  = c.pshift "
            + " WHERE (:branchId is null or c.branch =:branchId ) "
            + " AND  (:deptId is null or c.dept =:deptId ) "
            + " AND  (:shiftId is null or c.pshift =:shiftId ) "
            + " AND  (:shiftName is null or (lower(p.name) LIKE ('%' || lower(:shiftName) || '%')) ) "
            + " AND  (:trnNo is null or (lower(c.reftrnno) LIKE ('%' || lower(:trnNo) || '%')) ) "
            + " AND  (:trnName is null or (lower(c.expref) LIKE ('%' || lower(:trnName) || '%')) ) "
            + " AND  (:trnType is null or (lower(c.expref) LIKE ('%' || lower(:trnType) || '%')) ) "
            + " AND  ( cast(:shiftDate  as date) is null OR (c.shiftdate =:shiftDate) ) "
            + " GROUP BY c.branch,  c.dept, c.trnno, c.expref, c.pshift, c.refid, c.reftrnid, c.reftrnno, c.reftrntable, c.shiftdate "
            + " ORDER BY max(c.id) DESC ", nativeQuery = true)
    Page<DWagesSalaryList> findBy(@Param("branchId") Long branchId,
                                  @Param("deptId") Long deptId,
                                  @Param("shiftName") String shiftName,
                                  @Param("shiftId") Long shiftId,
                                  @Param("shiftDate") Date shiftDate,
                                  @Param("trnNo") String trnNo,
                                  @Param("trnName") String trnName,
                                  @Param("trnType") String trnType,
                                  Pageable pageable);


    @Query(value = "SELECT * from cwagessum c "
            + " WHERE (:branchId is null or c.branch =:branchId ) "
            + " AND  (:deptId is null or c.dept =:deptId ) "
            + " AND  (:trnNo is null or c.trnno =:trnNo ) LIMIT 1", nativeQuery = true)
    Optional<CwagesSUM> findBy(@Param("branchId") Long branchId,
                               @Param("deptId") Long deptId,
                               @Param("trnNo") String trnNo);


    @Query(value = " SELECT c.id as id, c.shiftdate as shiftDate, c.trnno as trnNo , c.branch as branch, b.name as branchName , c.dept as dept ," +
            " dd.name as deptName , c.pshift as pshiftId , p.name as pshiftName ,  c.trndate as trnDate , c.trnqty  as trnQty ," +
            " c.amt  as amount , c.reftrntable as transactiontable, c.cby as createdBy , ed.employeename as createdByName,  c.created_date as createdDate" +
            " from cwagessum c left join branch b on c.branch  = b.id left join employee_details ed on ed.id = c.cby" +
            " left join department_details dd on dd.id = c.dept left join pshifts p on p.id = c.pshift  " +
            " WHERE ( :trnNo is null OR c.trnno =:trnNo ) AND (:branch is null OR c.branch=:branch) AND (:dept is null OR c.dept=:dept)" +
            " AND ( cast(:shiftDate  as date) is null OR c.shiftdate =:shiftDate ) LIMIT 1 ", nativeQuery = true)
    PrintWagesSalaryVoucher findBy(@Param("trnNo") String trnNo,
                                   @Param("shiftDate") Date shiftDate,
                                   @Param("branch") Long branch,
                                   @Param("dept") Long dept);


    @Query(value = " SELECT coalesce(SUM(c.trnqty), 0.0)  as qty from cwagessum c " +
            "             WHERE (:productId is null or c.refid =:productId ) " +
            "             AND  (:trnNo is null or c.reftrnno =:trnNo ) " +
            "             AND  (:expref is null or c.expref =:expref ) " +
            "             AND  (:lot is null or c.lot =:lot )  ", nativeQuery = true)
    Double findBy(@Param("productId") Long productId,
                  @Param("trnNo") String trnNo,
                  @Param("lot") Double lot,
                  @Param("expref") String expref);


    @Query(value = " SELECT COALESCE( SUM(c.qty), 0.0) as qty FROM cwagessum c " +
            " WHERE (:trnNo IS NULL OR c.reftrnno = :trnNo ) " +
            " AND (:expref IS NULL OR c.expref = :expref) " +
            " AND (:itemId IS NULL OR c.refid = :itemId) " +
            " AND  (:lot IS NULL OR c.lot =:lot ) " +
            " group by c.refid , c.reftrnno , c.reftrnid, c.lot ", nativeQuery = true)
    Double sumOfAllocatedQty(@Param("trnNo") String trnNo,
                             @Param("itemId") Long itemId,
                             @Param("lot") Double lot,
                             @Param("expref") String expref);
}
