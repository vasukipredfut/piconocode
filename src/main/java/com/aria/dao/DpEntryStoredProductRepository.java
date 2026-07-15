package com.aria.dao;


import com.aria.dto.EntryProductList;
import com.aria.model.DpEntryStoredProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DpEntryStoredProductRepository extends JpaRepository<DpEntryStoredProduct, Long> {


    @Query(value = "SELECT  *  FROM direct_purchase_stored_product b WHERE b.entry_id =:entryId and b.batch_no =:batchNo and b.batch_id =:batchId ", nativeQuery = true)
    Optional<DpEntryStoredProduct> findByProductBatch(@Param("entryId") Long entryId, @Param("batchNo") String batchNo, @Param("batchId") long batchId);


    @Query(value = "SELECT  *  FROM direct_purchase_stored_product b WHERE b.id =:id and b.entry_id =:entryId and b.batch_no =:batchNo and b.batch_id =:batchId ", nativeQuery = true)
    Optional<DpEntryStoredProduct> findBy(@Param("id") Long id, @Param("entryId") Long entryId, @Param("batchNo") String batchNo, @Param("batchId") long batchId);


    @Query(value = "SELECT  *  FROM direct_purchase_stored_product b WHERE  b.batch_no =:batchNo limit 1", nativeQuery = true)
    DpEntryStoredProduct findByProductBatchNo(@Param("batchNo") String batchNo);

    @Query(value = "SELECT select_bin FROM  direct_purchase_stored_product where select_bin LIKE '%:name%'", nativeQuery = true)
    List<String> getAllBin(@Param("name") String name);

    @Query(value = "SELECT  *  FROM direct_purchase_stored_product b WHERE b.entry_id =:entryId ", nativeQuery = true)
    List<DpEntryStoredProduct> findBy( @Param("entryId") Long entryId);

    @Query(value = "SELECT  *  FROM direct_purchase_stored_product b WHERE b.bill_ref_id =:entryId ", nativeQuery = true)
    List<DpEntryStoredProduct> findByBillRefId( @Param("entryId") Long entryId);



    @Query(value = "SELECT  " +
            "b.id as id , b.batch_id as batchId , b.batch_no as batchNo , b.entry_id as entryId , " +
            "b.package_type as packageType , b.per_bag_weight as perBagWeight , " +
            "b.total_quantity as totalQuantity,  " +
            "b.camp_id as campId, b.color_id as colorId , c.name as colorName , " +
            "b.item_id as itemId, b.item_type as itemType , b.item_name as itemName , " +
            "po.advance_type as advanceType ,  " +
            "b.sgst as sgst, b.cgst as  cgst, b.igst as igst , " +
            "b.billamount as billamount ,   " +
            "b.po_id as poId , b.po_number as poNumber , " +
            "po.deliveryduedate  as deliveryduedate , po.proposal_id  as proposalId , " +
            "b.bill_ref_id as billRefId ,  " +
            "b.lot as lot , b.warranty_date as warrantyDate , b.warranty_certificate as warrantyCertificate,  " +
            "b.unit_cost as unitCost , b.unit_rate as unitRate , " +
            "bipir.sgst as sgst1, bipir.cgst as  cgst1, bipir.igst as igst1 ,bipir.freight_charges as freightCharges " +
            "FROM  " +
            "direct_purchase_stored_product b " +
            "left join color c on c.id = b.color_id    " +
            "left join purchase_order po   on po.id = b.po_id " +
            "left join bill_inward_product_item_received bipir on bipir.item_id = b.item_id and bipir.po_id  = po.id and bipir.inward_id = b.bill_ref_id " +
            " WHERE b.entry_id =:entryId  " +
            " group by  b.id , b.batch_id , b.batch_no  , b.entry_id  ,b.package_type  , b.per_bag_weight  , b.total_quantity  , " +
            "  b.camp_id  ,b.color_id  , c.name   , b.item_id , b.item_type , b.item_name,  po.advance_type  ,   " +
            "  b.sgst , b.cgst , b.igst  , b.billamount     ,b.po_id  , b.po_number   ,po.deliveryduedate   , po.proposal_id  , " +
            "  b.bill_ref_id    ,b.lot  , b.warranty_date , b.warranty_certificate  , b.unit_cost  , b.unit_rate  , bipir.sgst , bipir.cgst ," +
            "  bipir.igst , bipir.freight_charges", nativeQuery = true)
    List<EntryProductList> findAllByEntryId(@Param("entryId") Long entryId);


}
