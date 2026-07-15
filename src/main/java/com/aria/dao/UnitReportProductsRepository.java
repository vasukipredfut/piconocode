package com.aria.dao;

import com.aria.dto.UnitReportMapList;
import com.aria.dto.UnitReportProductsList;
import com.aria.model.UnitReportProducts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UnitReportProductsRepository extends JpaRepository<UnitReportProducts, Long> {


    @Query(value = "select u.refid as productId from unitreportproductlist u  where u.yieldgroup = :yieldGroup  and input = 'Y' order by u.reportorder  ASC ", nativeQuery = true)
    List<Long> findByInput(@Param("yieldGroup") Long yieldGroup );



    @Query(value = "select u.refid as productId from unitreportproductlist u  where u.yieldgroup = :yieldGroup  and output = 'Y' order by u.reportorder  ASC ", nativeQuery = true)
    List<Long> findByOutput(@Param("yieldGroup") Long yieldGroup );



    @Query(value = "select u.refid as productId, pm.name as productName from unitreportproductlist u " +
            " left join products_master pm on pm.id = u.refid  where u.yieldgroup = :yieldGroup and input =:inputValue order by u.reportorder " +
            " asc", nativeQuery = true)
    List<UnitReportProductsList> findByInputProducts(@Param("yieldGroup") Long yieldGroup , @Param("inputValue") String inputValue);

}
