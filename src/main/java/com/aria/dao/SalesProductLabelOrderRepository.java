package com.aria.dao;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.SalesProductLabel;
import com.aria.model.SalesProductLabelOrder;

@Repository
public interface SalesProductLabelOrderRepository extends JpaRepository<SalesProductLabelOrder, Long> {

	Optional<SalesProductLabelOrder> findByMrnNo(Long mrnNo);

	List<SalesProductLabelOrder> findByLabelIdAndLocationAndSubLocationIdAndOrderBranchAndLot(SalesProductLabel label,
			String location, Long subLocationId, Long branch, Long lot);

	@Query(value = "SELECT * from salesproductlabelorder s " +
			" WHERE ( cast(:oDate as date) IS NULL OR  (s.orderdate =:oDate)) " +
			" AND (:status IS NULL OR ( s.mrnstatus = :status)) ORDER BY s.id DESC ", nativeQuery = true)
	Page<SalesProductLabelOrder> findByFilter(@Param("status") String status, @Param("oDate") Date oDate, Pageable pageable);

	@Query(value = "SELECT * from salesproductlabelorder s " +
			" WHERE (cast(:oDate as date) IS NULL OR  (s.orderdate = :oDate)) " +
			" AND (:status IS NULL OR  (s.mrnstatus = :status)) ORDER BY s.id DESC ", nativeQuery = true)
	List<SalesProductLabelOrder> findByMrnStatusAndOrderDate(String status, Date oDate);

}
