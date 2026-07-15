package com.aria.dao;

import com.aria.dto.WagesJournalWork;
import com.aria.model.MaterialWithInBranchReceiptItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;

@Repository
public interface MaterialWithInBranchReceiptItemRepository extends JpaRepository<MaterialWithInBranchReceiptItem, Long> {

    @Query(value = "SELECT * FROM within_branch_receipt_items ld WHERE within_id =:withinId and item_id =:itemId",  nativeQuery = true)
    Optional<MaterialWithInBranchReceiptItem> findMaterialItem(@Param("withinId") Long withinId, @Param("itemId") Long itemId);

    @Query(value = "SELECT * FROM within_branch_receipt_items ld WHERE within_id =:withinId and item_id =:itemId  ",  nativeQuery = true)
    List<MaterialWithInBranchReceiptItem> findMaterialItemList(@Param("withinId") Long withinId, @Param("itemId") Long itemId);
    

}
