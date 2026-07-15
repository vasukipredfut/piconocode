package com.aria.dao;

import com.aria.model.JaggeryAttributesValues;
import com.aria.model.JaggeryInspection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JaggeryAttributesValuesRepository extends JpaRepository<JaggeryAttributesValues,Long> {

    @Query(value = "SELECT * from jaggattrvalues j  WHERE j.uniqno =:uniqNo limit 1" , nativeQuery = true)
    Optional<JaggeryAttributesValues> findBy(@Param("uniqNo") String uniqNo);

	Optional<JaggeryAttributesValues> findByIdAndUniqno(Long id, String uniqno);
}
