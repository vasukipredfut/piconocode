package com.aria.dao;


import com.aria.model.SupplierGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplierGroupRepo  extends JpaRepository<SupplierGroup, Long> {

    @Query(value = "select * from  suppliergroup sg WHERE (:name is null or lower(sg.name) like ('%' || lower(:name) || '%')) ", nativeQuery = true)
    List<SupplierGroup> findAll(@Param("name") String name);

}
