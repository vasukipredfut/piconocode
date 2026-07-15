package com.aria.dao;


import com.aria.model.RMProLots;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface RMProLotsRepository extends JpaRepository<RMProLots, Long> {




    @Query(value = "select max(concat_ws('-', b.name, r.lot)  ) AS dispName , b.name as name, b.id as productId, r.lot as lot  from brmproids b left join rmprolots r on r.refid = b.id where b.pcategory =:pcategory  group by b.name, b.id , r.lot " ,nativeQuery = true)
    List<PCateProduct> findBy(@Param("pcategory") String pcategory);

    @Query(value = "SELECT * FROM rmprolots WHERE (:productId is null or refid = :productId)", nativeQuery = true)
	List<RMProLots> findByRmProLot(Long productId);



}
