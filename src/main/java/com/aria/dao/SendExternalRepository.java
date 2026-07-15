package com.aria.dao;



import com.aria.dto.SendExternalEntityList;
import com.aria.model.SendExternalEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.Date;

@Repository
public interface SendExternalRepository extends JpaRepository<SendExternalEntity, Long> {

    @Query(value = " SELECT se.material_flow_send_external_id as materialFlowSendExternalId , se.to_Reach as toReach , se.created_by as createdBy , max(ed.employeename) as createdByName , "
            + " se.reference_date as referenceDate , string_agg(ei.send_external_item_name ,' ,')  as itemNames  , max(fb.name) as fromBranchName , se.status as status "
            + " FROM material_flow_send_external se left join material_flow_send_external_items ei on ei.send_external_ref_id = se.material_flow_send_external_id "
            + " left join employee_details ed on ed.id = se.created_by left join branch fb on fb.id = se.created_branch_id  "
            + " WHERE (:issueBy is null or lower(ed.employeename) like ('%' || lower(:issueBy) || '%')) "
            + " AND (:toReach is null or lower(se.to_Reach) like ('%' || lower(:toReach) || '%')) "
            + " AND (:fromBranchId is null or fb.id=:fromBranchId ) "
            + " AND ( :firmId is null or se.firm_id=:firmId ) "
            + " AND (:fromBranch is null or lower(fb.name) like ('%' || lower(:fromBranch) || '%')) "
            + " AND (:name is null or lower(ei.send_external_item_name) like ('%' || lower(:name) || '%')) "
            + " AND (cast(:dateFrom as timestamp) is null or (se.reference_date >= :dateFrom)) "
            + " AND (cast(:dateTo as timestamp) is null or (se.reference_date <= :dateTo)) "
            + " group by se.material_flow_send_external_id , se.created_by , se.to_Reach ORDER BY se.material_flow_send_external_id desc", nativeQuery = true)
    Page<SendExternalEntityList> getAll(@Param("dateFrom") Date dateFrom,
                                        @Param("dateTo") Date dateTo,
                                        @Param("toReach") String toReach,
                                        @Param("name") String name,
                                        @Param("issueBy") String issueBy,
                                        @Param("fromBranch") String fromBranch,
                                        @Param("fromBranchId") Long fromBranchId,
                                        @Param("firmId") Long firmId,
                                        Pageable pageable);

}
