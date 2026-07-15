package com.aria.dao;

import java.util.List;
import java.util.Optional;


import com.aria.dto.POList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.dto.SupplierQty;
import com.aria.model.PurchaseOrder;
import com.aria.model.Vendor;


@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    @Query(value = "SELECT * FROM purchase_order po WHERE po.po_no = :poNumber", nativeQuery = true)
    List<PurchaseOrder> getPurchaseOrderFilterListByPONumber(@Param("poNumber") String poNumber);

    @Query(value = "SELECT * FROM purchase_order po WHERE po_number = :poNumber and po.vendor_id = :vendorId", nativeQuery = true)
    Page<PurchaseOrder> getPurchaseOrderFilterbyPOandVendor(@Param("poNumber") String poNumber,
                                                            @Param("vendorId") Vendor vendorId, Pageable pageable);

    @Query(value = "SELECT * FROM purchase_order po WHERE po.po_number = :poNumber", nativeQuery = true)
    Page<PurchaseOrder> getPurchaseOrderFilterbyPoId(@Param("poNumber") String poNumber, Pageable pageable);

    @Query(value = "SELECT * FROM purchase_order po WHERE po.vendor_id = :vendorId", nativeQuery = true)
    Page<PurchaseOrder> getPurchaseOrderFilterbyVendorId(@Param("vendorId") Vendor vendorId, Pageable pageable);

    @Query(value = "SELECT * FROM purchase_order", nativeQuery = true)
    Page<PurchaseOrder> getPurchaseOrderFilterbyPOandVendor(Pageable pageable);

    @Query(value = "SELECT * FROM purchase_order po WHERE po.vendor_id = :vendorId AND (po.po_status IS NULL OR po.po_status='Open') AND po.status ='Authorized'", nativeQuery = true)
    List<PurchaseOrder> getPurchaseOrderFilterListByVendorId(@Param("vendorId") Long vendorId);

    List<PurchaseOrder> findByPoNumberAndVendor(String poNumber, Vendor v);

    List<PurchaseOrder> findByPoNumber(String poNumber);

    @Query(value = "SELECT * FROM purchase_order po WHERE po.proposal_id = :proposalId", nativeQuery = true)
    List<PurchaseOrder> getPurchaseOrderFilterListByProposalId(@Param("proposalId") Long proposalId);

    @Query(value = "SELECT * FROM purchase_order po LEFT JOIN ledger l on l.id = po.vendor_id WHERE po.type_flag='Factory' "
            + " AND (:poNumber is null or lower(po.po_no) like ('%' || lower(:poNumber) || '%')) "
            + " AND (:vendorName is null or lower(l.name) like ('%' || lower(:vendorName) || '%')) "
            + " AND (:vendorId is null or po.vendor_id =:vendorId) " + " AND (:frimId is null or po.firm_id = :frimId) "
            + " AND (:dept is null or po.dept_id = :dept) " + " AND (:branch is null or po.branch_id = :branch) "
            + " AND (:status is null or po.status =:status) "
            + " AND (:proposalid is null or po.proposal_id =:proposalid) ORDER BY po.id DESC", nativeQuery = true)
    Page<PurchaseOrder> getPurchaseOrderFilterByPOAndVendorId(@Param("poNumber") String poNumber,
                                                              @Param("vendorId") Long vendorId, @Param("vendorName") String vendorName,
                                                              @Param("proposalid") Long proposalid, @Param("frimId") Long frimId, @Param("dept") Long dept,
                                                              @Param("branch") Long branch, @Param("status") String status, Pageable pageable);

    @Query(value = "SELECT * FROM purchase_order po WHERE po.ho_po_id =:poID", nativeQuery = true)
    Optional<PurchaseOrder> getPurchaseOrderByHoPOId(@Param("poID") Long poID);

    @Query(value = "select po.id as poId, po.po_no as poNo, po.po_date as poDate, poi.item_id as itemId, poi.item_name as itemName," +
            " poi.final_order_qty as orderQty , poi.destin_uom as lot, poi.final_order_price  as unitRate , po.type_flag as typeFlag " +
            " from purchase_order po left join purchase_order_items poi on po.id = poi.purchase_order_id "
            + " WHERE po.type_flag ='Factory' AND po.status='Authorized' AND poi.active = true AND (po.po_status ='Open' or po.po_status IS NULL) "
            + " AND (:poNumber is null or lower(po.po_no) like ('%' || lower(:poNumber) || '%')) "
            + " AND (:vendorId is null or po.vendor_id =:vendorId) "
            + " AND (:vendorId is null or po.company_branch_id =:plantid) "
            + " AND (:proposalid is null or po.proposal_id =:proposalid) ORDER BY po.id DESC", nativeQuery = true)
    Page<SupplierQty> getSupplierQty(@Param("poNumber") String poNumber, @Param("vendorId") Long vendorId,
                                     @Param("proposalid") Long proposalid, @Param("plantid") Long plantid, Pageable pageable);

    @Query(value = " SELECT distinct po.id as id , po.po_no as poNumber FROM purchase_order po left join purchase_order_items poi on poi.purchase_order_id = po.id   " +
            " WHERE  po.status ='Authorized'  AND  po.firm_id =:firmId  " +
            " AND po.vendor_id in (select l.id from ledger l  where  l.sister_concern_group_id = (select l1.sister_concern_group_id from ledger l1 where l1.id=:vendorId) )   " +
            " AND (poi.po_inward_status IS NULL OR poi.po_inward_status='Open') ", nativeQuery = true)
    List<POList> purchaseOrderFilterListBySisterConcernVendorId(@Param("firmId") Long firmId, @Param("vendorId") Long vendorId);


    @Query(value = " SELECT distinct po.id as id , po.po_no as poNumber  FROM purchase_order po left join purchase_order_items poi on poi.purchase_order_id = po.id  " +
            " WHERE po.firm_id =:firmId AND po.vendor_id =:vendorId  AND po.status ='Authorized'  " +
            " AND  (poi.po_inward_status IS NULL OR poi.po_inward_status='Open') order by po.id DESC", nativeQuery = true)
    List<POList> purchaseOrderFilterListByVendorIdInwardPo(@Param("firmId") Long firmId, @Param("vendorId") Long vendorId);

    @Query(value = "SELECT * FROM purchase_order po WHERE po.firm_id =:firmId AND po.vendor_id =:vendorId AND (po.po_status IS NULL OR po.po_status='Open') AND po.status ='Authorized'", nativeQuery = true)
    List<PurchaseOrder> purchaseOrderFilterListByVendorId(@Param("firmId") Long firmId, @Param("vendorId") Long vendorId);


    @Query(value = "select  SUM(poi.final_order_qty) as poQty  from purchase_order po " +
            " left join purchase_order_items poi on poi.purchase_order_id = po.id left join proposal p on po.proposal_id  = p.id" +
            " where po.proposal_id = :proposalId and po.vendor_id = :vendorId  AND poi.spec_id =:specID  GROUP BY po.proposal_id  , poi.spec_id  ", nativeQuery = true)
    Double findByQty(@Param("proposalId") Long proposalId, @Param("vendorId") Long vendorId, @Param("specID") Long specID);





    @Query(value = "SELECT * FROM purchase_order po WHERE po.type_flag = 'Factory' order by po.id DESC ", nativeQuery = true)
    List<PurchaseOrder> findAll();





}
