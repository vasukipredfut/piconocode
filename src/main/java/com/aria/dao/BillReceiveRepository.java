package com.aria.dao;

import com.aria.model.BillReceive;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BillReceiveRepository extends JpaRepository<BillReceive, Long> {

    @Query(value = "SELECT br.* FROM bill_receive br LEFT JOIN  organisation org on org.id = be.firm_org_id"
            + " WHERE (:firmId is null or br.firm_org_id =:firmId) "
            + " AND (:firmName is null or lower(org.name) like ('%' || lower(:firmName) || '%')) "
            + " AND (:billNumber is null or br.bill_Number = :billNumber) "
            + " AND (:fromBranchId is null or br.from_Branch.id = :fromBranchId) "
            + " AND (:toBranchId is null or br.received_Branch.id = :toBranchId) "
            + " AND (:status is null or br.status = :status) "
            + " AND (cast(:dateFrom as timestamp) is null or (br.received_Date >= :dateFrom)) "
            + " AND (cast(:dateTo as timestamp) is null or (br.received_Date <= :dateTo))", nativeQuery = true)
    Page<BillReceive> filterBillReceiveList(@Param("firmId") Long firmId, @Param("firmName") String firmName,
                                            @Param("billNumber") String billNumber,
                                            @Param("fromBranchId") Long fromBranchId, @Param("toBranchId") Long toBranchId,
                                            @Param("status") String status, @Param("dateFrom") Date dateFrom, @Param("dateTo") Date dateTo, Pageable pageable);
}
