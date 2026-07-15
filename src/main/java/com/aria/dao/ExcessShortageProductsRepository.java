package com.aria.dao;




import com.aria.model.BillExchange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.ExcessShortageProducts;

import java.util.List;
import java.util.Optional;


@Repository
public interface ExcessShortageProductsRepository extends JpaRepository<ExcessShortageProducts, Long> {


    @Query(value = "SELECT SUM(  ( CAST (ex.quantity  as DOUBLE PRECISION) ) * ( CAST  (ex.lot as DOUBLE precision) ) ) as qty  "
            + " from excess_shortage_products ex  "
            + " WHERE excess_Storage_Id=:excessShortageId GROUP BY excess_storage_id  ",nativeQuery = true)
    double findByKgQty(@Param("excessShortageId") Long excessShortageId);

    @Query(value = "SELECT COALESCE( SUM(  ( CAST (ex.excess_or_shortage  as DOUBLE PRECISION) ) ),0.0) as qty  "
            + " from excess_shortage_products ex  "
            + " WHERE excess_Storage_Id=:excessShortageId GROUP BY excess_storage_id  ",nativeQuery = true)
    double findByBagQty(@Param("excessShortageId") Long excessShortageId);



    @Query(value = "SELECT *  FROM excess_shortage_products be  WHERE  be.excess_Storage_Id =:id ", nativeQuery = true)
    List<ExcessShortageProducts> findBy(@Param("id") Long id);
}