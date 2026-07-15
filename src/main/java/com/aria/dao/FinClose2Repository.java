package com.aria.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.aria.model.FinClose2;

@Repository
public interface FinClose2Repository extends JpaRepository<FinClose2, Long> {

	 @Query(value = "SELECT  *  FROM finclose2 fc2 order by fc2.id  asc", nativeQuery = true)
	List<FinClose2> getFinishedGoodsClosedSummaryTableList();

}
