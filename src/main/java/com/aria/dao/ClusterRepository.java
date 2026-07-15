package com.aria.dao;

import com.aria.model.Cluster;
import com.aria.model.DropDownList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClusterRepository extends JpaRepository<Cluster, Long> {



    @Query(value = "SELECT id, name FROM cluster cl where (:name is null or (lower(cl.name) LIKE ('%' || lower(:name) || '%')))",nativeQuery = true)
    List<DropDownList> searchAllTClusterName(@Param("name") String name);
}
