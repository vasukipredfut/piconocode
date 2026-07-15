
package com.aria.dao;

import java.util.List;

import jakarta.transaction.Transactional;

import com.aria.dto.IssueItemsBrief;
import com.aria.dto.IssueItemsValues;
import com.aria.dto.IssueQtyList;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.IssueItems;

@Repository
public interface IssueItemsRepository extends JpaRepository<IssueItems, Long> {

	@Query(value = "SELECT * FROM issue_items r WHERE r.issue_item_id =:issueItemId", nativeQuery = true)
	List<IssueItems> findAllByIssueItemId(@Param("issueItemId") Long issueItemId);

	@Query(value = "SELECT * FROM issue_items r WHERE r.issue_item_id =:issueItemId and r.item_id=:itemId", nativeQuery = true)
	List<IssueItems> findAllByIssueItemIdAndItem(@Param("issueItemId") Long issueItemId, @Param("itemId") Long itemId);

	@Query(value = "SELECT * FROM issue_items r WHERE r.issue_item_id =:issueItemId and r.item_id=:itemId and r.select_lot =:selectLot and r.active = 0 ", nativeQuery = true)
	List<IssueItems> findAllByIssueItemIdAndItemAndLot(@Param("issueItemId") Long issueItemId,
			@Param("itemId") Long itemId, @Param("selectLot") String selectLot);

	@Transactional
	@Modifying
	@Query(value = "DELETE FROM issue_items r WHERE r.issue_item_id =:issueItemId", nativeQuery = true)
	void deleteIssueItem(@Param("issueItemId") Long issueItemId);

	@Query(value = "SELECT * FROM issue_items r WHERE r.issue_item_id =:issueItemId and r.item_id=:itemId and (r.pending_Quantity IS NOT NULL AND r.pending_quantity not in ('0'))  ", nativeQuery = true)
	List<IssueItems> findBy(@Param("issueItemId") Long issueItemId, @Param("itemId") Long itemId);

	@Transactional
	@Modifying
	@Query(value = "SELECT item_id as item_id  , select_lot as select_lot , issue_item_id as issue_item_id, max(item_name) as item_name , max(requested_quantity) as requested_quantity, sum( CAST (allocated_quantity as DOUBLE PRECISION) )as allocated_quantity, max(pending_quantity) as pending_quantity  FROM issue_items where issue_item_id=:issueItemId and active = 0  group by item_id , select_lot , issue_item_id ", nativeQuery = true)
	List<IssueItemsValues> findBy(@Param("issueItemId") Long issueItemId);

	@Query(value = "SELECT COALESCE( SUM( CAST (ii.allocated_quantity as DOUBLE PRECISION) ),0.0) as quantity from material_issue mi left join issue_items ii on ii.issue_item_id = mi.id "
			+ " WHERE request_id = :requestId and issue_branch_id = :branch and issue_dept_id = :deptId AND ii.item_id =:itemId group by ii.item_id , ii.item_name ", nativeQuery = true)
	Double sumOfAllocatedQty(@Param("requestId") Long requestId, @Param("branch") Long branch,
			@Param("deptId") Long deptId, @Param("itemId") Long itemId);

	@Query(value = "SELECT COALESCE( SUM( CAST (ii.allocated_quantity as DOUBLE PRECISION) ),0.0) as quantity from material_issue mi left join issue_items ii on ii.issue_item_id = mi.id "
			+ " WHERE   ii.active = 0 and request_id = :requestId and issue_branch_id = :branch and issue_dept_id = :deptId "
			+ " AND ii.item_id =:itemId AND ii.select_Lot=:lot "
			+ " group by ii.item_id , ii.item_name ", nativeQuery = true)
	Double sumOfAllocatedQty(@Param("requestId") Long requestId, @Param("branch") Long branch,
			@Param("deptId") Long deptId, @Param("itemId") Long itemId, @Param("lot") String lot);

	@Query(value = "SELECT COALESCE( SUM( CAST (ii.allocated_quantity as DOUBLE PRECISION) ), 0.0) as quantity "
			+ " From material_issue mi left join issue_items ii on ii.issue_item_id = mi.id  "
			+ " WHERE mi.id = :issueId and ii.issued_from_batch = :batchNo and ii.select_lot = :lot "
			+ " AND ii.item_id =:itemId " + " group by ii.item_id , ii.item_name , ii.select_lot", nativeQuery = true)
	Double sumOfAllocatedQtyPending(@Param("issueId") Long issueId, @Param("batchNo") String batchNo,
			@Param("itemId") Long itemId, @Param("lot") String lot);

	@Query(value = " SELECT SUM( cast(t1.allocated_quantity as double precision) ) as allocatedQuantity , "
			+ " t1.issue , t1.issued_from_batch  , t1.issued_from_bin,  max(t1.requested_quantity ) as requestedQuantity , "
			+ " t1.select_lot  as lot , t1.item_id  as itemId , t1.item_name as itemName  from " + "   ( "
			+ "       SELECT  distinct ii.* from issue_items ii "
			+ "       left join material_issue mi on mi.id = ii.issue_item_id where  mi.request_id =:requestId "
			+ "   ) as t1 "
			+ " group by  t1.issue , t1.issued_from_batch  , t1.issued_from_bin, t1.select_lot , t1.item_id  , t1.item_name ", nativeQuery = true)
	List<IssueItemsBrief> findByBrief(@Param("requestId") Long requestId);

	@Query(value = "SELECT * FROM issue_items r WHERE r.issue_item_id =:issueItemId and r.active=0 ", nativeQuery = true)
	List<IssueItems> findAllByIssueItemIdANDActive(@Param("issueItemId") Long issueItemId);

    @Transactional
    @Modifying
	@Query(value = "UPDATE  issue_items r SET active=1 WHERE r.issue_item_id =:issueItemId and r.active=0 ", nativeQuery = true)
	void updateInActive(@Param("issueItemId") Long issueItemId);
    
    @Query(value = "SELECT " +
            "SUM(CAST(NULLIF(ii.allocated_quantity,'') AS DOUBLE PRECISION)) AS total_allocated, " +
            "MAX(CAST(NULLIF(ii.requested_quantity,'') AS DOUBLE PRECISION)) AS total_requested " +
            "FROM material_issue mi " +
            "LEFT JOIN issue_items ii ON ii.issue_item_id = mi.id " +
            "WHERE mi.request_id = :requestId",
            nativeQuery = true)
    IssueQtyList getQtySummary(@Param("requestId") Long requestId);
}
