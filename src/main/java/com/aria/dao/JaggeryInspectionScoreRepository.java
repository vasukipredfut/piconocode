package com.aria.dao;


import com.aria.model.JaggeryInspectionScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JaggeryInspectionScoreRepository extends JpaRepository<JaggeryInspectionScore, Long> {

    @Query(value = "SELECT * from jagginspscore j  WHERE j.uniqno =:uniqNo limit 1 " , nativeQuery = true)
    Optional<JaggeryInspectionScore> findBy(@Param("uniqNo") String uniqNo);

	Optional<JaggeryInspectionScore> findByIdAndUniqno(Long id, String uniqno);
}
