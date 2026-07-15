package com.aria.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.dto.WindmillPlanDto;
import com.aria.model.Windmill;

@Repository
public interface WindmillRepository extends JpaRepository<Windmill,Long> {
	
	@Query(value="select * from windmill w where w.type =:type",nativeQuery = true)
    List<Windmill> getPlans(@Param("type")String type);
	
	@Query("SELECT new com.aria.dto.WindmillPlanDto(w.id, w.name) " +
		       "FROM Windmill w WHERE w.type = :type")
		List<WindmillPlanDto> getPlan(@Param("type") String type);


}
