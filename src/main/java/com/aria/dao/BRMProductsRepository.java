package com.aria.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.dto.DayEndStockList;
import com.aria.model.BRMProducts;

@Repository
public interface BRMProductsRepository extends JpaRepository<BRMProducts, Long> {


    @Query(value = "SELECT distinct id FROM brmproids b WHERE  "
            + " (:type is null or (b.pcategory =:type) ) "
            + " AND (:branch is null or (b.bid =:branch) ) "
            + " AND (:dept is null or (b.did =:dept) ) ", nativeQuery = true)
    List<Long> findBY(@Param("branch") Long branch,
                             @Param("dept") Long dept,
                             @Param("type") String type);


    @Query(value = "SELECT pm.name as itemName, pm.id as itemId, br.bid as branchId  "
            + " FROM brmproids br left join products_master pm on pm.id = br.id WHERE br.day_end_closing_flag = true "
            + " AND (:branch is null or (br.bid =:branch) ) order by pm.id ", nativeQuery = true)
    List<DayEndStockList> findBY(@Param("branch") Long branch);


    @Query(value = "select pm.id as itemId, pm.name as itemName from products_master pm WHERE pm.id in (:itemIds) ", nativeQuery = true)
    List<DayEndStockList> findBY(@Param("itemIds") List<Long> ids);
}
