package com.aria.dao;

import com.aria.model.Camp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.Tuple;
import java.util.List;
import java.util.Optional;

@Repository
public interface CampRepository extends JpaRepository<Camp, Long> {




    @Query(value = "SELECT id, camp FROM camp  where camp <> '' AND (:state is null or state=:state)  ", nativeQuery = true)
    List<Tuple> findAlls(@Param("state") String state);


    @Query(value = "SELECT * FROM camp  where camp <> '' AND  place=:place  ", nativeQuery = true)
    List<Camp> campNameList(@Param("place") String place);

    @Query(value = "SELECT * FROM camp c where  lower(c.camp) like ('%' || lower(:camp) || '%')  LIMIT 1 ", nativeQuery = true)
    Optional<Camp> fndByCampName(@Param("camp") String camp);



}
