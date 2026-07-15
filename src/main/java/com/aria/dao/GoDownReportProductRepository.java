package com.aria.dao;

import com.aria.model.GoDownReportProduct;
import com.aria.model.ProcessReportName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoDownReportProductRepository extends JpaRepository<GoDownReportProduct, Long> {


    @Query(value = "SELECT * FROM go_down_report_product gd WHERE " +
            " (:name is null OR lower(gd.name) LIKE ('%' || lower(:name) || '%')) ", nativeQuery = true)
    List<GoDownReportProduct> goDownSummaryProductList (@Param("name") String name );
}
