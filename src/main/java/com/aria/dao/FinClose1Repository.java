package com.aria.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.aria.dto.FinishedGoodsSummary;
import com.aria.model.FinClose1;

@Repository
public interface FinClose1Repository extends JpaRepository<FinClose1, Long>{

	 @Query(value = "SELECT  fc2.id as orderNo , fc2.name as name   FROM finclose1 fc2 where fc2.parentid = :id order by fc2.id  asc", nativeQuery = true)
	List<FinishedGoodsSummary> getFinishedGoodsClosedSummaryProductNameList(Long id);

}
