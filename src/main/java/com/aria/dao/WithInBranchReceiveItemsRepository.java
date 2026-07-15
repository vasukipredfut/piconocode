package com.aria.dao;

import com.aria.dto.WagesJournalWork;
import com.aria.model.WithInBranchReceiveItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WithInBranchReceiveItemsRepository extends JpaRepository<WithInBranchReceiveItems, Long> {



    @Query(value = "SELECT * FROM within_branch_receive_items ld WHERE within_id =:withinId and item_Id =:itemId  ",  nativeQuery = true)
    List<WithInBranchReceiveItems> findMaterialItemList(@Param("withinId") Long withinId, @Param("itemId") Long itemId);

    @Query(value = " SELECT w.item_id as productId , w.item_name as productName , w.select_lot as lot, " +
            "s.trn_no as trnNo, SUM( CAST (w.quantity  as DOUBLE PRECISION) ) as qty " +
            "FROM with_in_branch_receive s LEFT JOIN within_branch_receive_items w ON w.within_id = s.id  " +
            "WHERE (:trnNo is null or s.trn_no = :trnNo ) AND s.trn_no IS NOT NULL  " +
            "AND (w.sal_status IS NULL or w.sal_status <> 'Completed' )" +
            "group by w.item_id , w.item_name , s.trn_no , w.select_lot ",  nativeQuery = true)
    List<WagesJournalWork> mrnReceiptProductList(@Param("trnNo") String trnNo);




}
