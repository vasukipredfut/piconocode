package com.aria.dao;

import com.aria.model.HupMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Date;
import java.util.List;

@Repository
public interface HupMappingRepository  extends JpaRepository<HupMapping, Long> {


    @Query(value = "SELECT * FROM hup_mapping h WHERE h.date =:date", nativeQuery = true)
    List<HupMapping> findAllByDate (@Param("date") Date date);

}
