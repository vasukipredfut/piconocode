package com.aria.dao;

import com.aria.dto.MaterialReturnList;
import com.aria.model.MaterialReturn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface MaterialReturnRepository extends JpaRepository<MaterialReturn, Long> {

    @Query(value = " SELECT  mr.id as id,  ri.item_name as itemName,  mr.to_reach as toReach, mr.to_reach_id as toReachId, mr.to_reach_name as toReachName, " +
            "mr.issue_date as issueDate, mr.dispatch_date as dispatchDate, mr.from_dept_id as requestDeptId, mr.from_dept_name as requestDeptName, " +
            "mr.request_date as requestDate,  ri.quantity as quantity , mr.from_name as requestFrom , mr.from_id as requestFromId , ri.id as returnId " +
            "FROM material_return mr  left join material_issue mi on mi.id  = mr.issue_id " +
            "left join return_items ri  on ri.return_id = mr.id " +
            "WHERE   ri.status = 'Pending'   and   ri.item_name IS NOT NULL     " +
            "AND (:firmId is null or mr.org_id=:firmId)  " +
            "AND (:issueDept is null or mi.issue_dept_id=:issueDept)  " +
            "AND (:issuebranch is null or mi.issue_branch_id =:issuebranch)" +
            "AND (:itemName is null or (lower(ri.item_name) LIKE ('%' || lower(:itemName) || '%')) )" +
            "AND (:requestBranchName is null or (lower(mr.to_reach_name) LIKE ('%' || lower(:requestBranchName) || '%')) ) " +
            "AND (:requestDeptName is null or (lower(mr.from_dept_name) LIKE ('%' || lower(:requestDeptName) || '%')) ) " +
            "AND (mr.issue_date BETWEEN  cast(:dateFrom  as date)  AND cast(:dateTo  as date) OR (cast(:dateFrom as date) is null OR cast(:dateTo as date) is null) )" +
            "ORDER BY ri.id DESC", nativeQuery = true)
    Page<MaterialReturnList> materialReturnList(@Param("firmId") Long firmId ,
                                                @Param("requestDeptName") String requestDeptName,
                                                @Param("requestBranchName") String requestBranchName,
                                                @Param("itemName") String itemName,
                                                @Param("dateFrom") Date dateFrom,
                                                @Param("dateTo") Date dateTo,
                                                @Param("issuebranch") Long issuebranch ,
                                                @Param("issueDept") Long issueDept ,
                                                Pageable pageable);


    @Query(value = "SELECT * FROM material_return ob WHERE ob.id =:issueId LIMIT 1", nativeQuery = true)
    Optional<MaterialReturn> findBy(@Param("issueId") Long issueId);


    @Query(value = "SELECT * FROM material_return mr WHERE mr.sent_external_id =:sentId Limit 1", nativeQuery = true)
    Optional<MaterialReturn> findBySendExternalId(@Param("sentId") Long sentId);


    @Query(value = "SELECT * FROM material_return mr WHERE mr.external_godown_id =:godownId Limit 1", nativeQuery = true)
    Optional<MaterialReturn> findByGodownId(@Param("godownId") Long godownId);

}
