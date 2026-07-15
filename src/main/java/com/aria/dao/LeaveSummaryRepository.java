package com.aria.dao;

import com.aria.model.BinRange;
import com.aria.model.LeaveSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveSummaryRepository extends JpaRepository<LeaveSummary,Long> {



    @Query(value = "SELECT  * FROM leave_summary l WHERE l.empcode =:empCode AND l.year=:year AND l.month =:month  ",nativeQuery = true)
    Optional<LeaveSummary> findBy(@Param("empCode") Long empCode, @Param("year") String year , @Param("month") String month);

}
