package com.aria.dao;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.aria.dto.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.*;

@Repository
public interface InstruLogRepository extends JpaRepository<InstruLog, Long> {


    @Query(value = "SELECT DISTINCT date, enrollnumber  FROM instrulog i WHERE i.enrollnumber =:bioid", nativeQuery = true)
    List<InstruLog> findAllByUser(@Param("bioid") Long bioid);


    @Query(value = "SELECT *  FROM instrulog i WHERE i.enrollnumber =:bioid AND i.date =:date ORDER BY slno ASC", nativeQuery = true)
    List<InstruLog> findAllByUser(@Param("bioid") Long bioid , @Param("date") Date date);

    @Query(value = "SELECT *  FROM instrulog i WHERE i.enrollnumber = :bioid ORDER BY i.attdatetime DESC LIMIT 1", nativeQuery = true)
    Optional<InstruLog> findLatestByBioId(@Param("bioid") Long bioid);

    @Query(value = "SELECT  count(enrollnumber) as count, max(enrollnumber) as bioId FROM instrulog i group by i.enrollnumber , i.date", nativeQuery = true)
    List<InstruLogList> findAlls();


    @Query(value = "SELECT  count(enrollnumber) as count FROM instrulog i WHERE i.enrollnumber=:bioid AND (i.date >=:start AND i.date <=:end )   group by i.enrollnumber , i.date", nativeQuery = true)
    Integer getAttendanceDays(@Param("bioid") Long bioid,
                              @Param("start") Date start,
                              @Param("end") Date end);
    
    @Query(value="select i.enrollnumber as fingerid,i.date as date,i.attdatetime as dateandtime,i.slno as loggedid,i.sysip as fingermechineid,\r\n"
    		+ "b.\"location\" as location from employee_details ed  left join instrulog i on i.enrollnumber = ed.bioid left join biomast b on b.machineip=i.sysip\r\n"
    		+ "where i.date =:fromdate and ed.employeename =:employeename",nativeQuery = true)
    List<EmployeeDailyLogList>  findListofEmployeesByFromdate(@Param("fromdate") LocalDate fromdate,@Param("employeename") String employeename);


    @Query(value = "SELECT *  FROM instrulog i WHERE i.enrollnumber =:bioid AND i.date =:date ORDER BY i.attdatetime DESC LIMIT 1", nativeQuery = true)
    Optional<InstruLog> findLatestByBioId(@Param("bioid") Long bioid , @Param("date") Date date);


}
