package com.aria.dao;

import com.aria.model.Districts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface DistrictsRepository extends JpaRepository<Districts, Long> {

    @Query(value = "SELECT  *  FROM districts d WHERE  " +
            "(:name is null or lower(d.name) like ('%' || lower(:name) || '%')) ", nativeQuery = true)
    List<Districts> findBy(@Param("name") String name);


}
