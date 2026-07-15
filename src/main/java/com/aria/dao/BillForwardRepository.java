package com.aria.dao;

import com.aria.model.BillForward;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface BillForwardRepository extends JpaRepository<BillForward, Long> {

    @Query(value = "SELECT bf.* FROM bill_forward bf LEFT JOIN branch inb ON inb.id = bf.from_branch_id  LEFT JOIN branch tob ON tob.id = bf.to_branch_id  LEFT JOIN  organisation org on org.id = bf.firm_org_id "
            + " WHERE (:firmName is null or lower(org.name) like ('%' || lower(:firmName) || '%'))"
            + " AND (:firmId is null or bf.firm_org_id = :firmId) "
            + " AND (:billNumber is null or lower(bf.bill_number) like ('%' || lower(:billNumber) || '%')) "
            + " AND (:fromBranchId is null or inb.id = :fromBranchId) "
            + " AND (:fromBranchName is null or lower(inb.name) like ('%' || lower(:fromBranchName) || '%')) "
            + " AND (:toBranchId is null or tob.id = :toBranchId) "
            + " AND (:toBranchName is null or lower(tob.name) like ('%' || lower(:toBranchName) || '%')) "
            + " AND (:status is null or bf.status = :status) "
            + " AND (cast(:dateFrom as timestamp) is null or (bf.forward_date >= :dateFrom)) "
            + " AND (cast(:dateTo as timestamp) is null or (bf.forward_date <= :dateTo)) order by bf.bill_date desc", nativeQuery = true)
    Page<BillForward> filterBillForwardList(@Param("firmId") Long firmId, @Param("firmName") String firmName,
                                            @Param("billNumber") String billNumber,
                                            @Param("fromBranchId") Long fromBranchId,
                                            @Param("fromBranchName") String fromBranchName,
                                            @Param("toBranchId") Long toBranchId,
                                            @Param("toBranchName") String toBranchName,
                                            @Param("status") String status,
                                            @Param("dateFrom") Date dateFrom,
                                            @Param("dateTo") Date dateTo,
                                            Pageable pageable);


    @Query(value = "SELECT  *  FROM bill_forward b WHERE b.bill_ref_id =:billInwardId", nativeQuery = true)
    Optional<BillForward> findByBillInwardId(@Param("billInwardId") Long billInwardId);


    @Query(value = "SELECT  *  FROM bill_forward b order by b.bill_date desc", nativeQuery = true)
    Page<BillForward> findAll(Pageable pageable);


    @Transactional
    @Modifying
    @Query(value = "update bill_forward set trn_no=:trnNo where id in (:ids) and status ='BillForwarded' ", nativeQuery = true)
    public void updateTrnNo(@Param("trnNo") Long trnNo, @Param("ids") List<Long> ids);


	@Query(value = "select * from bill_forward where id in (:ids) and status ='BillForwarded' ", nativeQuery = true)
	public List<BillForward> forwardList(@Param("ids") List<Long> ids);

}

