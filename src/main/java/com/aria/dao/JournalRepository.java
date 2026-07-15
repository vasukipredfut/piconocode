package com.aria.dao;

import com.aria.model.ProductMaster;
import com.aria.model.TransportTrip;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.dto.JournalList;
import com.aria.dto.JournalNameDto;
import com.aria.model.Journal;

import java.util.List;
import java.util.Optional;

@Repository
public interface JournalRepository extends JpaRepository<Journal, Long> {


    @Query(value = "SELECT id, name  FROM Journal jl WHERE jl.dept =:dept", nativeQuery = true)
    List<JournalList> getDropDownList(@Param("dept") Long dept);

    /*
    
    @Query(value = " SELECT jl.id as id, jl.name as name, jl.proid as productId, jl.dept as deptId, dp.name as deptName , " +
            " jbm.branch_id as branchId, jbm.branch_name as branchName   " +
            "  FROM Journal jl left join  journal_branch_map jbm on jbm.journal_id = jl.id left join department_details dp on dp.id = jl.dept " +
            " WHERE  jl.active_status='Y' and dp.org_id =:orgId and dp.id =:deptId  and jbm.branch_id =:branchId  ",nativeQuery = true)
    List<JournalNameDto> getJournalList(@Param("orgId") Long orgId, @Param("deptId") Long deptId, @Param("branchId") Long branchId);
*/
    @Query(value = " SELECT jl.id as id, jl.name as name, jl.proid as productId, jl.dept as deptId, dp.name as deptName , " +
            " jbm.branch_id as branchId, jbm.branch_name as branchName   " +
            "  FROM Journal jl left join  journal_branch_map jbm on jbm.journal_id = jl.id left join department_details dp on dp.id = jl.dept " +
            " WHERE  jl.active_status='Y' and dp.id =:deptId  and jbm.branch_id =:branchId  ",nativeQuery = true)
    List<JournalNameDto> getJournalList(@Param("deptId") Long deptId, @Param("branchId") Long branchId);

    @Query(value = "SELECT be.* FROM journal be LEFT JOIN products_master p ON p.id = be.proid LEFT JOIN department_details d ON d.id = be.dept "
            + " WHERE (be.active_Status=:status ) AND (:journalName is null or lower(be.name) like ('%' || lower(:journalName) || '%')) "
            + " AND (:batchNumber is null or lower(be.batchtype) like ('%' || lower(:batchNumber) || '%')) "
            + " AND (:processType is null or lower(be.type) like ('%' || lower(:processType) || '%')) "
            + " AND (:productName is null or lower(p.name) like ('%' || lower(:productName) || '%')) "
            + " AND (:department is null or lower(d.name) like ('%' || lower(:department) || '%')) "
            + " AND (:productvityType is null or lower(be.pvitytype) like ('%' || lower(:productvityType) || '%')) ORDER BY be.id DESC", nativeQuery = true)
    Page<Journal> filterJournalList(@Param("journalName") String journalName,
                                            @Param("batchNumber") String batchNumber, @Param("productvityType") String productvityType,
                                          @Param("processType") String processType, @Param("status") String status,
                                    @Param("productName") String productName, @Param("department") String department, Pageable pageable);



    @Query(value = "SELECT jl.id, jl.name , jl.batchproxid as batchProId, jl.stock_type as stockType  FROM Journal jl WHERE jl.blending=true", nativeQuery = true)
    List<JournalList> findBlendingEnabled();



    @Query(value = "SELECT id  FROM journal jl WHERE jl.proid=:proId", nativeQuery = true)
    List<Long> findByProduct(@Param("proId") Long proId);

    @Query(value = " SELECT jl.* FROM journal jl " +
            " left join journal_product_mapping jpm on jpm.journal_id  = jl.id " +
            " WHERE jpm.product_id =:proId AND jl.active_status='Y' Limit 1 ", nativeQuery = true)
    Optional<Journal> findBy(@Param("proId") Long proId);


    @Query(value = " SELECT * FROM journal jl " +
            " WHERE jl.proid =:proId AND jl.active_status='Y' Limit 1 ", nativeQuery = true)
    Optional<Journal> findByeProduct(@Param("proId") Long proId);



    @Query(value = " SELECT * FROM journal jl " +
            " WHERE jl.batchproxid =:proId AND jl.active_status='Y' Limit 1 ", nativeQuery = true)
    Optional<Journal> findBomProduct(@Param("proId") Long proId);


    @Query(value = " SELECT j.* from journal j " +
            " left join blending_advice_details ba on ba.journal_id = j.id " +
            " left join blending_advice_stock_details basd on basd.blending_advice_details_id = ba.id " +
            " where basd.id =:blendingAdviceStockId ", nativeQuery = true)
    Optional<Journal> findByBlendingStockId(@Param("blendingAdviceStockId") Long blendingAdviceStockId);

    @Query(value = " SELECT jl.id as id, jl.name as name, jl.proid as productId, jl.dept as deptId, dp.name as deptName , " +
            " jbm.branch_id as branchId, jbm.branch_name as branchName   " +
            " FROM Journal jl LEFT JOIN  asrs_journal_branch_map jbm ON jbm.journal_id = jl.id " +
            " LEFT JOIN department_details dp on dp.id = jl.dept " +
            " WHERE  jl.active_status='Y' AND jl.asrs = true " +
            " AND (:name is null or lower(jl.name) like ('%' || lower(:name) || '%')) " +
            " AND dp.id =:deptId  AND jbm.branch_id =:branchId  ", nativeQuery = true)
    List<JournalNameDto> getJournalList(@Param("name") String name, @Param("deptId") Long deptId, @Param("branchId") Long branchId);

}
