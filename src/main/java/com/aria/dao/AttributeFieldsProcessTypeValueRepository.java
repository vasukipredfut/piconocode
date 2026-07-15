package com.aria.dao;


import com.aria.model.AttributeFieldsProcessTypeValueBased;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttributeFieldsProcessTypeValueRepository extends JpaRepository<AttributeFieldsProcessTypeValueBased,Long> {

    @Query(value = "SELECT cm FROM AttributeFieldsProcessTypeValueBased cm WHERE  cm.attributes_id = :id")
    List<AttributeFieldsProcessTypeValueBased> findBy(@Param("id") Long id);
}
