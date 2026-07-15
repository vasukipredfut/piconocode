package com.aria.dao;

import com.aria.dto.GDCDetailsList;
import com.aria.dto.GDCItemsList;
import com.aria.dto.GDCList;
import com.aria.model.MaterialGDC;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface MaterialFlowGDCRepository extends JpaRepository<MaterialGDC,Long> {


    @Query(value = " select oi.gdc_number as gdcNo, max(oi.gdc_date) as gdcDate, oi.sender_by_branch_id as issueBranchId, max(ib.name) as issueBranchName,   " +
            " oi.sender_by_department as issueDeptId,  max(id.name) as issueDeptName, oi.created_by_department_id as reqDeptId, " +
            " max(rd.name) as reqDeptName,  oi.created_by_branch_id as requestBranchId, max(rb.name) as requestBranchName " +
            " FROM material_flow_gdc oi left join branch rb on rb.id = oi.created_by_branch_id left join branch ib on ib.id = oi.sender_by_branch_id " +
            " left join department_details rd on rd.id = oi.created_by_department_id left join department_details id on id.id = oi.sender_by_department " +
            " WHERE (:gdcNo is null or oi.gdc_number=:gdcNo ) " +
            " AND (:issueBranchId is null or oi.sender_by_branch_id=:issueBranchId) "+
            " AND (:firmId is null or oi.gdc_Company_Id =:firmId ) "+
            " AND (cast(:dateFrom as date) is null or ( DATE(oi.gdc_Date) >= DATE(:dateFrom) ) ) " +
            " AND (cast(:dateTo as date) is null or ( DATE(oi.gdc_Date) <= DATE(:dateTo) ) ) " +
            " group by oi.gdc_number , oi.sender_by_branch_id, oi.created_by_branch_id, oi.sender_by_department, oi.created_by_department_id  ORDER BY max(oi.material_Flow_Gdc_Id) DESC ", nativeQuery = true)
    Page<GDCList> gdcList(@Param("gdcNo") String gdcNo, @Param("issueBranchId") Long issueBranchId,
                          @Param("firmId") Long firmId, @Param("dateFrom") Date dateFrom,
                          @Param("dateTo") Date dateTo, Pageable pageable);


    @Query(value = " SELECT  mg.gdc_number as gdcNo , mg.sender_by_branch_id as issueBranchId , mg.created_by_branch_id as requestBranchId ,   " +
            " max(obi.request_id) as requestId , max(obi.issue_id) as issueId , max(mg.sender_by_id) as senderId , " +
            " max(obi.transport_mode) as transportMode , max(obi.updated_by) as updatedBy,  " +
            " max(obi.delivery_user_id) as deliveryUserId , max(obi.issue_mode) as issueMode , max(obi.transport_vehicle_no) truckNo , " +
            " max(obi.transport_vehicle_type) truckType, max(obi.dispatch_on) as dispatchOn , max(gdc_Date) as gdcDate " +
            " FROM material_flow_gdc mg LEFT JOIN other_branch_issue obi on obi.gdc_no = mg.gdc_number AND obi.issue_branch_id=mg.sender_by_branch_id " +
            " AND obi.request_branch_id =mg.created_by_branch_id   " +
            " WHERE obi.gdc_no = mg.gdc_number AND obi.issue_branch_id=mg.sender_by_branch_id " +
            " AND obi.request_branch_id =mg.created_by_branch_id  " +
            " AND (:gdcNo is null or mg.gdc_number = :gdcNo ) " +
            " AND (:issueBranchId is null or mg.sender_by_branch_id = :issueBranchId ) " +
            " AND (:reqBranchId is null or mg.created_by_branch_id = :reqBranchId ) " +
            " GROUP BY mg.gdc_number  , mg.sender_by_branch_id , mg.created_by_branch_id order by max(material_flow_gdc_id) DESC limit 1  ", nativeQuery = true)
    GDCDetailsList gdcDetails(@Param("gdcNo") String gdcNo,
                              @Param("issueBranchId") Long issueBranchId,
                              @Param("reqBranchId") Long reqBranchId 
                              );

    @Query(value = " SELECT ii.item_id as itemId, ii.item_name as itemName, ii.allocated_quantity as allocated_quantity,  " +
            " ii.select_lot as lot , ii.issued_from_batch as batch, issued_from_bin as binName " +
            " from other_branch_issue obi left join issue_items ii on ii.issue_item_id  = obi.issue_id  " +
            " left join products_master pm  on pm.id  = ii.item_id  " +
            " WHERE (:gdcNo is null or obi.gdc_no = :gdcNo ) " +
            " AND (:issueBranchId is null or obi.issue_branch_id = :issueBranchId ) " +
            " AND (:reqBranchId is null or obi.request_branch_id = :reqBranchId ) ", nativeQuery = true)
    List<GDCItemsList> gdcItemDetails(@Param("gdcNo") String gdcNo,
                                      @Param("issueBranchId") Long issueBranchId,
                                      @Param("reqBranchId") Long reqBranchId );

}
