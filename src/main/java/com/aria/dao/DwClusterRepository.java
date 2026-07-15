package com.aria.dao;

import com.aria.dto.ProductionDataSetList;
import com.aria.model.DwCluster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DwClusterRepository  extends JpaRepository<DwCluster,Long> {

    @Query(value = "SELECT *  FROM dwcluster dw "
            + "WHERE (:name is null or lower(dw.name) like ('%' || lower(:name) || '%'))  "
            +"AND (:branch is null or dw.branch_id = :branch) "
            +"AND (:dept is null or dw.dept_id = :dept) ", nativeQuery = true)
    List<DwCluster> findByList(@Param("name") String name, @Param("branch") Long branch, @Param("dept") Long dept);



}
