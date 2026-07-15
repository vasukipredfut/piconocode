package com.aria.dao;

import com.aria.model.BillExchange;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.aria.model.DirectPurchase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BillExchangeRepository extends JpaRepository<BillExchange, Long> {

	@Query(value = "SELECT be.* FROM bill_exchange be LEFT JOIN bill_inward bi on bi.id = be.bill_inward_id  "
			+ " LEFT JOIN branch b on b.id = bi.deliver_branch_id LEFT JOIN  organisation org on org.id = be.firm_org_id "
			+ " LEFT JOIN branch handOverToBranch on handOverToBranch.id = be.hand_over_to_branch_id "
			+ " WHERE (:firmId is null or be.firm_org_id =:firmId) "
			+ " AND (:firmName is null or lower(org.name) like ('%' || lower(:firmName) || '%')) "
			+ " AND (:billNumber is null or lower(be.bill_number) like ('%' || lower(:billNumber) || '%')) "
			+ " AND (:toBranchId is null or handOverToBranch.id = :toBranchId) "
			+ " AND (:toBranchName is null or lower(handOverToBranch.name)  like ('%' || lower(:toBranchName) || '%')) "
			+ " AND (:deliveryBranchId is null or b.id = :deliveryBranchId) "
			+ " AND (:deliveryBranchName is null or lower(b.name)  like ('%' || lower(:deliveryBranchName) || '%')) "
			+ " AND (:status is null or be.status = :status) "
			+ " AND (cast(:dateFrom as timestamp) is null or (be.bill_inward_date >= :dateFrom)) "
			+ " AND (cast(:dateTo as timestamp) is null or (be.bill_inward_date <= :dateTo)) ORDER BY be.bill_inward_date DESC " , nativeQuery = true)
	Page<BillExchange> filterBillExchangeList(@Param("firmId") Long firmId, @Param("firmName") String firmName,
											  @Param("billNumber") String billNumber,
											  @Param("toBranchId") Long toBranchId,
											  @Param("toBranchName") String toBranchName,
											  @Param("status") String status,
											  @Param("dateFrom") Date dateFrom,
											  @Param("dateTo") Date dateTo,
											  @Param("deliveryBranchId") Long deliveryBranchId,
											  @Param("deliveryBranchName") String deliveryBranchName,
											  Pageable pageable);




	@Query(value = "SELECT *  FROM bill_exchange be  WHERE  be.bill_inward_id =:billRefId LIMIT 1", nativeQuery = true)
	Optional<BillExchange> findBy(@Param("billRefId") Long billRefId);
}
