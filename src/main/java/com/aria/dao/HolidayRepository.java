package com.aria.dao;

import com.aria.model.Holiday;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Long> {


    @Query(value = "select * FROM holiday hd WHERE "
            + "(:holidayType is null or (lower(hd.holiday_type) LIKE ('%' || lower(:holidayType) || '%')) )"
            + "AND (:year is null or (lower(hd.year) LIKE ('%' || lower(:year) || '%')) )",nativeQuery = true)
    Page<Holiday> holidayFilterList(@Param("holidayType") String holidayType, @Param("year") String year,  Pageable pageable);

}
