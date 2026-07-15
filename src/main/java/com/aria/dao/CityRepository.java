package com.aria.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.City;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {

	List<City> findByStateId(Long stateId);

	@Query(value = "SELECT * FROM city c WHERE LOWER(c.name) = LOWER(:name) Limit 1", nativeQuery = true)
	Optional<City> findByName(@Param("name") String name);

	@Query(value = "SELECT  *  FROM city c WHERE  "
			+ "(:name is null or lower(c.district) like ('%' || lower(:name) || '%')) ", nativeQuery = true)
	List<City> findBy(@Param("name") String name);


	@Query(value = "SELECT * FROM city c WHERE c.stateid =:stateId AND LOWER(c.name) = LOWER(:name) Limit 1", nativeQuery = true)
	Optional<City> findByName(@Param("name") String name,
							  @Param("stateId") Long stateId);

}
