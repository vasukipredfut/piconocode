package com.aria.dao;

import java.util.List;

import com.aria.dto.WagesJournalWork;
import org.springframework.data.jpa.repository.JpaRepository;

import com.aria.model.RequestGodownItem;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RequestGodownItemRepository extends JpaRepository<RequestGodownItem, Long> {

	List<RequestGodownItem> findByGoDownExternalId(Long id);


	@Query(value = "SELECT * FROM request_items_godown ob WHERE ob.godown_external_id =:goDownExternalId AND ob.material_return='YES' ", nativeQuery = true)
	List<RequestGodownItem> findBy(@Param("goDownExternalId") Long goDownExternalId);


	@Query(value = "SELECT * FROM request_items_godown ob WHERE ob.godown_external_id =:goDownExternalId AND ob.item_id=:itemId AND ob.select_Lot=:lot ", nativeQuery = true)
	List<RequestGodownItem> findBy(@Param("goDownExternalId") Long goDownExternalId, @Param("itemId") Long itemId , @Param("lot") String lot);


	@Query(value = "SELECT s.item_id as productId ,  max(s.item_name) as productName , s.select_lot as lot, g.trn_no as trnNo,  " +
			"SUM( cast(s.allocated_quantity as DOUBLE PRECISION) ) as qty " +
			"FROM godownexternal g LEFT JOIN request_items_godown s ON g.id = s.godown_external_id " +
			"WHERE (:trnNo is null or g.trn_no = :trnNo ) AND s.item_id IS NOT NULL " +
			"AND (s.sal_status IS NULL OR s.sal_status <>'Completed' ) " +
			"group by s.item_id , g.trn_no , s.select_lot  ", nativeQuery = true)
	List<WagesJournalWork> getGodownItem(@Param("trnNo") String trnNo);;
}
