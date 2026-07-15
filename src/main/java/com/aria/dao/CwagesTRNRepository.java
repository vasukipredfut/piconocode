package com.aria.dao;

import com.aria.dto.AssignUserListForSal;
import com.aria.dto.CwagesEmployees;
import com.aria.dto.CwagesTRNSumList;
import com.aria.dto.PrintWagesSalaryVoucher;
import com.aria.model.CwagesDetails;
import com.aria.model.CwagesTRN;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;


@Repository
public interface CwagesTRNRepository extends JpaRepository<CwagesTRN, Long> {

    @Query(value = "select c.empcode as empId , sum(c.amt) as amount from cwagestrn c where c.empcode=:empcode AND ( cast(:shiftDate  as date) is null OR (c.shiftdate =:shiftDate) ) AND  c.pshift=:shiftId  group by c.empcode,  c.shiftdate", nativeQuery = true)
    CwagesTRNSumList findBy(@Param("empcode") Long empcode, @Param("shiftId") Long shiftId, @Param("shiftDate") Date shiftDate);

    @Query(value = "select * from cwagestrn c where  ( cast(:shiftDate  as date) is null OR (c.shiftdate =:shiftDate) ) AND c.empcode=:empcode   order by c.id desc limit 1", nativeQuery = true)
    CwagesTRN findBy(@Param("shiftDate") Date shiftDate, @Param("empcode") Long empcode);


    @Query(value = "select COUNT( distinct c.shiftdate)  as count, sum(c.amt)  as amount from cwagestrn c where "
            + " c.empcode =:userId  "
            + " AND ( c.shiftdate BETWEEN cast(:fromDate  as date) AND cast(:toDate  as date) ) "
            + " GROUP BY  c.empcode", nativeQuery = true)
    AssignUserListForSal findUserContractSalCount(@Param("userId") Long userId,
                                                  @Param("fromDate") Date fromDate,
                                                  @Param("toDate") Date toDate);

    @Query(value = "SELECT * from cwagestrn c "
            + " WHERE (:trnId is null or c.trnid =:trnId ) "
            + " AND  (:trnNo is null or c.trnno =:trnNo ) ",nativeQuery = true)
    List<CwagesTRN> findBy(@Param("trnId") Long trnId, @Param("trnNo") String trnNo);



    @Query(value = " select  c.empcode as empId , ed.name as employeeName , c.amt as amount from cwagestrn c left join emast_daily ed on ed.id = c.empcode   "
            + " WHERE (:trnId is null OR c.trnid =:trnId ) AND (:trnNo is null OR c.trnno =:trnNo ) AND  ( cast(:shiftDate as date) is null OR c.shiftdate =:shiftDate ) ", nativeQuery = true)
    List<CwagesEmployees> findByCwagesEmployees(@Param("trnId") Long trnId, @Param("trnNo") String trnNo, @Param("shiftDate") Date shiftDate );


}
