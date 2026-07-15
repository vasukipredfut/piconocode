package com.aria.dao;

import com.aria.model.LuZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.Tuple;
import java.util.List;
import java.util.Optional;

@Repository
public interface LuZoneRepository extends JpaRepository<LuZone, Long> {

    @Query(value = "select distinct name, id, wcid from luzone where branch=:branch and dept=:dept and dacat_id=:act and wagestype =:staffType ", nativeQuery = true)
    List<Tuple> findAll(@Param("branch") Long branch, @Param("dept") Long dept, @Param("act") Long act, @Param("staffType") String staffType);

    @Query(value = "select * from luzone where name=:name", nativeQuery = true)
    Optional<LuZone> findByName(@Param("name") String name);

    @Query(value = "select * from luzone where name=:name  and  branch=:branch", nativeQuery = true)
    Optional<LuZone> findByName(@Param("name") String name, @Param("branch") Long branch);

    @Query(value = "select  ss.id as designationId , ss.name as designation , max(COALESCE((ss.da),0.0) ) as da,  max(COALESCE((ss.allowance),0.0) ) as allowance ,max(COALESCE((ss.minbasic),0.0) ) as basic, max(ss.trnyear) as trnyear  from luzone l left join salary_structure ss on ss.id = l.female  where l.id =:luzoneId and ss.id <> 0 and ss.status = 'A'  group by l.id ,ss.id  , ss.name LIMIT 1", nativeQuery = true)
    WagesSal findByFeMale(@Param("luzoneId") Long luzoneId);

    @Query(value = "select  ss.id as designationId , ss.name as designation , max(COALESCE((ss.da),0.0) ) as da,  max(COALESCE((ss.allowance),0.0) ) as allowance , max(COALESCE((ss.minbasic),0.0) ) as basic, max(ss.trnyear) as trnyear  from luzone l left join salary_structure ss on ss.id = l.male  where l.id =:luzoneId and ss.id <> 0 and ss.status = 'A'  group by l.id ,ss.id  , ss.name LIMIT 1", nativeQuery = true)
    WagesSal findByMale(@Param("luzoneId") Long luzoneId);

    @Query(value = "select distinct name, id, wcid, wctable from luzone where branch=:branch and dept=:dept and dacat_id=:act and wagestype =:staffType AND male <> 0", nativeQuery = true)
    List<Tuple> findMaleAll(@Param("branch") Long branch, @Param("dept") Long dept, @Param("act") Long act, @Param("staffType") String staffType);

    @Query(value = "select distinct name, id, wcid , wctable from luzone where branch=:branch and dept=:dept and dacat_id=:act and wagestype =:staffType AND female <> 0", nativeQuery = true)
    List<Tuple> findFeMaleAll(@Param("branch") Long branch, @Param("dept") Long dept, @Param("act") Long act, @Param("staffType") String staffType);

    @Query(value = "select distinct name, id, wcid, wctable from luzone where branch=:branch and dept=:dept and dacat_id=:act and wagestype =:staffType ", nativeQuery = true)
    List<Tuple> luZoneStaffList(@Param("branch") Long branch, @Param("dept") Long dept, @Param("act") Long act, @Param("staffType") String staffType);

    @Query(value = "select  ss.id as designationId , ss.name as designation , max(COALESCE((ss.da),0.0) ) as da,  max(COALESCE((ss.allowance),0.0) ) as allowance ,max(COALESCE((ss.minbasic),0.0) ) as basic, max(ss.trnyear) as trnyear  from  salary_structure ss where ss.name=:name and ss.id <> 0 and ss.status = 'A'  group by ss.id  , ss.name LIMIT 1", nativeQuery = true)
    WagesSal findByCName(@Param("name") String name);
}
