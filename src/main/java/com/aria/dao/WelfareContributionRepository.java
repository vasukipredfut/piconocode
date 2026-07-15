package com.aria.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.WelfareContribution;

@Repository
public interface WelfareContributionRepository extends JpaRepository<WelfareContribution, Long> {
	@Query(value = "SELECT  * FROM welfare_contribution w WHERE w.from_date =:fromDate and w.to_date =:toDate", nativeQuery = true)
    List<WelfareContribution> findAllDates(@Param("fromDate") Date fromDate,@Param("toDate") Date toDate);
}
