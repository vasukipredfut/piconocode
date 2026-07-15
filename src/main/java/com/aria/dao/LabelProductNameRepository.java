package com.aria.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.LabelProductName;

@Repository
public interface LabelProductNameRepository extends JpaRepository<LabelProductName, Long> {

	List<LabelProductName> findByProductGroupId(Long productGroupId);
	@Query(value = "SELECT * FROM labelproductname lp where lp.id =:id", nativeQuery=true)
	Optional<LabelProductName> findById(@Param("id") Long id);

}
