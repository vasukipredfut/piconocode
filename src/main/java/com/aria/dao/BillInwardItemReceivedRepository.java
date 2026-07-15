package com.aria.dao;

import com.aria.dto.BillInwardItemReceives;
import com.aria.model.BillInwardItemReceived;


import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.aria.model.Lot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BillInwardItemReceivedRepository extends JpaRepository<BillInwardItemReceived, Long> {


    @Query(value = "SELECT br FROM BillInwardItemReceived br WHERE (br.inwardId =:inwardId)")
    List<BillInwardItemReceived> listBillInwardProducts(@Param("inwardId") Long inwardId);

    @Query(value = "SELECT br FROM BillInwardItemReceived br WHERE (br.batchNo =:batchNo) AND (br.inwardId =:inwardId)")
    List<BillInwardItemReceived> listBillInwardProductsByBatchNo(@Param("batchNo") String batchNo, @Param("inwardId") Long inwardId);

    @Query(value = "SELECT br FROM BillInwardItemReceived br WHERE br.batchNo =:batchNo")
    List<BillInwardItemReceived> listBillInwardBatchNo(@Param("batchNo") String batchNo);


    @Query(value = "SELECT COALESCE(SUM(received_quantity),0.0) FROM bill_inward_product_item_received bi WHERE bi.po_id=:poId", nativeQuery = true)
    Double sumOfPoQty(@Param("poId") Long poId);

    @Query(value = "SELECT COALESCE(SUM(received_quantity),0.0)  FROM bill_inward_product_item_received bi WHERE bi.po_id=:poId AND bi.item_id=:itemId ", nativeQuery = true)
    Double sumOfPoQty(@Param("poId") Long poId, @Param("itemId") Long itemId);


    @Query(value = "SELECT br FROM BillInwardItemReceived br WHERE (br.batchNo =:batchNo) AND (br.batchId =:batchId)")
    Optional<BillInwardItemReceived> findBillInwardProductsByBatchNo(@Param("batchNo") String batchNo, @Param("batchId") Long batchId);

    @Query(value = "select inward_id as inwardId, item_id as itemId, SUM(received_quantity) as qty, lot as  lot, po_id as poId FROM bill_inward_product_item_received WHERE inward_id =:inwardId GROUP BY item_id, inward_id ,po_id , lot ", nativeQuery = true)
    List<BillInwardItemReceives> findBillInwardProducts(@Param("inwardId") Long inwardId);


    @Query(value = "select * from bill_inward_product_item_received bi WHERE (bi.check_lorry_serial_date =:date) AND (bi.item_id =:itemId) order by bi.id desc limit 1", nativeQuery = true)
    Optional<BillInwardItemReceived> findByLorrySerialNo(@Param("date") Date date, @Param("itemId") Long itemId);


    @Query(value = "SELECT SUM(received_quantity)  FROM bill_inward_product_item_received bi WHERE bi.po_id=:poId AND bi.item_id=:itemId", nativeQuery = true)
    Double sumOfPoQtyWithPoIdANDItemId(@Param("poId") Long poId, @Param("itemId") Long itemId);

    @Query(value = "SELECT * FROM bill_inward_product_item_received bi WHERE bi.inward_id =:inwardId AND bi.item_id =:itemId LIMIT 1", nativeQuery = true)
    Optional<BillInwardItemReceived> listBillInwardProducts(@Param("inwardId") Long inwardId,@Param("itemId") Long itemId);


    @Query(value = "SELECT * FROM bill_inward_product_item_received bi WHERE bi.inward_id =:inwardId", nativeQuery = true)
    List<BillInwardItemReceived> findBy(@Param("inwardId") Long inwardId);



    @Query(value = "SELECT br FROM BillInwardItemReceived br WHERE (br.batchNo =:batchNo) AND (br.batchId =:batchId)")
    List<BillInwardItemReceived> findByBillInwardProductsByBatchNo(@Param("batchNo") String batchNo, @Param("batchId") Long batchId);


    @Query(value = "SELECT bi.lot FROM bill_inward_product_item_received bi WHERE bi.inward_id =:inwardId", nativeQuery = true)
    List<String> findByLots(@Param("inwardId") Long inwardId);


    @Query(value = "SELECT bi.* FROM bill_inward_product_item_received bi left join bill_inward b on bi.inward_id =b.id where b.product_category = 'consumables' ", nativeQuery = true)
    List<BillInwardItemReceived> findAll();


    @Query(value = "SELECT * FROM bill_inward_product_item_received br WHERE (br.batch_no =:batchNo) AND (br.item_id =:itemId) limit 1", nativeQuery = true)
    Optional<BillInwardItemReceived> findByProductsIdANDBatchNo( @Param("itemId") Long itemId, @Param("batchNo") String batchNo);


    @Query(value = "SELECT SUM(received_quantity)  FROM bill_inward_product_item_received bi WHERE bi.inward_id =:inwardId AND bi.item_id=:itemId", nativeQuery = true)
    Double sumOfReceivedQty(@Param("inwardId") Long inwardId, @Param("itemId") Long itemId);



    @Query(value = "SELECT * FROM bill_inward_product_item_received br WHERE (br.po_id =:poId) AND (br.item_id =:itemId) Limit 1",nativeQuery = true)
    Optional<BillInwardItemReceived> findBy(@Param("poId") Long poId, @Param("itemId") Long itemId);
}
