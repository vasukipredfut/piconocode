package com.aria.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.AttributeFieldsProcessTypeOptionBased;

@Repository
public interface AttributeFieldsProcessTypeOptionRepository extends JpaRepository<AttributeFieldsProcessTypeOptionBased,Long> {

	@Query(value = "SELECT cm FROM AttributeFieldsProcessTypeOptionBased cm WHERE (:id is null or cm.attributes_id = :id) ")
	Optional<AttributeFieldsProcessTypeOptionBased> findByProcessTypeId(@Param("id") Long id);

	@Query(value = "SELECT cm FROM AttributeFieldsProcessTypeOptionBased cm WHERE (:id is null or cm.attributes_id = :id) ")
	List<AttributeFieldsProcessTypeOptionBased> findByProcessTypeOptionId(@Param("id") Long id);

	@Query(value = "SELECT cm FROM AttributeFieldsProcessTypeOptionBased cm WHERE  cm.attributes_id = :id")
	List<AttributeFieldsProcessTypeOptionBased> findBy(@Param("id") Long id);

}
