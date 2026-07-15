package com.aria.dao;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.dto.ComparisonInspectionData;
import com.aria.dto.RoqCommentList;
import com.aria.model.InwardCheck;

@Repository
public interface InwardCheckRepository extends JpaRepository<InwardCheck, Long> {

	@Query(value = "SELECT * FROM inward_check ie WHERE " + " (:batchNo is null or ie.batch_no =:batchNo) "
			+ "AND (:productId is null or ie.product_id =:productId) "
			+ "AND (cast(:inwardDate as timestamp) is null or (ie.inward_date =:inwardDate))", nativeQuery = true)
	Optional<InwardCheck> findByInwardCheck(@Param("batchNo") String batchNo, @Param("productId") Long productId,
			@Param("inwardDate") Date inwardDate);

	@Query(value = "SELECT * FROM inward_check ie WHERE (:batchNo is null or ie.batch_no =:batchNo) AND (:entryType is null or ie.recheck_log =:entryType) ", nativeQuery = true)
	Optional<InwardCheck> findByInwardCheck(@Param("batchNo") String batchNo, @Param("entryType") String entryType);

	@Query(value = "SELECT * FROM inward_check ie WHERE (:batchNo is null or ie.batch_no =:batchNo)  ", nativeQuery = true)
	Optional<InwardCheck> findByInwardCheck(@Param("batchNo") String batchNo);

	@Query(value = "select  i.batch_no as batchNo,recheck_log as entryType  from inward_check i left join direct_purchase_stored_product d on d.batch_no =i.batch_no  where d.camp_id=:campId and status ='Completed' and i.id <=:id limit 5  ", nativeQuery = true)
	List<ComparisonInspectionData> findByInwardCheckCamp(@Param("campId") Long campId, @Param("id") long id);

	@Query(value = "SELECT i.batch_no AS batchNo, i.recheck_log AS entryType FROM inward_check i LEFT JOIN direct_purchase_stored_product d ON d.batch_no =i.batch_no LEFT JOIN direct_purchase dp ON d.entry_id = dp.id LEFT JOIN camp c ON dp.camp_id = c.id WHERE dp.camp_id = :campId AND dp.stock_status = 'Completed' AND i.product_id = :productId ORDER BY dp.created_date DESC LIMIT 5", nativeQuery = true)
	List<ComparisonInspectionData> findByInwardCheckCampId(@Param("campId") Long campId,
			@Param("productId") Long productId);

	@Query(value = "select rc.id as id, rc.name as name from roqcomment rc", nativeQuery = true)
	List<RoqCommentList> findRoqComment();

}