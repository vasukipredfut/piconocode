package com.aria.dao;

import com.aria.model.GoDownReportProductMenuTitleMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoDownReportProductMenuTitleMappingRepository extends JpaRepository<GoDownReportProductMenuTitleMapping,Long> {





    @Query(value = "SELECT gp.menu_name as manuName FROM go_down_report_product_menu_map gp " +
            " where  gp.item_id =:itemId order by menu_id asc " , nativeQuery = true)
    List<String> menulist (@Param("itemId") Long itemId);

}
