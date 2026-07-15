package com.aria.dao;

import com.aria.model.HolidayDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface HolidayDetailsRepository extends JpaRepository<HolidayDetails, Long> {



    @Query(value = "select hd.date as date from holiday_details hd left join holiday h on h.id = hd.holiday_id where h.year =:trnYear and ( hd.date BETWEEN cast(:dateFrom  as date)  AND cast(:dateTo  as date) )  ", nativeQuery = true)
    public List<Date> findBy(@Param("trnYear") String trnYear,
                      @Param("dateFrom") Date dateFrom,
                      @Param("dateTo") Date dateTo);

}
