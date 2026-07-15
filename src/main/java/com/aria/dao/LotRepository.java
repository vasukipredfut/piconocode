package com.aria.dao;

import com.aria.model.Lot;

import java.util.List;
import java.util.Optional;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LotRepository extends JpaRepository<Lot, Long> {


    @Query(value = "SELECT * FROM lot WHERE (:productId is null or refid = :productId)", nativeQuery = true)
    List<Lot> findByLot(@Param("productId") Long productId);


    @Query(value = "select   distinct lot  from lot l  where l.lot_value IS NOT NULL ", nativeQuery = true)
    List<String> findByLotlist();


    @Query(value = "select max( concat_ws('-', b.name, l.lot)  ) AS dispName , b.name as name, b.id as productId, l.lot as lot  from products_master b left join lot l on l.refid = b.id where b.pcategory =:pcategory  group by b.name, b.id , l.lot ", nativeQuery = true)
    List<PCateProduct> findBy(@Param("pcategory") String pcategory);


    @Query(value = "select string_agg(lot_str, ',') as lot from lot Where refid =:refid", nativeQuery = true)
    String findBy(@Param("refid") Long refid);

    @Query(value = "SELECT * FROM lot WHERE  refid =:productId AND lot=:lot", nativeQuery = true)
    List<Lot> findByLot(@Param("productId") Long productId, @Param("lot") Double lot);


    @Query(value = "SELECT * FROM lot WHERE (:productId is null or refid = :productId) AND (:lot is null or lot =:lot) LIMIT 1", nativeQuery = true)
    Optional<Lot> findByLotANDProductId(@Param("productId") Long productId, @Param("lot") Double lot);


}
