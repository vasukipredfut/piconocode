package com.aria.dao;

import com.aria.dto.EmployeeDailyLogList;
import com.aria.model.Dwlog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DwlogRepository extends JpaRepository<Dwlog, Long> {





@Query(value = "SELECT * FROM dwlog d WHERE d.bioid = :bioId ORDER BY d.attdatetime DESC limit 1",nativeQuery = true)
Optional<Dwlog> findByBioId(@Param("bioId") Long bioId);


@Query(value = "SELECT * FROM dwlog d WHERE d.slno = :slno",nativeQuery = true)
Optional<Dwlog> findBySlno(@Param("slno") Long slno);




@Query(value = "select i.bioid as fingerid,i.date as date,i.attdatetime as dateandtime,i.slno as loggedid,i.sysip as fingermechineid,"
 + " b.location as location from emast_daily  ed  left join dwlog i on i.bioid = ed.bioid left join biomast b on b.machineip=i.sysip "
 + " where i.date =:fromdate and ed.name =:employeename ",nativeQuery = true)
List<EmployeeDailyLogList> findListofWagesByFromdate(@Param("fromdate") LocalDate fromdate,@Param("employeename") String employeename);
    
}
