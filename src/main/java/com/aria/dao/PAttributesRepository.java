package com.aria.dao;

import com.aria.model.PAttributes;
import com.aria.model.Transport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PAttributesRepository extends JpaRepository<PAttributes, Long> {


    @Query(value = "SELECT *  FROM pattributes p WHERE u.name =:name", nativeQuery = true)
    Optional<PAttributes> findByName(@Param("name") String name);

    Boolean existsByName(String name);

    @Query(value = "SELECT be.* FROM pattributes be INNER JOIN department_details br ON br.id = be.dept WHERE (:attributesName is null or lower(be.name) like ('%' || lower(:attributesName) || '%')) "
            + "AND (:deptName is null or lower(br.name) like ('%' || lower(:deptName) || '%')) ",nativeQuery = true)
    Page<PAttributes> filterPAttributes(String attributesName,String deptName, Pageable pageable);


    @Query(value = "SELECT be.* FROM pattributes be INNER JOIN department_details br ON br.id = be.dept WHERE (:attributesName is null or lower(be.name) like ('%' || lower(:attributesName) || '%')) "
            + "AND (:deptName is null or br.id=:deptId) ",nativeQuery = true)
    List<PAttributes> filtersByPAttributes(String attributesName, Long deptId, Pageable pageable);
}
