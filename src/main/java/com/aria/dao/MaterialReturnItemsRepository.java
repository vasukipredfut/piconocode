package com.aria.dao;

import com.aria.model.MaterialReturnItems;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface MaterialReturnItemsRepository extends JpaRepository<MaterialReturnItems, Long> {


    @Query(value = "SELECT * FROM return_items ob WHERE ob.return_id =:issueId AND ob.id=:id ", nativeQuery = true)
    List<MaterialReturnItems> findBy(@Param("issueId") Long issueId, @Param("id") Long id);

    @Query(value = "SELECT * FROM return_items ob WHERE ob.return_id =:returnId AND ob.item_id=:itemId AND ob.item_name =:itemName limit 1 ", nativeQuery = true)
    Optional<MaterialReturnItems> findBy(@Param("returnId") Long returnId, @Param("itemId") Long itemId,  @Param("itemName") String itemName);

    @Query(value = "SELECT ob.* FROM return_items ob left join material_return mr on mr.id = ob.return_id WHERE ob.status IS NOT NULL AND ob.return_date IS NOT NULL AND mr.sent_external_id =:sentId  AND ob.item_id =:itemId AND ob.item_name =:itemName AND ob.quantity =:quantity limit 1 ", nativeQuery = true)
    MaterialReturnItems findBy(@Param("sentId") Long sentId, @Param("itemId") Long itemId, @Param("itemName") String itemName, @Param("quantity") String quantity);


    @Query(value = "SELECT ob.* FROM return_items ob left join material_return mr on mr.id = ob.return_id WHERE ob.status IS NOT NULL AND ob.return_date IS NOT NULL AND mr.external_godown_id =:godownId  AND ob.item_id =:itemId AND ob.item_name =:itemName AND ob.quantity =:quantity limit 1 ", nativeQuery = true)
    MaterialReturnItems findByGodownId(@Param("godownId") Long sentId, @Param("itemId") Long itemId, @Param("itemName") String itemName, @Param("quantity") String quantity);

}
