package com.aria.dao;

import com.aria.dto.DirectPurchaseDetails;
import com.aria.model.DirectPurchase;
import com.aria.model.DirectPurchaseList;
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
public interface DirectPurchaseRepository extends JpaRepository<DirectPurchase, Long> {


    @Query(value = "SELECT dp  FROM DirectPurchase dp  WHERE "
            + " (:billno is null or dp.referenceNo = :billno) "
            + "AND (:branchId is null or dp.branch.id = :branchId) "
            + "AND (dp.storageDate BETWEEN (cast(:fromDate as timestamp)) AND (cast(:toDate as timestamp)) )")
    Page<DirectPurchase> getListDirectPurchase(@Param("billno") String billno, @Param("branchId") Long branchId, @Param("fromDate") Date fromDate, @Param("toDate") Date toDate, Pageable pageable);


    @Query(value = "SELECT dp.id as id, dp.product_category as productCategory, dp.vendor_id, l.name as vendorName, "
            + " dp.reference_no as billNumber, dp.bill_amount as billAmount, dp.stock_status as status, dp.inward_date as inwardDate, "
            + " bi.lorry_without_Weight as lorryWithoutWeight FROM direct_purchase dp left join  ledger l on l.id=dp.vendor_id "
            + " LEFT JOIN bill_inward bi on bi.id = dp.bill_ref_id LEFT JOIN department_details dd on dd.id = dp.hand_over_to_dept_id  "
            + " WHERE (:billno is null or (lower(dp.reference_no) like ('%' || lower(:billno) || '%')) ) "
            + " AND (:productCategory is null or (lower(dp.product_category) like ('%' || lower(:productCategory) || '%')) ) "
            + " AND (:orgId is null or dp.firm_org_id = :orgId) "
            + " AND (:branchId is null or dp.branch_id = :branchId) "
            + " AND (:deptId is null or dp.hand_over_to_dept_id = :deptId) "
            + " AND (:status is null or (lower(dp.stock_status) LIKE ('%' || lower(:status) || '%')) ) "
            + " AND (:vendorId is null or dp.vendor_id = :vendorId) "
            + " AND (:vendorName is null or (lower(l.name) LIKE ('%' || lower(:vendorName) || '%')) ) "
            + " AND ( (cast(:fromDate as timestamp)) is null OR (dp.inward_date >= :fromDate ) ) "
            + " AND ( (cast(:toDate as timestamp)) is null OR (dp.inward_date <= :toDate ) ) ORDER BY dp.id DESC", nativeQuery = true)
    Page<DirectPurchaseList> getListDirectPurchase(@Param("productCategory") String productCategory,
                                                   @Param("billno") String billno,
                                                   @Param("vendorId") Long vendorId,
                                                   @Param("vendorName") String vendorName,
                                                   @Param("status") String status,
                                                   @Param("orgId") Long orgId,
                                                   @Param("branchId") Long branchId,
                                                   @Param("deptId") Long deptId,
                                                   @Param("fromDate") Date fromDate,
                                                   @Param("toDate") Date toDate,
                                                   Pageable pageable);
    /*
    
    @Query(value = "SELECT dp.id as id, dp.vendor_id, l.name as vendorName, dp.reference_no as billNumber, dp.bill_amount as billAmount, dp.inward_date as inwardDate, dp.bill_ref_id  as billInwardId , dp.lorry_date as lorryDate,  dp.bill_date as billDate,  l.address as address, l.city as city, l.state as state, l.gst as gstNumber, dp.sub_total_amount, "
    		+ "dp.vendor_id as vendor  FROM direct_purchase dp, ledger l  WHERE "
 +" l.id=dp.vendor_id AND dp.id=:id", nativeQuery = true)
    DirectPurchaseList getDirectPurchaseListbyId(@Param("id") Long id);*/


    @Query(value = "SELECT  " +
            " dp.id as id, dp.vendor_id, dp.reference_no as billNumber, dp.product_category as productCategory,     " +
            " dp.bill_amount as billAmount, dp.inward_date as inwardDate,  dp.lorry_date as lorryDate,  " +
            " dp.bill_date as billDate, dp.sub_total_amount, dp.vendor_id as vendor , " +
            " l.name as vendorName, l.state as state, l.gst as gstNumber, l.address as address, l.city as city,   " +
            " bi.id  as billInwardId , bi.bill_freight_amount  as billFrightAmount , bi.eway_bill as ewayBill , " +
            " bi.lorry_without_weight  as lorryWithoutWeight,  bi.lorry_with_weight as lorryWithWeight,  " +
            " bi.lorry_serial_no as lorrySerialNo, bi.lorry_freight as lorryFreight,  " +
            " bi.dedlorryfreight   as dedLorryfreight, bi.lorryfreight  as lorryfreightValue,  " +
            " bi.tcs  as tcs, bi.tcsper  as tcsper, bi.cess_tax  as cessTax , " +
            " bi.round_of_type   as roundOfType, bi.round_of_value   as roundOfValue, " +
            " bi.insurance  as insurance , bi.lorryno  as lorryno, " +
            " bi.created_date  as created_date, ed.employeename   as created_byname ,  " +
            " bi.updated_date   as updated_date, ed1.employeename   as updated_byname  , " +
            " stock.employeename  as stockInwardperson  , " +
            " bi.igst as igst, bi.sgst as sgst, bi.cgst as cgst , " +
            " bi.igstper as  igstper, bi.sgstper as  sgstper , bi.cgstper as cgstper " +
            " FROM  " +
            " direct_purchase dp  " +
            " left join ledger l on l.id=dp.vendor_id  " +
            " left join bill_inward bi on bi.id= dp.bill_ref_id  " +
            " left join employee_details ed on ed.id= bi.created_by  " +
            " left join employee_details ed1 on ed1.id= bi.updated_by   " +
            " left join employee_details stock on stock.id= dp.updated_by   " +
            " WHERE dp.id=:id", nativeQuery = true)
    DirectPurchaseList getDirectPurchaseListbyId(@Param("id") Long id);


    @Query(value = "SELECT   " +
            "  dp.id as id, dp.reference_no as referenceNo, b.id as branchId , b.name as branchName ," +
            "  dp.storage_date as storageDate ,  " +
            "  dp.updated_by  as updatedBy,  dp.updated_date as stockInwardByDate, ed.employeename as stockInwardBy , " +
            "  dp.created_by as createdBy , dp.created_date as stockInwardByDate1, ed1.employeename as stockInwardBy1 , " +
            "  dp.vendor_id as vendorId ,  dp.product_category as productCategory,   " +
            "  dp.inward_date as inwardDate,  dp.lorry_date as lorryDate, dp.bill_date as billDate, " +
            "  dp.bill_amount as billAmount,  dp.lorry_freight_amount as lorryFreightAmount,  dp.stock_status  as stockStatus ," +
            "  dp.approved_by  as approvedBy,  dp.approved__date as approvedDate, approve.employeename as approvedByName ," +
            "  dp.authorised_by   as authorisedBy,  dp.authorised_date  as authorisedDate, author.employeename as authorisedByName ," +
            "  dp.verified_by  as verifiedBy,  dp.verified_date  as verifiedDate, verify.employeename as verifiedByName ," +
            "  dp.rejected_by   as rejectedBy,  dp.rejected_date   as rejectedDate, reject.employeename as rejectedByName ," +
            "  c.id as campId, c.camp as campName , dp.excess_shortage as excessOrShortage , dp.credit_or_debit as creditOrDebit ," +
            "  dp.etransfer  as eTransfer , dd.id as handOverToDepartment, dd.name as handOverToDepartmentName , " +
            "  dp.firm_org_id as firmOrgId , bi.id as billRefId " +
            "  FROM   " +
            "  direct_purchase dp   " +
            "  left join branch b  on b.id = dp.branch_id   " +
            "  left join department_details dd on dd.id = dp.hand_over_to_dept_id    " +
            "  left join employee_details ed on ed.id= dp.updated_by    " +
            "  left join employee_details ed1 on ed1.id= dp.created_by    " +
            "  left join employee_details approve on approve.id= dp.approved_by  " +
            "  left join employee_details author on author.id= dp.authorised_by " +
            "  left join employee_details verify on verify.id= dp.verified_by " +
            "  left join employee_details reject on reject.id= dp.rejected_by  " +
            "  left join camp c on c.id= dp.camp_id  " +
            "  left join bill_inward bi on bi.id= dp.bill_ref_id " +
            "  left join ledger l on l.id=dp.vendor_id   " +
            "  WHERE dp.id=:id ", nativeQuery = true)
    DirectPurchaseDetails getDirectPurchaseDetailsById(@Param("id") Long id);


    @Query(value = "SELECT dp  FROM DirectPurchase dp  WHERE "
            + " (:billno is null or dp.referenceNo = :billno) "
            + "AND (:branchId is null or dp.branch.id = :branchId)")
    Page<DirectPurchase> getListDirectPurchase(@Param("billno") String billno, @Param("branchId") Long branchId, Pageable pageable);

    @Query(value = "SELECT *  FROM direct_purchase dp  WHERE dp.reference_no =:billno AND dp.vendor_id =:vendorId ORDER BY dp.id DESC LIMIT 1 ", nativeQuery = true)
    Optional<DirectPurchase> findByDirectPurchase(@Param("billno") String billno, @Param("vendorId") Long vendorId);

    @Query(value = "SELECT *  FROM direct_purchase dp  WHERE dp.reference_no =:billno AND dp.bill_Ref_Id =:billRefId", nativeQuery = true)
    Optional<DirectPurchase> findBy(@Param("billno") String billno, @Param("billRefId") Long billRefId);


    @Query(value = "SELECT *  FROM direct_purchase dp  WHERE  dp.bill_Ref_Id =:billRefId LIMIT 1", nativeQuery = true)
    Optional<DirectPurchase> findBy(@Param("billRefId") Long billRefId);


    @Query(value = "select * from direct_purchase dp where dp.id not in " +
            " ( select distinct  dps.entry_id  from direct_purchase_stored_product dps ) ORDER BY dp.id ASC ", nativeQuery = true)
    List<DirectPurchase> findAllIssues();


}
