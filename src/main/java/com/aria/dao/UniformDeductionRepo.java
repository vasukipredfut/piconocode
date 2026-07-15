package com.aria.dao;

import com.aria.dto.OtherDeductionData;
import com.aria.model.UniformDeduction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
@Repository
public interface UniformDeductionRepo extends JpaRepository<UniformDeduction, Long> {


    @Query(value = "select ud.empcode as empId, SUM(amount) as amt from uniform_deduction ud  WHERE ud.empcode=:userId AND ud.contable =:contable "
            + " AND (ud.created_time BETWEEN :fromDate AND :toDate) "
            + " GROUP BY ud.empcode ", nativeQuery = true)
    OtherDeductionData findBy(@Param("userId") Long userId,
                              @Param("contable") String contable,
                              @Param("fromDate") Date fromDate,
                              @Param("toDate") Date toDate);
}
