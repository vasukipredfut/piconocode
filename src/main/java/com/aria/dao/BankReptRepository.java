package com.aria.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.aria.dto.GoDownSummaryList;
import com.aria.model.StkTable;

@Repository
public interface BankReptRepository extends JpaRepository<StkTable, Long> {

	@Query(value = "SELECT s.refid as itemId, pm.name as itemName " +
            " FROM stktable s left join products_master pm on pm.id = s.refid  where s.year = :year AND s.month = :month "
            , nativeQuery = true)
	 List<GoDownSummaryList> findByProduct(Long year, Long month);

	@Query(value = "SELECT distinct pm.id as itemId, pm.name as itemName  FROM products_master pm  where pm.pcategory in ('AR','AF') "
            , nativeQuery = true)
	 List<GoDownSummaryList> findByProducts();
}
